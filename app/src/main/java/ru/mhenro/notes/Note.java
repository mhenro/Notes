package ru.mhenro.notes;

import java.util.Date;

/**
 * Created by mhenr on 29.10.2016.
 */

public class Note {
    private long id;
    private int img;
    private String header;
    private String note;
    private Date date;
    private boolean isNotified;

    public Note() {
        id = 0;
        img = 0;
        header = "";
        note = "";
        date = new Date();
        isNotified = false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isNotified() {
        return isNotified;
    }

    public void setNotified(boolean notified) {
        isNotified = notified;
    }
}
