package com.sunkist.photoboard;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


public class ContactsActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    final static int LOADER_ID = 0;
    SimpleCursorAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1,
                null, new String[] { ContactsContract.Contacts.DISPLAY_NAME },
                new int[] { android.R.id.text1},

                // api 11 에서 deprecated
                // http://developer.android.com/reference/android/widget/CursorAdapter.html#FLAG_AUTO_REQUERY
                // UI Thread 성능이슈로 인해 ANR 이 발생할 수도 있기 때문.
                // ??? : CursorLoad 와 CursorAdapter 를 같이 쓰는 경우 중복으로 리쿼리된다 ???
                // CursorAdapter.FLAG_AUTO_REQUERY |

                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        getLoaderManager().initLoader(LOADER_ID, null, this); // onCreateLoader 를 호출
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        // Bundle은 Loader에 대한 추가적인 argument들이지만, CursorLoader에는 사용되지 않는다.
        return new CursorLoader(this, ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursor.moveToFirst();
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
        // 	Loader 에서 cursor 를 close 해준다.
    }
}
