package hcmut.cse.bookslover;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import hcmut.cse.bookslover.models.User;
import hcmut.cse.bookslover.utils.APIRequest;
import hcmut.cse.bookslover.utils.CredentialsPrefs;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setTitle("Đăng Kí");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final EditText txtUsername = (EditText) findViewById(R.id.username);
        final EditText txtPassword = (EditText) findViewById(R.id.password);
        final EditText txtRePassword = (EditText) findViewById(R.id.rePassword);
        final EditText txtEmail = (EditText) findViewById(R.id.email);
        final Button btnLogin = (Button) findViewById(R.id.btnLogin);
        final Button btnRegister = (Button) findViewById(R.id.btnRegister);

        if (btnRegister != null) {
            btnRegister.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Perform action on click
                    final String username = txtUsername.getText().toString();
                    final String password = txtPassword.getText().toString();
                    final String rePassword = txtRePassword.getText().toString();
                    final String email = txtEmail.getText().toString();
                    if (username.isEmpty() || password.isEmpty() || rePassword.isEmpty() || email.isEmpty()) return;
                    if (!rePassword.equals(password)) {
                        displayToast("Mật khẩu không khớp nhau!");
                        return;
                    }

                    RequestParams params = new RequestParams();
                    params.put("username", username);
                    params.put("password", password);
                    params.put("email", email);

                    APIRequest.post("users", params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject r) {
                            try {
                                int status = r.getInt("status");
                                if (status == 1) {
                                    //register success
                                    displayToast("Đăng kí thành công!");
                                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                    intent.putExtra("username", username);
                                    intent.putExtra("password", password);
                                    finish();
                                    startActivity(intent);
                                } else {
                                    String message = r.getString("message");
                                    //register fail
                                    displayToast("Đăng kí thất bại: " + message);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject r) {
                            //register fail
                            displayToast("Có lỗi xãy ra, hãy thử lại sau!");
                        }
                    });
                }
            });
        }

        if (btnLogin != null) {
            btnLogin.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    finish();
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void displayToast(String message) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }
}
