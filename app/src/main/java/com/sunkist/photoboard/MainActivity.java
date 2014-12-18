package com.sunkist.photoboard;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class MainActivity extends ActionBarActivity {

    final static String HOST = "http://conductive-set-796.appspot.com";
    final static int TAKE_CAMERA = 1;
    final static int TAKE_GALLERY = 2;

    ProgressDialog pendingDialog = null;
    PhotoArrayAdapter photoArrayAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pendingDialog = new ProgressDialog(this);
        pendingDialog.setIndeterminate(true);

        GridView gridView = (GridView) findViewById(R.id.gridView);
        photoArrayAdapter = new PhotoArrayAdapter(this);
        gridView.setAdapter(photoArrayAdapter);

        load();
    }

    void load() {
        new LoadTask().execute(HOST);
    }

    class LoadTask extends ApiTask {
        @Override
        protected void onPreExecute() {
            pendingDialog.show();
        }

        @Override
        protected Object doInBackground(String... urls) {
            Object result = super.doInBackground(urls);
            ArrayList<Photo> photoArrayList = new ArrayList<Photo>();
            if ( result != null ) {
                Response response = (Response) result;
                String json = null;
                if ( response != null && response.code() == 200) {
                    try {
                        json = response.body().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if ( json != null ) {
                    Gson gson = new Gson();

                    JsonParser jsonParser = new JsonParser();
                    JsonElement jsonElement = jsonParser.parse(json);
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    JsonArray jsonArray = jsonObject.get("photos").getAsJsonArray();
                    for(int i=0; i<jsonArray.size(); i++) {
                        Photo photo = gson.fromJson(jsonArray.get(i), Photo.class);
                        photoArrayList.add(photo);
                    }
                }
            }
            return photoArrayList;
        }

        @Override
        protected void onPostExecute(Object result) {
            photoArrayAdapter.clear();
            photoArrayAdapter.addAll((ArrayList<Photo>) result);
            pendingDialog.hide();
        }
    }

    class UploadTask extends ApiTask {
        byte[] fileData;

        public UploadTask(byte[] fileData) {
            this.fileData = fileData;
        }

        @Override
        protected void onPreExecute() {
            pendingDialog.show();
        }

        @Override
        public Request.Builder getBuilder(String url) {
            MediaType mediaType = MediaType.parse("image/jpeg");

            RequestBody requestBody =  new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("file", "file.jpg", RequestBody.create(mediaType, fileData))
                    .build();

            Request.Builder builder = super.getBuilder(url);
            return builder.post(requestBody);
        }

        @Override
        protected void onPostExecute(Object result) {
            pendingDialog.hide();

            int statusCode = 0;
            if (result != null) {
                Response response = (Response) result;
                statusCode = response.code();
                if (statusCode == 200) {
                    toast("업로드 성공");
                    load();
                    return;
                }
            }
            toast(String.format("업로드 실패 (%d)", statusCode));
            return;
        }
    }

    class PhotoArrayAdapter extends ArrayAdapter<Photo> {
        public PhotoArrayAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if ( convertView == null ) {
                convertView = getLayoutInflater().inflate(R.layout.row_photo, parent, false);
            }

            ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
            TextView textView = (TextView) convertView.findViewById(R.id.textView);

            Photo photo = getItem(position);

            Glide.with(convertView.getContext())
                    .load(HOST + photo.image_url)
                    .animate(R.anim.abc_fade_in)
                    .into(imageView);

            textView.setText(getDateString(Long.parseLong(photo.created_at) * 1000L));

            return convertView;
        }

        String getDateString(long timestamp) {
            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
            calendar.setTimeInMillis(timestamp);
            return DateFormat.format("yyyy-MM-dd", calendar).toString();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if ( id == R.id.action_upload ) {
            Intent intent = new Intent();
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, TAKE_CAMERA);
            return true;
        }
        else if ( id == R.id.action_refresh ) {
            load();
            return true;
        }
        /*
        else if ( id == R.id.action_settings ) {
            toast("TODO: 설정");
            return true;
        }
        */

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( resultCode == RESULT_OK ) {
            if ( requestCode == TAKE_CAMERA ) {
                Uri imageUri = data.getData();
                String path = getRealPathFromUri(imageUri);

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(path, options);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream) ;
                byte[] bitmapData = stream.toByteArray();

                toast(String.format("load %d bytes", bitmapData.length));

                new UploadTask(bitmapData).execute(HOST + "/new");
            }
        }
    }

    String getRealPathFromUri(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(columnIndex);
    }

    void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
