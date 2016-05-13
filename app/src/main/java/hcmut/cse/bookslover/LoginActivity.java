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
import com.pushbots.push.Pushbots;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import hcmut.cse.bookslover.models.User;
import hcmut.cse.bookslover.utils.APIRequest;
import hcmut.cse.bookslover.utils.CredentialsPrefs;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setTitle("Đăng Nhập");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final AutoCompleteTextView txtUsername = (AutoCompleteTextView) findViewById(R.id.username);
        final EditText txtPassword = (EditText) findViewById(R.id.password);
        final Button btnLogin = (Button) findViewById(R.id.btnLogin);
        final Button btnRegister = (Button) findViewById(R.id.btnRegister);

        // set value after register successful
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            txtUsername.setText(extras.getString("username"));
            txtPassword.setText(extras.getString("password"));
        }

        if (btnLogin != null) {
            btnLogin.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Perform action on click
                    final String username = txtUsername.getText().toString();
                    final String password = txtPassword.getText().toString();
                    if (username.isEmpty() || password.isEmpty()) return;
                    APIRequest.authenticate(username, password, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject r) {
                            try {
                                int status = r.getInt("status");
                                if (status == 1) {
                                    //login success
                                    displayToast("Đăng nhập thành công!");
                                    txtUsername.setText("");
                                    txtPassword.setText("");

                                    Gson gson = new Gson();
                                    User user = gson.fromJson(r.getJSONObject("data").toString(), User.class);
                                    CredentialsPrefs.saveCredentials(username, password, user);
                                    Pushbots.sharedInstance().setAlias(user.get_id());
                                    if (user.getAdmin()) {
                                        //admin login
                                    } else {
                                        //normal user login
                                    }
                                    finish();
                                } else {
                                    //login fail
                                    displayToast("Đăng nhập thất bại!");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject r) {
                            //login fail
                            displayToast("Đăng nhập thất bại!");
                        }
                    });
                }
            });
        }

        if (btnRegister != null) {
            btnRegister.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
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
