package hcmut.cse.bookslover;

import android.app.AlertDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.MenuItem;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Callback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import hcmut.cse.bookslover.models.User;
import hcmut.cse.bookslover.utils.APIRequest;
import hcmut.cse.bookslover.utils.BlurTransform;
import hcmut.cse.bookslover.utils.CircleTransform;
import hcmut.cse.bookslover.utils.CredentialsPrefs;
import hcmut.cse.bookslover.utils.UserAdapter;
import hcmut.cse.bookslover.utils.ImageHandler;


public class UserProfileActivity extends AppCompatActivity {
    int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    Button btnSelect;
    ImageView ivImage;
    CustomLayout layout;
    ListView userInfo;
    TextView nameTV;
    TextView emailTV;
    private UserAdapter adapter;
    boolean change = false;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        setTitle("Thông tin người dùng");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ivImage = (ImageView) findViewById(R.id.ivImage);
        userInfo = (ListView) findViewById(R.id.userInfo);
        nameTV = (TextView) findViewById(R.id.nameTV);
        emailTV = (TextView) findViewById(R.id.emailTV);

        nameTV.setText(CredentialsPrefs.getCurrentUser().getName());
        emailTV.setText(CredentialsPrefs.getCurrentUser().getEmail());

        progressBar = (ProgressBar) findViewById(R.id.loading);
        //load avatar
        Picasso.with(getApplicationContext()).load(CredentialsPrefs.getCurrentUser().getAbsoluteAvatarUrl()).resize(200, 200).centerCrop().transform(new CircleTransform(100, 0)).into(ivImage, new Callback() {
            @Override
            public void onSuccess() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {//You will get your bitmap here
                        progressBar.setVisibility(View.GONE);
                    }
                }, 100);
            }
            @Override
            public void onError() {

            }
        });

        layout = (CustomLayout) findViewById(R.id.baseInfo);
        Picasso.with(getApplicationContext()).load(CredentialsPrefs.getCurrentUser().getAbsoluteAvatarUrl()).resize(350, 250).centerCrop().transform(new BlurTransform(this, 10)).into(layout);
        //load user info
        ArrayList<User> personList = new ArrayList<>();
        personList.add(CredentialsPrefs.getCurrentUser());
        adapter = new UserAdapter(getApplicationContext(), personList);
        userInfo.setAdapter(adapter);
        ivImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        //edit button click
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EditUserActivity.class);
                startActivityForResult(intent, 2);
            }
        });

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent output = new Intent();
            if (change)
                output.putExtra("avatar", "1");
            else
                output.putExtra("avatar", "0");
            setResult(RESULT_OK, output);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectImage() {
        final CharSequence[] items = {"Chụp ảnh mới", "Chọn ảnh có sẵn", "Hủy"};

        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
        builder.setTitle("Ảnh đại diện");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (item == 1) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
                } else if (item == 2) {
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
        if (requestCode == 2) {
            nameTV.setText(CredentialsPrefs.getCurrentUser().getName());
            emailTV.setText(CredentialsPrefs.getCurrentUser().getEmail());
            ArrayList<User> personList = new ArrayList<>();
            personList.add(CredentialsPrefs.getCurrentUser());
            adapter = new UserAdapter(getApplicationContext(), personList);
            userInfo.setAdapter(adapter);
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
                    final String url = responseBody.getJSONObject("data").get("url").toString();
                    if (status == 1) {
                        // update user info
                        RequestParams params = new RequestParams();
                        params.put("avatar", url);
                        APIRequest.put("users/" + CredentialsPrefs.getCurrentUser().get_id(), params, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject r) {
                                try {
                                    int status = r.getInt("status");
                                    if (status == 1) {
                                        //update success
                                        //update avatar and background
                                        ivImage.setImageBitmap(ImageHandler.getCircularBitmap(ImageHandler.resize(BitmapFactory.decodeFile(ImageLink), 200, 200), 5));
                                        layout.setBackground(new BitmapDrawable(ImageHandler.resize(ImageHandler.blurBitmap(BitmapFactory.decodeFile(ImageLink), getApplicationContext()), 350, 250)));
                                        CredentialsPrefs.getCurrentUser().setAvatar(url);
                                        change = true;
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        //update fail
                                        change = false;
                                        progressBar.setVisibility(View.GONE);
                                        displayToast("Cập nhật ảnh đại diện thất bại");
                                    }
                                } catch (JSONException e) {
                                    change = false;
                                    progressBar.setVisibility(View.GONE);
                                    displayToast("Có lỗi xảy ra trong khi đang cập nhật ảnh đại diện!");
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject r) {
                                //update fail
                                change = false;
                                progressBar.setVisibility(View.GONE);
                                displayToast("Cập nhật ảnh đại diện không thành công!");
                            }
                        });
                    } else {
                        change = false;
                        progressBar.setVisibility(View.GONE);
                        displayToast("Tải ảnh lên thất bại!");
                    }
                } catch (JSONException e) {
                    change = false;
                    progressBar.setVisibility(View.GONE);
                    displayToast("Có lỗi xảy ra trong quá trình tải ảnh lên!");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
                change = false;
                progressBar.setVisibility(View.GONE);
                displayToast("Tải ảnh lên không thành công!");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                change = false;
                progressBar.setVisibility(View.GONE);
                displayToast("Dung lượng ảnh quá lớn! Không thể tải lên thành công");
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
    public void onBackPressed() {
        Intent output = new Intent();
        if (change)
            output.putExtra("avatar", "1");
        else
            output.putExtra("avatar", "0");
        setResult(RESULT_OK, output);
        finish();
    }

}