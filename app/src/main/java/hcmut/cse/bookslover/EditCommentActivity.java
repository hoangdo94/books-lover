package hcmut.cse.bookslover;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import javax.json.Json;
import javax.json.JsonObject;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;
import hcmut.cse.bookslover.models.Comment;
import hcmut.cse.bookslover.utils.APIRequest;
import hcmut.cse.bookslover.utils.CredentialsPrefs;

public class EditCommentActivity extends AppCompatActivity {

    EditText et;
    ImageView iv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_comment);
        setTitle("Chỉnh sửa");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Bundle extras = getIntent().getExtras();
        iv = (ImageView) findViewById(R.id.edit_comment_ava);
        et = (EditText) findViewById(R.id.edit_comment_et);
        final Button edit = (Button) findViewById(R.id.edit_comment_btnEdit);
        Button cancel = (Button) findViewById(R.id.edit_comment_btnCancel);
        et.setText(extras.getString("edit_comment"));
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals(extras.getString("edit_comment")) || s.toString().trim().equals("")){
                    edit.setClickable(false);
                    edit.setBackgroundColor(Color.parseColor("#d3d3d3"));
                }
                else {
                    edit.setClickable(true);
                    edit.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        Picasso.with(getApplicationContext())
                .load(CredentialsPrefs.getCurrentUser().getAbsoluteAvatarUrl())
                .resize(200, 200)
                .centerCrop()
                .into(iv);

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et.getText().toString().equals(extras.getString("edit_comment"))) {
                    Intent output = new Intent();
                    output.putExtra("change", "0");
                    setResult(RESULT_OK, output);
                    finish();
                } else {
                    JsonObject jo;
                    jo = Json.createObjectBuilder()
                            .add("content", et.getText().toString())
                            .build();
                    ByteArrayEntity entity = null;
                    try {
                        entity = new ByteArrayEntity(jo.toString().getBytes("UTF-8"));
                        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                        APIRequest.put(getApplicationContext(), "comments/" + extras.getString("id"), entity, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, final JSONObject responseBody) {
                                try {
                                    int status = responseBody.getInt("status");
                                    if (status == 1) {
                                        output("1");
                                    } else {
                                        output("2");
                                    }
                                } catch (JSONException e) {
                                    output("2");
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
                                output("2");
                            }
                        });
                    } catch (UnsupportedEncodingException e) {
                        output("2");
                    }

                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                output("0");
            }
        });
    }

    public void output(String id) {
        Intent output = new Intent();
        output.putExtra("change", id);
        if (id.equals("1"))
            output.putExtra("new_comment", et.getText().toString());
        setResult(RESULT_OK, output);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            output("0");
        }
        return super.onOptionsItemSelected(item);
    }
}
