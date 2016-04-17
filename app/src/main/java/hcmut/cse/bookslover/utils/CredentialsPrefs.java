package hcmut.cse.bookslover.utils;

import android.content.SharedPreferences;
import android.provider.Settings;

/**
 * Created by hoangdo on 4/10/16.
 */
public class CredentialsPrefs {
    public static final String PREFS_NAME = "BooksLoverPrefsFile";
    private static SharedPreferences sharedPrefs;
    private static boolean loggedIn = false;
    private static String username = "def";
    private static String password  = "def";

    public static void setPrefs(SharedPreferences prefs) {
        sharedPrefs = prefs;
    }

    public static void fetchSavedCredentials() {
        username = sharedPrefs.getString("username", "def");
        password = sharedPrefs.getString("password", "def");
        if (username != "def" && password != "def") {
            loggedIn = true;
        } else {
            loggedIn = false;
        }
    }

    public static void setCredentials(String u, String p) {
        username = u;
        password = p;

        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.commit();

        loggedIn = true;
    }

    public static void clearCredentials() {
        username = "def";
        password = "def";

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

    public static boolean isLoggedIn() { return loggedIn; }

}

