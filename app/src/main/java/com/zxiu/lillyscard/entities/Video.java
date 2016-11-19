package com.zxiu.lillyscard.entities;

import android.util.Log;

import com.google.firebase.database.Exclude;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Xiu on 11/3/2016.
 */

public class Video {
    static String TAG = "Video";
    static String DASH_REGEX = "dashmpd=([^&]*)";
    static Pattern DASH_PATTERN = Pattern.compile(DASH_REGEX);

    private final OkHttpClient client = new OkHttpClient();

    public String id, title, language;

    @Exclude
    public String dash;

    @Exclude
    public String getThumbnails() {
        return id == null ? null : "https://img.youtube.com/vi/" + id + "/0.jpg";
    }

    @Exclude
    public String getUrl() {
        return id == null ? null : "https://www.youtube.com/watch?v=" + id;
    }

    @Exclude
    public String getManifestUrl() {
        return id == null ? null : " https://www.youtube.com/get_video_info?&video_id=" + id;
    }

    @Exclude
    public void fetchDashUrl() {
        Request request = new Request.Builder()
                .url(getManifestUrl())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, "error = " + e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Headers responseHeaders = response.headers();
                for (int i = 0, size = responseHeaders.size(); i < size; i++) {
//                    System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                }

                dash = response.body().string();
//                final int chunkSize = 500;
//                for (int i = 0; i < dash.length(); i += chunkSize) {
//                    Log.i(TAG, dash.substring(i, Math.min(dash.length(), i + chunkSize)));
//                }

                dash = URLDecoder.decode(dash, "UTF-8");
                dash = URLDecoder.decode(dash, "UTF-8");
                dash = URLDecoder.decode(dash, "UTF-8");
                Matcher matcher = DASH_PATTERN.matcher(dash);
                if (matcher.find()) {
                    dash = matcher.group(1);
                } else {
                    dash = null;
                }
                Log.w(TAG, "getUrl()=" + getUrl() + " getManifestUrl()=" + getManifestUrl());

            }
        });
    }

    @Override
    public String toString() {
        return "Video{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
