package ru.mhenro.notes;

import android.app.LoaderManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {

    public static boolean IS_TEST = false;
    private static final int CM_VIEW_ID = 1;
    private static final int CM_EDIT_ID = 2;
    private static final int CM_DELETE_ID = 3;

    private DB db;
    private SimpleCursorAdapter scAdapter;
    private ListView lvNotes;
    private Toolbar tbMain;
    private Button btnNewNote;
    private AdView adViewMain;

    enum WINDOW_MODE {
        WND_VIEW,
        WND_EDIT,
        WND_CREATE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* initialize the AdMob */
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-4322047714383216~2215842888");
        adViewMain = (AdView) findViewById(R.id.adViewMain);
        AdRequest adRequest = null;
        if (IS_TEST) {
            adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
             //       .addTestDevice("AC98C820A50B4AD8A2106EDE96FB87D4")  // An example device ID
                    .build();
        }
        else {
            adRequest = new AdRequest.Builder().build();
        }
        if (adRequest != null) {
            adViewMain.loadAd(adRequest);
        }

        /* open the db connection */
        db = new DB(this);
        db.open();

        /* creating columns for adapter */
        String[] from = new String[] {DB.COLUMN_IMG, DB.COLUMN_HEADER, DB.COLUMN_DATETIME};
        int[] to = new int[] {R.id.ivIcon, R.id.tvText, R.id.tvDateTime};

        /* creating the adapter and setup the listview */
        scAdapter = new SimpleCursorAdapter(this, R.layout.my_list_item, null, from, to, 0);
        lvNotes = (ListView)findViewById(R.id.lvNotes);
        lvNotes.setAdapter(scAdapter);
        lvNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                openNoteWindow(WINDOW_MODE.WND_VIEW, id);
            }
        });

        /* creating toolber */
        tbMain = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(tbMain);
        getSupportActionBar().setTitle(R.string.app_name);

        /* adding context menu to listview */
        registerForContextMenu(lvNotes);

        /* creating the loader for reading the data */
        getSupportLoaderManager().initLoader(0, null, this);

        /* creating logic for button "new note" */
        btnNewNote = (Button)findViewById(R.id.btnNewNote);
        btnNewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNoteWindow(WINDOW_MODE.WND_CREATE, 0);
            }
        });


        /* creating a thread to check notifications */
        final Handler handler = new Handler();
        final Intent intentService = new Intent(this, NotifyService.class);
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                startService(intentService);
                handler.postDelayed(this, 60000);
            }
        };
        handler.postDelayed(r, 1000);
    }

    public void openNoteWindow(WINDOW_MODE mode, long id) {
        Intent intent = null;

        switch(mode) {
            case WND_CREATE:
                intent = new Intent(this, NoteActivity.class);
                intent.putExtra("mode", "create");
                startActivity(intent);
                break;
            case WND_VIEW:
                intent = new Intent(this, NoteActivity.class);
                intent.putExtra("mode", "view");
                intent.putExtra("id", String.valueOf(id));
                startActivity(intent);
                break;
            case WND_EDIT:
                intent = new Intent(this, NoteActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("id", String.valueOf(id));
                startActivity(intent);
                break;
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CM_VIEW_ID, 0, R.string.view_record);
        menu.add(0, CM_EDIT_ID, 0, R.string.edit_record);
        menu.add(0, CM_DELETE_ID, 0, R.string.delete_record);
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo acmi = null;

        switch(item.getItemId()) {
            case CM_VIEW_ID:
                /* get list item from the context menu */
                acmi = (AdapterView.AdapterContextMenuInfo) item
                        .getMenuInfo();
                openNoteWindow(WINDOW_MODE.WND_VIEW, acmi.id);
                return true;

            case CM_EDIT_ID:
                /* get list item from the context menu */
                acmi = (AdapterView.AdapterContextMenuInfo) item
                        .getMenuInfo();
                openNoteWindow(WINDOW_MODE.WND_EDIT, acmi.id);
                return true;

            case CM_DELETE_ID:
                /* get list item from the context menu */
                acmi = (AdapterView.AdapterContextMenuInfo) item
                        .getMenuInfo();
                /* remove note from db by id*/
                db.delNote(acmi.id);
                /* reload the cursor */
                getSupportLoaderManager().getLoader(0).forceLoad();
                return true;

            default: return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
        Intent intentService = new Intent(this, NotifyService.class);
        stopService(intentService);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bndl) {
        return new MyCursorLoader(this, db);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        scAdapter.swapCursor(cursor);
        getSupportActionBar().setTitle(getApplicationContext().getString(R.string.app_name) + " (" + cursor.getCount() + ")");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    static class MyCursorLoader extends CursorLoader {
        DB db;

        public MyCursorLoader(Context ctx, DB db) {
            super(ctx);
            this.db = db;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = db.getAllData();
            return cursor;
        }
    }
}
