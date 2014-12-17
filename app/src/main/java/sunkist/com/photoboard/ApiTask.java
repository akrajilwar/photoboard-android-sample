package sunkist.com.photoboard;

import android.os.AsyncTask;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;


class ApiTask extends AsyncTask<String, Void, Object> {
    OkHttpClient client = new OkHttpClient();

    @Override
    protected Object doInBackground(String... urls) {
        String url = urls[0];
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .build();
        Object response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}

