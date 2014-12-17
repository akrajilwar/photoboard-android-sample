package sunkist.com.photoboard;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    final static String HOST = "http://conductive-set-796.appspot.com";
    PhotoArrayAdapter photoArrayAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

            textView.setText(photo.created_at);

            return convertView;
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

        if ( id == R.id.action_refresh ) {
            load();
        }
        else if ( id == R.id.action_settings ) {
            toast("TODO: 설정");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
