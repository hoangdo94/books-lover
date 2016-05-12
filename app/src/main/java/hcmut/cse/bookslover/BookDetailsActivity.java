package hcmut.cse.bookslover;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.json.Json;
import javax.json.JsonObject;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;
import hcmut.cse.bookslover.models.Book;
import hcmut.cse.bookslover.models.Comment;
import hcmut.cse.bookslover.utils.APIRequest;
import hcmut.cse.bookslover.utils.CommentAdapter;
import hcmut.cse.bookslover.utils.CredentialsPrefs;

public class BookDetailsActivity extends AppCompatActivity {
    Book book;
    PopupWindow popupWindow;
    FloatingActionButton fab;
    RelativeLayout back_layout;
    private CommentAdapter adapter;
    ArrayList<Comment> comments = new ArrayList<>();
    View inflatedView;
    int editCmIndex;
    int currentPage;
    boolean isNext = false;
    boolean flag_loading = false;
    ProgressBar loading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Gson gson = new Gson();
        Intent intent = getIntent();
        book = gson.fromJson(intent.getStringExtra("data"), Book.class);
        setTitle(book.getTitle());

        ImageView cover = (ImageView) findViewById(R.id.img_cover);
        TextView title = (TextView) findViewById(R.id.tv_title);
        TextView author = (TextView) findViewById(R.id.tv_author);
        TextView year = (TextView) findViewById(R.id.tv_year);
        TextView genres = (TextView) findViewById(R.id.tv_genres);
        HtmlTextView review = (HtmlTextView) findViewById(R.id.tv_review);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        back_layout = (RelativeLayout) findViewById(R.id.back_dim_layout);
        Picasso.with(getApplicationContext())
                .load(book.getAbsoluteCoverUrl())
                .resize(240, 360)
                .centerCrop()
                .into(cover);
        try {
            title.setText(book.getTitle());
        } catch (Exception e) {
            title.setText(getResources().getString(R.string.text_unkown));
        }
        try {
            author.setText(book.getAuthor());
        } catch (Exception e) {
            author.setText(getResources().getString(R.string.text_unkown));
        }
        try {
            year.setText(book.getPublishYear());
        } catch (Exception e) {
            year.setText(getResources().getString(R.string.text_unkown));
        }
        try {
            String[] gs = book.getGenres();
            String genresText = "";
            for (int i=0; i<gs.length; i++) {
                if (i > 0) genresText += ", ";
                genresText += gs[i];
            }
            if (genresText.trim().isEmpty()) genresText = getResources().getString(R.string.text_unkown);
            genres.setText(genresText);
        } catch (Exception e) {
            genres.setText(getResources().getString(R.string.text_unkown));
        }
        try {
            review.setHtmlFromString(book.getReview(), new HtmlTextView.RemoteImageGetter());
        } catch (Exception e) {
            review.setHtmlFromString(" ", null);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comments.clear();
                currentPage = 1;
                onShowPopup(v);
                back_layout.setVisibility(View.VISIBLE);

                APIRequest.get("favorites/book/" + book.get_id(), null, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, final JSONObject responseBody) {
                        try {
                            int status = responseBody.getInt("status");

                            if (status == 1) {
                                JSONArray data = responseBody.getJSONArray("data");
                                TextView num_favorite = (TextView) inflatedView.findViewById(R.id.num_favorite);
                                if (data.length() == 0)
                                    num_favorite.setText("Hãy là người đầu tiên thích sách này");
                                else
                                    num_favorite.setText(Integer.toString(data.length()));
                            } else {
                                displayToast("Không thể tải được số lượt yêu thích!");
                            }
                        } catch (JSONException e) {
                            displayToast("Có lỗi xảy ra!");
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
                        displayToast("Có lỗi xảy ra!");
                    }
                });

                getComment();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onShowPopup(View v){
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // inflate the custom popup layout
        inflatedView = layoutInflater.inflate(R.layout.popup_layout, null, false);
        loading = (ProgressBar) inflatedView.findViewById(R.id.loading);
        // get device size
        final Display display = getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);

        // set height depends on the device size
        popupWindow = new PopupWindow(inflatedView, size.x, FrameLayout.LayoutParams.WRAP_CONTENT);

        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_window));
        popupWindow.setFocusable(true);
        popupWindow.setAnimationStyle(R.style.Animation);
        popupWindow.showAtLocation(v, Gravity.TOP, 0, 0);
        final ImageView send = (ImageView) inflatedView.findViewById(R.id.sendBtn);
        final EditText cm = (EditText) inflatedView.findViewById(R.id.writeComment);
        cm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().equals(""))
                    send.setVisibility(View.VISIBLE);
                else
                    send.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!CredentialsPrefs.isLoggedIn()) {
                    displayToast("Bạn chưa đăng nhập!");
                    return;
                }
                loading.setVisibility(View.VISIBLE);
                JsonObject jo;
                jo = Json.createObjectBuilder()
                        .add("content", cm.getText().toString())
                        .build();
                ByteArrayEntity entity = null;
                try {
                    entity = new ByteArrayEntity(jo.toString().getBytes("UTF-8"));
                    entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    APIRequest.post(getApplicationContext(), "comments/" + book.get_id(), entity, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, final JSONObject responseBody) {
                            try {
                                int status = responseBody.getInt("status");
                                if (status == 1) {
                                    Gson gson = new Gson();
                                    comments.add(0, gson.fromJson(responseBody.getString("data"), Comment.class));
                                    adapter.notifyDataSetChanged();
                                    cm.setText("");
                                } else {
                                    displayToast("Bình luận không thành công!");
                                }
                            } catch (JSONException e) {
                                displayToast("Có lỗi xảy ra!");
                                e.printStackTrace();
                            }
                            loading.setVisibility(View.GONE);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
                            displayToast("Có lỗi xảy ra!");
                            loading.setVisibility(View.GONE);
                        }
                    });
                } catch (UnsupportedEncodingException e) {
                    displayToast("Có lỗi xảy ra!");
                    loading.setVisibility(View.GONE);
                    e.printStackTrace();
                }

            }
        });
        inflatedView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int orgX = 0, orgY = 0;
                int offsetX, offsetY;

                switch (motionEvent.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        orgX = (int) motionEvent.getRawX();
                        orgY = (int) motionEvent.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        offsetX = (int) motionEvent.getRawX() - orgX;
                        offsetY = (int) motionEvent.getRawY() - orgY;
                        if (offsetY > 600) {
                            popupWindow.dismiss();
                            break;
                        }

                        popupWindow.update(0, offsetY, -1, size.y - offsetY);
                        break;
                    case MotionEvent.ACTION_UP:
                        popupWindow.update(0, 0, -1, -2);
                        break;
                }
                return true;
            }
        });

        //listview and its listener
        final ListView listView = (ListView) inflatedView.findViewById(R.id.commentsListView);
        adapter = new CommentAdapter(getApplicationContext(), comments);
        listView.setAdapter(adapter);
        //add listener
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Comment touchCm = (Comment) listView.getItemAtPosition(position);

                if (CredentialsPrefs.isLoggedIn() && CredentialsPrefs.getUsername().equals(touchCm.getUser().getUsername())) {
                    CharSequence[] items = {"Sửa", "Xóa", "Hủy"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(BookDetailsActivity.this);
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {

                            if (item == 0) {
                                editCmIndex = comments.indexOf(touchCm);
                                Intent intent = new Intent(getApplicationContext(), EditCommentActivity.class);
                                intent.putExtra("edit_comment", touchCm.getContent());
                                intent.putExtra("id", touchCm.get_id());
                                startActivityForResult(intent, 1);
                            } else if (item == 1) {
                                APIRequest.delete("comments/" + touchCm.get_id(), null, new JsonHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, final JSONObject responseBody) {
                                        try {
                                            int status = responseBody.getInt("status");
                                            if (status == 1) {
                                                comments.remove(touchCm);
                                                adapter.notifyDataSetChanged();
                                                displayToast("Đã xóa!");
                                            } else {
                                                displayToast("Có lỗi xảy ra!");
                                            }
                                        } catch (JSONException e) {
                                            displayToast("Có lỗi xảy ra!");
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
                                        displayToast("Có lỗi xảy ra!");
                                    }
                                });
                            } else if (item == 2) {
                                dialog.dismiss();
                            }
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();

                    wmlp.gravity = Gravity.TOP | Gravity.LEFT;
                    wmlp.x = 0;
                    wmlp.y = (int) view.getY() + 90;

                    dialog.show();
                    dialog.getWindow().setLayout(240, WindowManager.LayoutParams.WRAP_CONTENT);
                }
                return false;
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
                    if (flag_loading == false) {
                        flag_loading = true;
                        getComment();
                    }
                }
            }
        });

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                send.setOnClickListener(null);
                cm.addTextChangedListener(null);
                inflatedView.setOnTouchListener(null);
                listView.setOnScrollListener(null);
                listView.setOnItemLongClickListener(null);
                back_layout.setVisibility(View.GONE);
            }
        });

    }

    public void displayToast(String message) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                if (data.getStringExtra("change").equals("1")) {
                    comments.get(editCmIndex).setContent(data.getStringExtra("new_comment"));
                    adapter.notifyDataSetChanged();
                    displayToast("Đã cập nhật!");
                }
                else if (data.getStringExtra("change").equals("2"))
                    displayToast("Có lỗi xảy ra!");
            }
        }

    }

    public void getComment() {
        APIRequest.get("comments/" + book.get_id() + "?page=" + Integer.toString(currentPage), null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, final JSONObject responseBody) {
                try {
                    int status = responseBody.getInt("status");
                    isNext = responseBody.getInt("page") <= responseBody.getInt("pages");
                    if (responseBody.getInt("total") == 0)
                        inflatedView.findViewById(R.id.no_comment).setVisibility(View.VISIBLE);
                    if (status == 1) {
                        JSONArray data = responseBody.getJSONArray("data");

                        Gson gson = new Gson();

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject c = data.getJSONObject(i);
                            Comment comment = gson.fromJson(c.toString(), Comment.class);
                            comments.add(comment);
                        }

                        adapter.notifyDataSetChanged();

                        if (isNext) {
                            currentPage += 1;
                            flag_loading = false;
                        }

                    } else {
                        displayToast("Không thể tải được các bình luận!");
                    }
                } catch (JSONException e) {
                    displayToast("Có lỗi xảy ra!");
                    e.printStackTrace();
                }

                loading.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
                displayToast("Có lỗi xảy ra!");
                loading.setVisibility(View.GONE);
            }
        });
    }
}
