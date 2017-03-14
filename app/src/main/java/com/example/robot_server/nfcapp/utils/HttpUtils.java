package com.example.robot_server.nfcapp.utils;

import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by robot-server on 14.03.17.
 */

public class HttpUtils {

    private static OkHttpClient httpClient = new OkHttpClient();

    public static void sendPost(Request request) {
        sendPost(request, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d("NFCTAG", "Error posting scan result to " + call.request().url().host() + ", connectivity issue");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    Log.v("NFCTAG", "Successfully posted scan result to " + call.request().url().host());
                } else {
                    Log.v("NFCTAG", "Error posting scan result to " + call.request().url().host());
                }
                Log.v("NFCTAG", response.code() + ": " + response.body().string());
            }
        });
    }

    public static void sendPost(Request request, Callback callback) {
        httpClient.newCall(request).enqueue(callback);
    }

}
