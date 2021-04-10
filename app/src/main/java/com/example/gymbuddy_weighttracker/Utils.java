package com.example.gymbuddy_weighttracker;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.mikephil.charting.data.Entry;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class Utils {

    private static Utils instance;
    private SharedPreferences sharedPreferences;
    private static final String ALL_ENTRIES_KEY = "all entries";
    public static final String ALL_NOTES_KEY = "all notes";
    public static final String REF_TIME_KEY = "ref time";
    public static final String GOAL_KEY = "goal";
    public static final String SETTING1_KEY = "setting1";
    public static final String SETTING2_KEY = "setting2";
    public static final String SETTING3_KEY = "setting3";
    public static final String STARTING_WEIGHT="starting weight";
    public static final String STARTING_DATE="starting date";
    public static final String UNIT_KEY="unit";
    public static final String MAX_KEY="max";
    public static final String MIN_KEY="min";

    static Context context;
    static float refTime;
    static float goal;
    static float startingWeight;
    static float startingDate;
    static float max;
    static float min;
    static boolean setting1;
    static boolean setting2;
    static boolean setting3;
    static String unit;

    public Utils(Context context) {
        sharedPreferences = context.getSharedPreferences("weight_db", Context.MODE_PRIVATE);

        if (null == getAllEntries()) {
            initData();
            max=getMax();
            min=getMin();
            refTime = getRefTime();
            goal = getGoal();
            setting1 = getSetting1();
            setting2 = getSetting2();
            setting3=getSetting3();
            startingDate=getStartingDate();
            startingWeight=getStartingDate();
            unit=getUnit();

        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

    }

    public String getUnit() {
        String unit = sharedPreferences.getString(UNIT_KEY, " kg");
        return unit;
    }

    public boolean setUnit(String unit) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(UNIT_KEY, unit);
        editor.commit();
        return true;
    }

    public long getRefTime() {
        long refTime = sharedPreferences.getLong(REF_TIME_KEY, 0);
        return refTime;
    }

    public boolean setRefTime(long refTime) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(REF_TIME_KEY, refTime);
        editor.commit();
        return true;
    }

    public float getStartingWeight() {
        float startingWeight = sharedPreferences.getFloat(STARTING_WEIGHT, 0);
        return startingWeight;
    }

    public boolean setStartingWeight(float startingWeight) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(STARTING_WEIGHT, startingWeight);
        editor.commit();
        return true;
    }

    public float getMax() {
        float max = sharedPreferences.getFloat(MAX_KEY, 0);
        return max;
    }

    public boolean setMax(float max) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(MAX_KEY, max);
        editor.commit();
        return true;
    }

    public float getMin() {
        float min = sharedPreferences.getFloat(MIN_KEY, 0);
        return min;
    }

    public boolean setMin(float min) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(MIN_KEY, min);
        editor.commit();
        return true;
    }

    public float getStartingDate() {
        float startingDate = sharedPreferences.getFloat(STARTING_DATE, 0);
        return startingDate;
    }

    public boolean setStartingDate(float startingDate) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(STARTING_DATE, startingDate);
        editor.commit();
        return true;
    }

    public float getGoal() {
        float goal = sharedPreferences.getFloat(GOAL_KEY, 0);
        return goal;
    }

    public boolean setGoal(float goal) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(GOAL_KEY, goal);
        editor.commit();
        return true;
    }

    public boolean getSetting1() {
        boolean setting1 = sharedPreferences.getBoolean(SETTING1_KEY, false);
        return setting1;
    }

    public boolean setSetting1(boolean setting1) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SETTING1_KEY, setting1);
        editor.commit();
        return true;
    }

    public boolean getSetting2() {
        boolean setting2 = sharedPreferences.getBoolean(SETTING2_KEY, false);
        return setting2;
    }

    public boolean setSetting2(boolean setting2) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SETTING2_KEY, setting2);
        editor.commit();
        return true;
    }

    public boolean getSetting3() {
        boolean setting3 = sharedPreferences.getBoolean(SETTING3_KEY, false);
        return setting3;
    }

    public boolean setSetting3(boolean setting3) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SETTING3_KEY, setting2);
        editor.commit();
        return true;
    }

    private void initData() {
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> notes = new ArrayList<>();
//
//        entries.add(new Entry(0,60f));
//        notes.add(null);
//        entries.add(new Entry(1,55f));
//        notes.add("dupa");
//        entries.add(new Entry(2,40f));
//        notes.add(null);
//        entries.add(new Entry(3,32f));
//        notes.add("pupa");
//        entries.add(new Entry(4,12f));
//        notes.add(null);
//        entries.add(new Entry(5,55f));
//        notes.add("oko");


        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        editor.putString(ALL_NOTES_KEY, gson.toJson(notes));
        editor.putString(ALL_ENTRIES_KEY, gson.toJson(entries));
        editor.commit();
    }

    public static Utils getInstance(Context context) {
        if (null == instance) {
            instance = new Utils(context);
        }
        return instance;
    }

    public ArrayList<Entry> getAllEntries() {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Entry>>() {
        }.getType();
        ArrayList<Entry> entries = gson.fromJson(sharedPreferences.getString(ALL_ENTRIES_KEY, null), type);
        return entries;
    }

    public ArrayList<Note> getAllNotes() {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Note>>() {
        }.getType();
        ArrayList<Note> notes = gson.fromJson(sharedPreferences.getString(ALL_NOTES_KEY, null), type);
        return notes;
    }

    public boolean updateEntries(ArrayList<Entry> entries) {
        if (null != entries) {
            Gson gson = new Gson();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(ALL_ENTRIES_KEY);
            editor.putString(ALL_ENTRIES_KEY, gson.toJson(entries));
            editor.commit();
        }
        return false;
    }

    public boolean updateNotes(ArrayList<Note> notes) {
        if (null != notes) {
            Gson gson = new Gson();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(ALL_NOTES_KEY);
            editor.putString(ALL_NOTES_KEY, gson.toJson(notes));
            editor.commit();
        }
        return false;
    }

    public boolean addToAllEntries(Entry entry) {
        ArrayList<Entry> entries = getAllEntries();
        if (null != entries) {
            if (entries.add(entry)) {
                Gson gson = new Gson();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove(ALL_ENTRIES_KEY);
                editor.putString(ALL_ENTRIES_KEY, gson.toJson(entries));
                editor.commit();
                return true;
            }
        }
        return false;
    }

    public boolean addToAllNotes(Note note) {
        ArrayList<Note> notes = getAllNotes();
        if (null != notes) {
            if (notes.add(note)) {
                Gson gson = new Gson();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove(ALL_NOTES_KEY);
                editor.putString(ALL_NOTES_KEY, gson.toJson(notes));
                editor.commit();
                return true;
            }
        }
        return false;
    }

    public boolean addToAllEntriesOrdered(Entry entry, int position) {
        ArrayList<Entry> entries = getAllEntries();
        if (null != entries) {
            entries.add(position, entry);
            Gson gson = new Gson();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(ALL_ENTRIES_KEY);
            editor.putString(ALL_ENTRIES_KEY, gson.toJson(entries));
            editor.commit();
            return true;
        }
        return false;
    }

    public boolean addToAllNotesOrdered(Note note, int position) {
        ArrayList<Note> notes = getAllNotes();
        if (null != notes) {
            notes.add(position, note);
            Gson gson = new Gson();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(ALL_NOTES_KEY);
            editor.putString(ALL_NOTES_KEY, gson.toJson(notes));
            editor.commit();
            return true;
        }
        return false;
    }

    public boolean removeFromAllEntries(Entry entry) {
        ArrayList<Entry> entries = getAllEntries();
        if (null != entries) {
            for (Entry e : entries) {
                if (entry.getX() == e.getX()) {
                    if (entries.remove(e)) {
                        Gson gson = new Gson();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove(ALL_ENTRIES_KEY);
                        editor.putString(ALL_ENTRIES_KEY, gson.toJson(entries));
                        editor.commit();
                        break;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public boolean removeFromAllNotes(Note note) {
        ArrayList<Note> notes = getAllNotes();
        if (null != notes) {
            for (Note n : notes) {
                if (note.getId() == n.getId()) {
                    if (notes.remove(n)) {
                        Gson gson = new Gson();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove(ALL_NOTES_KEY);
                        editor.putString(ALL_NOTES_KEY, gson.toJson(notes));
                        editor.commit();
                        break;
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
