package hcmut.cse.bookslover.utils;

import android.content.SharedPreferences;
import android.provider.Settings;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import hcmut.cse.bookslover.models.User;

/**
 * Created by hoangdo on 4/10/16.
 */
public class CredentialsPrefs {
    public static final String PREFS_NAME = "BooksLoverPrefsFile";
    private static SharedPreferences sharedPrefs;
    private static boolean loggedIn = false;
    private static String username;
    private static String password;
    private static User user;

    public static void setPrefs(SharedPreferences prefs) {
        sharedPrefs = prefs;
    }

    public static void fetchSavedCredentials() {
        username = sharedPrefs.getString("username", "");
        password = sharedPrefs.getString("password", "");
    }

    public static void saveCredentials(String u, String p, User us) {
        username = u;
        password = p;
        user = us;

        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.commit();

        loggedIn = true;
    }

    public static void clearCredentials() {
        username = "";
        password = "";

        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.commit();

        loggedIn = false;
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }

    public static User getCurrentUser() {
        return user;
    }

    public static boolean isLoggedIn() {
        return loggedIn;
    }

}

