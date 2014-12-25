package com.sunkist.photoboard;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
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
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Calendar;
import java.util.Locale;


// http://wptrafficanalyzer.in/blog/loading-listview-with-sdcard-thumbnail-images-and-displaying-its-title-size-width-and-height-by-merging-cursors-using-matrixcursor/

public class GalleryLoaderManagerActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    final static int LOADER_ID = 0;

    CursorAdapter adapter;
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


        matrixCursor = new MatrixCursor(new String[] { "_id", "_data", "_details" });
        adapter = new CursorAdapter(this, null,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER) {

            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                View view = getLayoutInflater().inflate(R.layout.row_gallery_photo, parent, false);
                // view.setTag(viewHolder);
                return view;
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                // viewHolder = view.getTag();

                ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
                TextView textView = (TextView) view.findViewById(R.id.textView);

                Photo photo = new Photo(cursor);
                Glide.with(context).load(photo.uri).crossFade().into(imageView);
                textView.setText(photo.dateAdded);
            }
        };
        gridView.setAdapter(adapter);

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle bundle) {
        return new CursorLoader(this, Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
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
}

