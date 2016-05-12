package hcmut.cse.bookslover;

import android.app.AlertDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

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
import hcmut.cse.bookslover.utils.APIRequest;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);
        setTitle("Thêm sách");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        scrollView = (ScrollView) findViewById(R.id.addBook_form);
        image = (ImageView) findViewById(R.id.bookCover);
        intro = (EditText) findViewById(R.id.intro);
        title = (EditText) findViewById(R.id.title);
        author = (EditText) findViewById(R.id.author);
        year = (EditText) findViewById(R.id.year);
        genre = (EditText) findViewById(R.id.genre);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        progressBar = (ProgressBar) findViewById(R.id.loading);

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

                if (!year.getText().toString().equals(""))
                    publishYear = Integer.parseInt(year.getText().toString());

                RequestParams params = new RequestParams();
                params.put("title", txtTitle);
                params.put("author", txtAuthor);
                if (!txtGenre.equals(""))
                    params.put("genres", txtGenre);
                if (publishYear > 0)
                    params.put("publishYear", publishYear);
                params.put("review", txtIntro);
                if (!coverUrl.equals(""))
                    params.put("cover", coverUrl);

                APIRequest.post("books", params, new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject r) {
                        try {
                            int status = r.getInt("status");
                            if (status == 1) {
                                Toast.makeText(getApplicationContext(), "Sách của bạn đã được thêm!", Toast.LENGTH_SHORT).show();
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
            progressBar.setVisibility(View.VISIBLE);
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

                postImage(destination.getPath());
            } else if (requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null,
                        null);
                Cursor cursor = cursorLoader.loadInBackground();
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();

                String selectedImagePath = cursor.getString(column_index);

                Bitmap bm;
                bm = BitmapFactory.decodeFile(selectedImagePath);
                postImage(selectedImagePath);
            }
            return;
        }
    }

    public void postImage(final String ImageLink){
        RequestParams params = new RequestParams();
        try {
            params.put("file", new File(ImageLink));
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
                        Picasso.with(getApplicationContext())
                                .load("http://api.ws.hoangdo.info/images/" + coverUrl)
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
                    } else {
                        displayToast("Tải ảnh lên thất bại!");
                        progressBar.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    displayToast("Có lỗi xảy ra trong quá trình tải ảnh lên!");
                    progressBar.setVisibility(View.GONE);
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
}
