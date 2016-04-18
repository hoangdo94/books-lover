package hcmut.cse.bookslover;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import hcmut.cse.bookslover.models.User;
import hcmut.cse.bookslover.utils.APIRequest;
import hcmut.cse.bookslover.utils.CredentialsPrefs;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        CredentialsPrefs.setPrefs(getSharedPreferences(CredentialsPrefs.PREFS_NAME, 0));
        CredentialsPrefs.fetchSavedCredentials();
        final String username = CredentialsPrefs.getUsername();
        final String password = CredentialsPrefs.getPassword();

        if (!username.isEmpty() && !password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Đang xác thực thông tin đăng nhập...", Toast.LENGTH_SHORT).show();
            // Authenticate
            APIRequest.authenticate(username, password, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject r) {
                    try {
                        int status = r.getInt("status");
                        if (status == 1) {
                            Gson gson = new Gson();
                            User user = gson.fromJson(r.getJSONObject("data").toString(), User.class);
                            CredentialsPrefs.saveCredentials(username, password, user);
                        }
                    } catch (JSONException e) {

                    }
                    startMainActivity();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject r) {
                    startMainActivity();
                }
            });
        } else {
            startMainActivity();
        }
    }

    void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
