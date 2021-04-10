package com.example.gymbuddy_weighttracker;

import java.io.Serializable;

public class Note implements Serializable {
    float id;
    String note;

    public Note(float id, String note) {
        this.id = id;
        this.note = note;
    }

    public float getId() {
        return id;
    }

    public void setId(float id) {
        this.id = id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
