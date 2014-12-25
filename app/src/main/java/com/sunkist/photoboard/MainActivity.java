package com.sunkist.photoboard;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.model.GraphObject; // Import OK.
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends ActionBarActivity {

    final static String HOST = "http://conductive-set-796.appspot.com";
    final static int TAKE_CAMERA = 1;
    final static int TAKE_GALLERY = 2;
    final static int TAKE_CUSTOM_GALLERY = 3;
    final static int TAKE_CUSTOM_LOADER_MANAGER_GALLERY = 4;

    GridView gridView = null;
    ProgressDialog pendingDialog = null;
    PhotoArrayAdapter photoArrayAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pendingDialog = new ProgressDialog(this);
        pendingDialog.setIndeterminate(true);

        gridView = (GridView) findViewById(R.id.gridView);
        photoArrayAdapter = new PhotoArrayAdapter(this);
        gridView.setAdapter(photoArrayAdapter);

        final Context context = this;

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Photo photo = (Photo) parent.getAdapter().getItem(position);
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("imageUrl", HOST + photo.image_url);
                startActivity(intent);
            }
        });

        onConfigurationChanged(Resources.getSystem().getConfiguration());

        load();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        float scalefactor = getResources().getDisplayMetrics().density * 120;
        int number = getWindowManager().getDefaultDisplay().getWidth();
        int numColumns = (int) ((float) number / (float) scalefactor);
        gridView.setNumColumns(numColumns);
        Toast.makeText(this, "numColumns : " + numColumns, Toast.LENGTH_SHORT).show();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
            // TextView textView = (TextView) convertView.findViewById(R.id.textView);

            Photo photo = getItem(position);

            Glide.with(convertView.getContext())
                    .load(HOST + photo.image_url)
                    .animate(R.anim.abc_fade_in)
                    .into(imageView);

            // textView.setText(getDateString(Long.parseLong(photo.created_at) * 1000L));

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
            String[] rows = { "사진촬영", "앨범", "커스텀 갤러리", "커스텀 갤러리 (Loader Manager)" };
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, rows);
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setAdapter(adapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if ( which == 0 ) {
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(intent, TAKE_CAMERA);
                            }
                            else if ( which == 1 ) {
                                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(intent, TAKE_GALLERY);
                            }
                            else if ( which == 2 ) {
                                Intent intent = new Intent(MainActivity.this, GallerySimpleActivity.class);
                                startActivityForResult(intent, TAKE_CUSTOM_GALLERY);
                            }
                            else {
                                Intent intent = new Intent(MainActivity.this, GalleryLoaderManagerActivity.class);
                                startActivityForResult(intent, TAKE_CUSTOM_LOADER_MANAGER_GALLERY);
                            }
                        }
                    })
                    .create();
            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.show();

            return true;
        }
        else if ( id == R.id.action_refresh ) {
            load();
            return true;
        }
        else if ( id == R.id.action_contacts ) {
            Intent intent = new Intent(this, ContactsActivity.class);
            startActivity(intent);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if ( resultCode == RESULT_OK ) {
            if ( requestCode == TAKE_CAMERA || requestCode == TAKE_GALLERY ||
                    requestCode == TAKE_CUSTOM_GALLERY || requestCode == TAKE_CUSTOM_LOADER_MANAGER_GALLERY ) {
                Uri imageUri = intent.getData();
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
