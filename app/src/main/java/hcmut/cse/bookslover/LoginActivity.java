package hcmut.cse.bookslover;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import hcmut.cse.bookslover.utils.APIRequest;
import hcmut.cse.bookslover.utils.CredentialsPrefs;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setTitle("Đăng Nhập/ Đăng Kí");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final EditText txtUsername = (EditText) findViewById(R.id.txtUsername);
        final EditText txtPassword = (EditText) findViewById(R.id.txtPassword);
        final TextView tvRePassword = (TextView) findViewById(R.id.textView7);
        final EditText txtRePassword = (EditText) findViewById(R.id.txtRePassword);
        final TextView tvEmail = (TextView) findViewById(R.id.textView8);
        final EditText txtEmail = (EditText) findViewById(R.id.txtEmail);
        final Button btnLogin = (Button) findViewById(R.id.btnLogin);
        final Button btnRegister = (Button) findViewById(R.id.btnRegister);


        if (btnLogin != null) {
            btnLogin.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (btnLogin.getText().equals("Login")) {
                        // Perform action on click
                        final String username = txtUsername.getText().toString();
                        final String password = txtPassword.getText().toString();
                        if (username == null || password == null) return;
                        APIRequest.authenticate(username, password, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject r) {
                                try {
                                    int status = r.getInt("status");
                                    if (status == 1) {
                                        //login success
                                        displayToast("Logged In!");
                                        txtUsername.setText("");
                                        txtPassword.setText("");
                                        CredentialsPrefs.setCredentials(username, password);
                                        JSONObject user = r.getJSONObject("data");
                                        Boolean admin = user.getBoolean("admin");
                                        if (admin) {
                                            //admin login
                                        } else {
                                            //normal user login
                                        }
                                        //TODO - request to get user info
                                        finish();
                                    } else {
                                        //login fail
                                        displayToast("Failed to Login");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject r) {
                                //login fail
                                displayToast("Failed to Login");
                            }
                        });
                    }
                    else {
                        // Perform action on click
                        final String username = txtUsername.getText().toString();
                        final String password = txtPassword.getText().toString();
                        final String rePassword = txtRePassword.getText().toString();
                        final String email = txtEmail.getText().toString();
                        if (username == null || password == null || rePassword == null || email == null) return;
                        if (!rePassword.equals(password)) {
                            displayToast("Password and Re-password do not match!");
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
                                        displayToast("Registered! Enter again to Login");
                                        txtUsername.setText("");
                                        txtPassword.setText("");
                                        txtRePassword.setText("");
                                        txtEmail.setText("");
                                        tvRePassword.setVisibility(View.GONE);
                                        tvEmail.setVisibility(View.GONE);
                                        txtRePassword.setVisibility(View.GONE);
                                        txtEmail.setVisibility(View.GONE);
                                        btnLogin.setText("Login");
                                        btnRegister.setText("Not a member? Register now!");
                                    } else {
                                        //register fail
                                        displayToast("Failed to Register");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject r) {
                                //register fail
                                displayToast("Failed to Register");
                            }
                        });
                    }
                }
            });
        }

        if (btnRegister != null) {
            btnRegister.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Set visible
                    if (btnRegister.getText().equals("Not a member? Register now!")) {
                        tvRePassword.setVisibility(View.VISIBLE);
                        tvEmail.setVisibility(View.VISIBLE);
                        txtRePassword.setVisibility(View.VISIBLE);
                        txtEmail.setVisibility(View.VISIBLE);
                        btnLogin.setText("Register");
                        btnRegister.setText("Already registered! Login here.");
                    }
                    else {
                        tvRePassword.setVisibility(View.GONE);
                        tvEmail.setVisibility(View.GONE);
                        txtRePassword.setVisibility(View.GONE);
                        txtEmail.setVisibility(View.GONE);
                        btnLogin.setText("Login");
                        btnRegister.setText("Not a member? Register now!");
                    }
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
