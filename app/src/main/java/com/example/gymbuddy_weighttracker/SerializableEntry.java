package com.example.gymbuddy_weighttracker;

import java.io.Serializable;

public class SerializableEntry implements Serializable {
    float X;
    float Y;
    String note;

    public SerializableEntry(float x, float y, String note) {
        X = x;
        Y = y;
        this.note = note;
    }

    public float getX() {
        return X;
    }

    public void setX(float x) {
        X = x;
    }

    public float getY() {
        return Y;
    }

    public void setY(float y) {
        Y = y;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
