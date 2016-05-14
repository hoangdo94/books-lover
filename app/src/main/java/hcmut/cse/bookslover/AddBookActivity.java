package hcmut.cse.bookslover;

import android.app.AlertDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cz.msebera.android.httpclient.Header;
import hcmut.cse.bookslover.models.Book;
import hcmut.cse.bookslover.utils.APIRequest;
import hcmut.cse.bookslover.utils.ImageHandler;

public class AddBookActivity extends AppCompatActivity {
    int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    ScrollView scrollView;
    ImageView image;
    EditText intro;
    EditText title;
    EditText author;
    EditText year;
    EditText genre;
    Button btnAdd;
    String coverUrl = "";
    ProgressBar progressBar;
    boolean isEdit = false;
    Book book;
    String imageLink = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);
        setTitle("Thêm sách");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        scrollView = (ScrollView) findViewById(R.id.addBook_form);
        image = (ImageView) findViewById(R.id.bookCover);
        title = (EditText) findViewById(R.id.title);
        author = (EditText) findViewById(R.id.author);
        year = (EditText) findViewById(R.id.year);
        genre = (EditText) findViewById(R.id.genre);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        progressBar = (ProgressBar) findViewById(R.id.loading);

        intro = (EditText) findViewById(R.id.intro);

        Intent checkIntent = getIntent();
        Bundle extras = checkIntent.getExtras();
        if (extras != null) {
            setTitle("Chỉnh sửa sách");
            isEdit = true;
            btnAdd.setText(R.string.action_edit);
            progressBar.setVisibility(View.VISIBLE);
            book = new Gson().fromJson(extras.getString("book"), Book.class);
            Picasso.with(getApplicationContext())
                    .load(book.getAbsoluteCoverUrl())
                    .resize(320, 480)
                    .centerCrop()
                    .into(image, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            displayToast("Không thể tải ảnh thành công!");
                            progressBar.setVisibility(View.GONE);
                        }
                    });
            try {
                title.setText(book.getTitle());
            } catch (Exception e) {
                title.setText("");
            }
            try {
                author.setText(book.getAuthor());
            } catch (Exception e) {
                author.setText("");
            }
            try {
                year.setText(book.getPublishYear());
            } catch (Exception e) {
                year.setText("");
            }
            try {
                String[] gs = book.getGenres();
                String genresText = "";
                for (int i=0; i<gs.length; i++) {
                    if (i > 0) genresText += ", ";
                    genresText += gs[i];
                }
                if (genresText.trim().isEmpty()) genresText = "";
                genre.setText(genresText);
            } catch (Exception e) {
                genre.setText("");
            }
            try {
                intro.setText(book.getReview());
            } catch (Exception e) {
                intro.setText("");
            }
        } else {
            book = new Book();
        }

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        intro.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.intro) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String txtTitle = title.getText().toString();
                String txtAuthor = author.getText().toString();
                String txtGenre = genre.getText().toString();
                String txtIntro = intro.getText().toString();
                int publishYear = -1;

                if (txtTitle.isEmpty() || txtAuthor.isEmpty() || txtIntro.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Bạn phải nhập tên sách và tác giả, đánh giá về sách!", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);

                if (!year.getText().toString().equals(""))
                    publishYear = Integer.parseInt(year.getText().toString());

                RequestParams params = new RequestParams();
                params.put("title", txtTitle);
                book.setTitle(txtTitle);
                params.put("author", txtAuthor);
                book.setAuthor(txtAuthor);
                if (!txtGenre.equals("")) {
                    params.put("genres", txtGenre);
                    String gs[] = txtGenre.split(",");
                    for (int i = 0; i < gs.length; i++) {
                        gs[i] = gs[i].trim();
                    }
                    book.setGenres(gs);
                }
                if (publishYear > 0) {
                    params.put("publishYear", publishYear);
                    book.setPublishYear(Integer.toString(publishYear));
                }
                params.put("review", txtIntro);
                book.setReview(txtIntro);
                if (!imageLink.equals(""))
                    postImage(params);
                else
                    postData(params);
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

    private void selectImage() {
        final CharSequence[] items = {"Chụp ảnh mới", "Chọn ảnh có sẵn", "Hủy"};

        AlertDialog.Builder builder = new AlertDialog.Builder(AddBookActivity.this);
        builder.setTitle("Bìa sách");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Chụp ảnh mới")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Chọn ảnh có sẵn")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
                } else if (items[item].equals("Hủy")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                File destination = new File(Environment.getExternalStorageDirectory(),
                        System.currentTimeMillis() + ".jpg");

                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageLink = destination.getPath();
            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null,
                        null);
                Cursor cursor = cursorLoader.loadInBackground();
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();

                String selectedImagePath = cursor.getString(column_index);

                imageLink = selectedImagePath;

            }

            image.setImageBitmap(ImageHandler.resize(BitmapFactory.decodeFile(imageLink), 320, 480));
            return;
        }
    }

    public void postImage(final RequestParams data_params){
        RequestParams params = new RequestParams();
        try {
            params.put("file", new File(imageLink));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        APIRequest.upload(params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, final JSONObject responseBody) {
                try {
                    int status = responseBody.getInt("status");
                    coverUrl = responseBody.getJSONObject("data").get("url").toString();
                    if (status == 1) {
                        postData(data_params);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        displayToast("Tải ảnh lên thất bại!");
                    }
                } catch (JSONException e) {
                    progressBar.setVisibility(View.GONE);
                    displayToast("Có lỗi xảy ra trong quá trình tải ảnh lên!");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
                displayToast("Tải ảnh lên không thành công!");
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                displayToast("Dung lượng ảnh quá lớn! Không thể tải lên thành công");
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    public void displayToast(String message) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }

    public void postData(final RequestParams data_params) {
        if (!coverUrl.equals("")) {
            data_params.put("cover", coverUrl);
            book.setCover(coverUrl);
        }
        if (!isEdit)
            APIRequest.post("books", data_params, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject r) {
                    try {
                        int status = r.getInt("status");
                        if (status == 1) {
                            Toast.makeText(getApplicationContext(), "Sách của bạn đã được thêm!", Toast.LENGTH_SHORT).show();
                            Intent output = new Intent();
                            output.putExtra("data", new Gson().toJson(book, Book.class).toString());
                            setResult(RESULT_OK, output);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Thêm sách thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Có lỗi xảy ra!", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject r) {
                    Toast.makeText(getApplicationContext(), "Thêm sách thất bại!", Toast.LENGTH_SHORT).show();
                }
            });
        else
            APIRequest.put("books/" + book.get_id(), data_params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject r) {
                    try {
                        int status = r.getInt("status");
                        if (status == 1) {
                            displayToast("Đã chỉnh sửa");
                            Intent output = new Intent();
                            output.putExtra("data", new Gson().toJson(book, Book.class).toString());
                            setResult(RESULT_OK, output);
                            finish();
                        } else {
                            displayToast("Có lỗi xảy ra!");
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        displayToast("Có lỗi xảy ra!");
                        finish();
                    }
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject r) {
                    displayToast("Có lỗi xảy ra!");
                    progressBar.setVisibility(View.GONE);
                    finish();
                }
            });
    }
}
