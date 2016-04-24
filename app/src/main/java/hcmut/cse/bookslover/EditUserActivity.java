package hcmut.cse.bookslover;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import javax.json.Json;
import javax.json.JsonObject;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;
import hcmut.cse.bookslover.utils.APIRequest;
import hcmut.cse.bookslover.utils.CredentialsPrefs;

public class EditUserActivity extends AppCompatActivity {

    Button edit;
    AutoCompleteTextView txtUsername;
    AutoCompleteTextView txtName;
    AutoCompleteTextView txtEmail;
    EditText txtPassword;
    EditText txtRePassword;
    EditText txtAge;
    EditText txtWebsite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);
        setTitle("Cập nhật thông tin");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edit = (Button) findViewById(R.id.btnEdit);
        txtUsername = (AutoCompleteTextView) findViewById(R.id.username);
        txtName = (AutoCompleteTextView) findViewById(R.id.name);
        txtEmail = (AutoCompleteTextView) findViewById(R.id.email);
        txtPassword = (EditText) findViewById(R.id.password);
        txtRePassword = (EditText) findViewById(R.id.rePassword);
        txtAge = (EditText) findViewById(R.id.age);
        txtWebsite = (EditText) findViewById(R.id.website);
        //set previous user info
        txtUsername.setText(CredentialsPrefs.getCurrentUser().getUsername());
        txtName.setText(CredentialsPrefs.getCurrentUser().getName());
        txtEmail.setText(CredentialsPrefs.getCurrentUser().getEmail());
        if (CredentialsPrefs.getCurrentUser().getMeta().getAge() > -1)
            txtAge.setText(String.valueOf(CredentialsPrefs.getCurrentUser().getMeta().getAge()));
        txtWebsite.setText(CredentialsPrefs.getCurrentUser().getMeta().getWebsite());

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = txtUsername.getText().toString();
                final String password = txtPassword.getText().toString();
                final String rePassword = txtRePassword.getText().toString();
                final String email = txtEmail.getText().toString();
                final String name = txtName.getText().toString();
                final String age = txtAge.getText().toString();
                final String website = txtWebsite.getText().toString();

                if (username.isEmpty() || password.isEmpty()
                        || rePassword.isEmpty() || email.isEmpty()) {
                    displayToast("Tên đăng nhập, mật khẩu, nhập lại mật khẩu và email không được để trống");
                    return;
                }
                if (!password.equals(rePassword)) {
                    displayToast("Mật khẩu không khớp nhau");
                    return;
                }
                if (!isInteger(age)) {
                    displayToast("Tuổi phải là số");
                    return;
                }

                RequestParams params = new RequestParams();
                params.put("username", username);
                params.put("password", password);
                params.put("email", email);
                params.put("name", name);
                params.put("meta", "{'age': " + Integer.parseInt(age) + ", 'website': '" + website + "'}" );
//                params.put("website", website);
                JsonObject jo = Json.createObjectBuilder()
                        .add("username", username)
                        .add("password", password)
                        .add("email", email)
                        .add("name", name)
                        .add("meta", Json.createObjectBuilder()
                                .add("age", Integer.parseInt(age))
                                .add("website", website))
                        .build();
                ByteArrayEntity entity = null;
                try {
                    entity = new ByteArrayEntity(jo.toString().getBytes("UTF-8"));
                    entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    APIRequest.put(getApplicationContext(), "users/" + CredentialsPrefs.getCurrentUser().get_id(), entity, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject r) {
                            try {
                                int status = r.getInt("status");
                                if (status == 1) {
                                    //update success
                                    displayToast("Cập nhật thành công");
                                    CredentialsPrefs.saveCredentials(username, password);
                                    //update user info
                                    CredentialsPrefs.getCurrentUser().setUsername(username);
                                    CredentialsPrefs.getCurrentUser().setName(name);
                                    CredentialsPrefs.getCurrentUser().getMeta().setAge(Integer.parseInt(age));
                                    CredentialsPrefs.getCurrentUser().setEmail(email);
                                    CredentialsPrefs.getCurrentUser().getMeta().setWebsite(website);
                                    finish();
                                } else {
                                    //update failed
                                    displayToast("Cập nhật thất bại");
                                }
                            } catch (JSONException e) {
                                displayToast("Cập nhật thất bại");
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject r) {
                            //register fail
                            displayToast("Có lỗi xãy ra, hãy thử lại sau!");
                        }
                    });
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void displayToast(String message) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
