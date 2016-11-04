package ru.mhenro.notes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mhenr on 26.10.2016.
 */

public class DB {
    private  static final String DB_NAME = "DB_NOTES";
    private static final int DB_VERSION = 8;
    private static final String TBL_NOTES = "TBL_NOTES";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_IMG = "img";
    public static final String COLUMN_HEADER = "header";
    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_DATETIME = "datetime";
    public static final String COLUMN_ISNOTIFIED = "notified";

    private static final String DB_CREATE =
            "CREATE TABLE " + TBL_NOTES + "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_IMG + " integer, " +
            COLUMN_HEADER + " text, " +
            COLUMN_NOTE + " text, " +
            COLUMN_DATETIME + " text, " +
            COLUMN_ISNOTIFIED + " integer" +
            ");";

    private final Context ctx;

    private DBHelper dbHelper;
    private SQLiteDatabase db;

    /* constructor */
    public DB(Context ctx) {
        this.ctx = ctx;
    }

    /* open connection */
    public void open() {
        dbHelper = new DBHelper(ctx, DB_NAME, null, DB_VERSION);
        if (dbHelper == null) Log.v("service-->", "blya!");
        db = dbHelper.getWritableDatabase();
    }

    /* close connections */
    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    /* get all notes from the db */
    public Cursor getAllData() {
        return db.query(TBL_NOTES, null, null, null, null, null, null);
    }

    /* getting note text by id */
    public Note getNote(long id) {
        Cursor cursor = null;
        Note note = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TBL_NOTES + " WHERE _id = ?", new String[] {String.valueOf(id)});
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                note = new Note();
                note.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                note.setImg(cursor.getInt(cursor.getColumnIndex(COLUMN_IMG)));
                note.setHeader(cursor.getString(cursor.getColumnIndex(COLUMN_HEADER)));
                note.setNote(cursor.getString(cursor.getColumnIndex(COLUMN_NOTE)));
                note.setNotified(cursor.getInt(cursor.getColumnIndex(COLUMN_ISNOTIFIED)) == 1 ? true : false);

                String tmp = cursor.getString(cursor.getColumnIndex(COLUMN_DATETIME));
                SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                try {
                    Date date = format.parse(tmp);
                    note.setDate(date);
                } catch(java.text.ParseException e) {
                    note.setDate(null);
                }
            }
            return note;
        }
        finally {
            if (cursor != null) cursor.close();
        }
    }

    /* adding new note into the db */
    public void addNote(int img, String header, String note, String date) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_IMG, img);
        cv.put(COLUMN_HEADER, header);
        cv.put(COLUMN_NOTE, note);
        cv.put(COLUMN_DATETIME, date);
        cv.put(COLUMN_ISNOTIFIED, 0);
        db.insert(TBL_NOTES, null, cv);
    }

    /* editing note */
    public void editNote(long id, int img, String header, String note, String date) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_IMG, img);
        cv.put(COLUMN_HEADER, header);
        cv.put(COLUMN_NOTE, note);
        cv.put(COLUMN_DATETIME, date);
        cv.put(COLUMN_ISNOTIFIED, 0);
        db.update(TBL_NOTES, cv, COLUMN_ID + " = " + id, null);
    }

    public void setNoteNotify(long id, boolean isNotified) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ISNOTIFIED, isNotified ? 1 : 0);
        db.update(TBL_NOTES, cv, COLUMN_ID + " = " + id, null);
    }

    /* deleting note from the db */
    public void delNote(long id) {
        db.delete(TBL_NOTES, COLUMN_ID + " = " + id, null);
    }

    /* helper class for managing database */
    private class DBHelper extends SQLiteOpenHelper {
        /* constructor */
        public DBHelper(Context ctx, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(ctx, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE);

            /* TESTING!!!
            ContentValues cv = new ContentValues();
            for (int i = 1; i < 20; i++) {
                cv.put(COLUMN_IMG, R.drawable.dr_note);
                cv.put(COLUMN_HEADER, "Запись " + i);
                cv.put(COLUMN_NOTE, "SFSLDFLKSDNFLKSDNFKLNSDF\nJKSDNGJSDNJ oFIO OI OSE OISE ISEO IS \n\nAlex");
                cv.put(COLUMN_DATETIME, "01.01.201" + i);

                db.insert(TBL_NOTES, null, cv);
            }
            */
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            /* ugly hack! */
            if (newVersion != oldVersion) {
                db.execSQL("DROP TABLE IF EXISTS " + TBL_NOTES);
                onCreate(db);
            }


            //TODO: Add functionality for upgrading db
        }
    }
}
