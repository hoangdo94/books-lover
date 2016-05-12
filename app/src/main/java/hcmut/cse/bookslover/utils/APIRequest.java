package hcmut.cse.bookslover.utils;
import android.content.Context;

import com.loopj.android.http.*;

import cz.msebera.android.httpclient.HttpEntity;

/**
 * Created by hoangdo on 4/10/16.
 */
public class APIRequest {
    private static final String BASE_URL = "http://api.ws.hoangdo.info/";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.setBasicAuth(CredentialsPrefs.getUsername(), CredentialsPrefs.getPassword());
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.setBasicAuth(CredentialsPrefs.getUsername(), CredentialsPrefs.getPassword());
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(Context context, String url, HttpEntity entity, AsyncHttpResponseHandler responseHandler) {
        client.setBasicAuth(CredentialsPrefs.getUsername(), CredentialsPrefs.getPassword());
        client.post(context, getAbsoluteUrl(url), entity, "application/json", responseHandler);
    }

    public static void put(Context context, String url, HttpEntity entity, AsyncHttpResponseHandler responseHandler) {
        client.setBasicAuth(CredentialsPrefs.getUsername(), CredentialsPrefs.getPassword());
        client.put(context, getAbsoluteUrl(url), entity, "application/json", responseHandler);
    }

    public static void put(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.setBasicAuth(CredentialsPrefs.getUsername(), CredentialsPrefs.getPassword());
        client.put(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void delete(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.setBasicAuth(CredentialsPrefs.getUsername(), CredentialsPrefs.getPassword());
        client.delete(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void authenticate(String username, String password, AsyncHttpResponseHandler responseHandler) {
        client.setBasicAuth(username, password);
        client.post(getAbsoluteUrl("auth"), null, responseHandler);
    }

    public static void upload(RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl("upload"), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}

