package ru.mhenro.notes;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class NoteActivity extends AppCompatActivity /*implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>*/{

    private final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
    private DB db;
    private EditText etNote;
    private EditText etHeader;
    private TextView tvHeader;
    private CheckBox chkNotify;
    private DatePicker dpDateNotify;
    private Toolbar tbEdit;
    private AdView adViewNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        adViewNote = (AdView) findViewById(R.id.adViewNote);
        AdRequest adRequest = null;
        if (MainActivity.IS_TEST) {
            adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
              //      .addTestDevice("014E280D10015012")  // An example device ID
                    .build();
        }
        else {
            adRequest = new AdRequest.Builder().build();
        }
        if (adRequest != null) {
            adViewNote.loadAd(adRequest);
        }

        /* open the db connection */
        db = new DB(this);
        db.open();

        /* creating toolbar */
        tbEdit = (Toolbar) findViewById(R.id.tbEdit);
        setSupportActionBar(tbEdit);

        etHeader = (EditText)findViewById(R.id.etHeader);
        tvHeader = (TextView)findViewById(R.id.tvHeader);

        chkNotify = (CheckBox)findViewById(R.id.chkNotify);
        dpDateNotify = (DatePicker)findViewById(R.id.dpDateNotify);

        etNote = (EditText)findViewById(R.id.etNote);
        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        Note note = new Note();
        if (id != null) {
            note = db.getNote(Integer.valueOf(id));
        }
        getSupportActionBar().setTitle(note.getHeader());
        String mode = getIntent().getStringExtra("mode");

        final SimpleDateFormat fYear = new SimpleDateFormat("yyyy");
        final SimpleDateFormat fMonth = new SimpleDateFormat("MM");
        final SimpleDateFormat fDay = new SimpleDateFormat("dd");

        switch(mode) {
            case "create":
                etNote.setText("");
                etHeader.setText("");
                chkNotify.setChecked(false);
                Calendar today = Calendar.getInstance();
                dpDateNotify.init(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH), null);
                break;

            case "view":
                etNote.setText(note.getNote());
                etNote.setKeyListener(null);
                etHeader.setText(note.getHeader());
                etHeader.setKeyListener(null);
                chkNotify.setChecked(note.getImg() == R.drawable.dr_notify ? true : false);
                dpDateNotify.init(Integer.valueOf(fYear.format(note.getDate())), Integer.valueOf(fMonth.format(note.getDate()))-1, Integer.valueOf(fDay.format(note.getDate())), null);
                dpDateNotify.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
                chkNotify.setEnabled(false);
                break;

            case "edit":
                etNote.setText(note.getNote());
                etHeader.setText(note.getHeader());
                chkNotify.setChecked(note.getImg() == R.drawable.dr_notify ? true : false);
                dpDateNotify.init(Integer.valueOf(fYear.format(note.getDate())), Integer.valueOf(fMonth.format(note.getDate()))-1, Integer.valueOf(fDay.format(note.getDate())), null);
                break;
        }
    }

    public void openMainWindow(/*MainActivity.WINDOW_MODE mode*/) {
        Intent intent = null;

        intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void openNoteWindow() {
        Intent intent = new Intent(this, NoteActivity.class);
        intent.putExtra("mode", "edit");
        intent.putExtra("id", getIntent().getStringExtra("id"));
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        String mode = getIntent().getStringExtra("mode");
        menu.findItem(R.id.action_done).setVisible("view".equals(mode) ? false : true);
        menu.findItem(R.id.action_edit).setVisible("view".equals(mode) ? true : false);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String mode = getIntent().getStringExtra("mode");

        switch (item.getItemId()) {
            case R.id.action_edit:
                openNoteWindow();
                return true;

            case R.id.action_done:
                if ("create".equals(mode)) {
                    Note note = new Note();
                    note.setImg(R.drawable.dr_note);
                    note.setHeader(etHeader.getText().toString());
                    note.setNote(etNote.getText().toString());
                    if (chkNotify.isChecked()) {
                        try {
                            note.setDate(format.parse(dpDateNotify.getDayOfMonth() + "." + String.valueOf(dpDateNotify.getMonth() + 1) + "." + dpDateNotify.getYear()));
                        } catch (ParseException e) {
                            chkNotify.setChecked(false);
                            note.setDate(new Date());
                        }
                    }
                    db.addNote(chkNotify.isChecked() ? R.drawable.dr_notify : R.drawable.dr_note, note.getHeader(), note.getNote(), format.format(note.getDate()));
                }
                else if ("edit".equals(mode)) {
                    String id = getIntent().getStringExtra("id");
                    Note note = db.getNote(Integer.valueOf(id));
                    note.setHeader(etHeader.getText().toString());
                    note.setNote(etNote.getText().toString());
                    note.setDate(new Date());
                    if (chkNotify.isChecked()) {
                        try {
                            note.setDate(format.parse(dpDateNotify.getDayOfMonth() + "." + String.valueOf(dpDateNotify.getMonth() + 1) + "." + dpDateNotify.getYear()));
                        } catch (ParseException e) {
                            chkNotify.setChecked(false);
                            note.setDate(new Date());
                        }
                    }
                    db.editNote(note.getId(), chkNotify.isChecked() ? R.drawable.dr_notify : R.drawable.dr_note, note.getHeader(), note.getNote(), format.format(note.getDate()));
                }
                openMainWindow();

                return true;

            case R.id.action_exit:
                openMainWindow();

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
/*
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new NoteActivity.MyCursorLoader(this, db);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
    */
}
