package com.example.robot_server.nfcapp.utils;

import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpUtils {

    private static OkHttpClient httpClient = new OkHttpClient();

    public static void send(Request request) {
        send(request, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d("NFCTAG", "Error sending a request to " + call.request().url().host() + ", connectivity issue");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    Log.v("NFCTAG", "Successfully sent a request to " + call.request().url().host());
                } else {
                    Log.v("NFCTAG", "Error sending a request to " + call.request().url().host());
                }
                Log.v("NFCTAG", response.code() + ": " + response.body().string());
            }
        });
    }

    public static void send(Request request, Callback callback) {
        httpClient.newCall(request).enqueue(callback);
    }

}
