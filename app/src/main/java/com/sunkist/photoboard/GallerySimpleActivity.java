package com.sunkist.photoboard;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Calendar;
import java.util.Locale;


// http://wptrafficanalyzer.in/blog/loading-listview-with-sdcard-thumbnail-images-and-displaying-its-title-size-width-and-height-by-merging-cursors-using-matrixcursor/

public class GallerySimpleActivity extends ActionBarActivity {
    SimpleCursorAdapter adapter;
    MatrixCursor matrixCursor;
    Cursor thumbCursor;
    Cursor imageCursor;
    String thumbImageId = "";
    String thumbImageData = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        GridView gridView = (GridView) findViewById(R.id.gridView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getAdapter().getItem(position);
                Photo photo = new Photo(cursor);

                Intent intent = getIntent();
                intent.setData(photo.uri);
                setResult(RESULT_OK, intent);

                finish();
            }
        });

        Cursor cursor = getContentResolver().query(Images.Media.EXTERNAL_CONTENT_URI,
                null, null, null, null);
        GalleryAdapter adapter = new GalleryAdapter(this, cursor);
        gridView.setAdapter(adapter);
    }

    class Photo {
        public Uri uri = null;
        public String dateAdded = null;

        public Photo(Cursor cursor) {
            long id = cursor.getLong(cursor.getColumnIndex(Images.Media._ID));
            this.uri = ContentUris.withAppendedId(Images.Media.EXTERNAL_CONTENT_URI, id);
            this.dateAdded = getDateString(cursor.getLong(cursor.getColumnIndex(Images.Media.DATE_TAKEN)));
        }

        String getDateString(long timestamp) {
            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
            calendar.setTimeInMillis(timestamp);
            return DateFormat.format("yyyy-MM-dd", calendar).toString();
        }
    }

    class GalleryAdapter extends CursorAdapter {
        public GalleryAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return getLayoutInflater().inflate(R.layout.row_gallery_photo, null);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
            TextView textView = (TextView) view.findViewById(R.id.textView);

            Photo photo = new Photo(cursor);
            Glide.with(context).load(photo.uri).crossFade().into(imageView);
            textView.setText(photo.dateAdded);
        }
    }
}

