package ru.mhenro.notes;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mhenr on 30.10.2016.
 */

public class NotifyService extends IntentService {
    private DB db;

    public NotifyService() {
        super("notifyService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        init();
    }

    @Override
    public void onDestroy() {
        db.close();
        super.onDestroy();
    }

    private void init() {
         /* open the db connection */
        db = new DB(this);
        db.open();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Log.v("service-->", "working!");

        Cursor cursor = db.getAllData();
        if (cursor.moveToFirst()) {
            //Log.v("service-->", "have data!");
            do {
                Note note = new Note();
                note.setId(cursor.getLong(cursor.getColumnIndex(DB.COLUMN_ID)));
                note.setImg(cursor.getInt(cursor.getColumnIndex(DB.COLUMN_IMG)));
                note.setHeader(cursor.getString(cursor.getColumnIndex(DB.COLUMN_HEADER)));
                note.setNote(cursor.getString(cursor.getColumnIndex(DB.COLUMN_NOTE)));
                note.setNotified(cursor.getInt(cursor.getColumnIndex(DB.COLUMN_ISNOTIFIED)) == 1 ? true : false);

                String tmp = cursor.getString(cursor.getColumnIndex(DB.COLUMN_DATETIME));
                SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                try {
                    Date date = format.parse(tmp);
                    note.setDate(date);
                } catch(java.text.ParseException e) {
                    note.setDate(null);
                }

                /* creating notification if needed */
                Date today = new Date();
                if ((today.after(note.getDate()) || format.format(today).equals(format.format(note.getDate())))
                        && !note.isNotified()
                        && note.getImg() == R.drawable.dr_notify) {
                    //Log.v("service-->", "create notification!");
                    createNotification(String.valueOf(note.getId()), note.getHeader(), note.getNote());
                }
            } while (cursor.moveToNext());
        }
    }

    /* function to create user's notification */
    public void createNotification(String noteId, String noteHeader, String noteText) {
        /* first of all, check if we've already notified the user */
        Note note = db.getNote(Long.valueOf(noteId));
        if (note.isNotified()) {
            return;
        }

        android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.dr_notify)
                .setContentTitle(noteHeader)
                .setContentText(noteText);

        Intent resultIntent = new Intent(this, NoteActivity.class);
        resultIntent.putExtra("id", noteId);
        resultIntent.putExtra("mode", "view");
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(NoteActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int id = 0;
        notificationManager.notify(id, builder.build());

        /* update db that we have already notified the user */
        db.setNoteNotify(Long.valueOf(noteId), true);
    }
}
