package com.example.gymbuddy_weighttracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.MarkerImage;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static java.util.Comparator.comparing;

public class MainActivity extends AppCompatActivity {

    private LineChart weightChart;
    private ExtendedFloatingActionButton addEntry;
    private TextView txtNote, txtWeight, txtDate;
    private ConstraintLayout goalDetails;
    private TextView txtStartingDate, txtStartingWeight, txtGoal, txtProgressGoal, txtProgress, txtCurrent, txtCurrentDate, txtProgressDate;
    private EditText edtWeight, edtNote;
    private Button btnAdd, btnDelete;
    private ImageView imgWeight, imgClose, imgCloseInput, imgSwipe;
    private ArrayList<Entry> entries;
    private ArrayList<Note> notes;
    private LineDataSet set1;
    private Spinner spinnerWhen;
    private ArrayList<ILineDataSet> dataSet;
    private LineData data;
    private String unit;
    private boolean isBlurred = false;
    public long refTime = 0;
    public float startingWeight, startingDate;
    private int setBack = 0;
    private boolean doubleBackToExitPressedOnce;
    private CardView details, input;
    private float goal = 0;
    private int comparedWeightOffset = 1;
    private boolean inputOngoing = false;
    private Entry markedEntry;
    boolean[] areEnabled = new boolean[3];
    String[] settingsNames = new String[areEnabled.length];
    Date timeMilliseconds;
    DateFormat dateTimeFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
    DateFormat dateTimeFormatWithHrs = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
    private MenuItem selectedMenuItem;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private boolean wasModified;
    private final float kgToLbsMultiplier = (float) 2.20;
    private float multiplier;
    private float max, min;
//    private AdView mainAd;
    private FrameLayout mainAdContainer;
    private FirebaseAnalytics mFirebaseAnalytics;

    public boolean verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                    .setTitle(R.string.permissions)
                    .setIcon(R.drawable.ic_info)
                    .setMessage(R.string.permissionsMsg)
                    .setPositiveButton(R.string.ok, (dialog, which) -> ActivityCompat.requestPermissions(
                            activity,
                            PERMISSIONS_STORAGE,
                            REQUEST_EXTERNAL_STORAGE))
                    .show();
            return false;
        } else return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onOptionsItemSelected(selectedMenuItem);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        selectedMenuItem = item;
        switch (item.getItemId()) {
            case R.id.setGoal:
                if (entries.isEmpty()) {
                    Toast.makeText(this, getResources().getString(R.string.readigFirst), Toast.LENGTH_SHORT).show();
                } else {
                    handleSetGoal();
                }
                return true;
            case R.id.settings:
                handleSettings();
                return true;
            case R.id.delete:
                handleDeleteAll();
                return true;
            case R.id.save:
                if (verifyStoragePermissions(this)) {
                    handleSave();
                }
                return true;
            case R.id.load:
                if (verifyStoragePermissions(MainActivity.this)) {
                    handleLoad();
                }
                return true;
            case R.id.units:
                handleUnits();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleUnits() {
        String units[] = {getResources().getString(R.string.kgs), getResources().getString(R.string.lbs)};
        int checkedItem = 0;
        if (unit.equals(getResources().getString(R.string.lbs))) {
            checkedItem = 1;
        }
        new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                .setTitle(getResources().getString(R.string.selectUnits))
                .setIcon(R.drawable.ic_settings)
                .setSingleChoiceItems(units, checkedItem, (dialog, which) -> {
                    if (!unit.equals(units[which])) {
                        unit = units[which];
                        Utils.getInstance(MainActivity.this).setUnit(unit);
                        wasModified = true;
                    } else {
                        wasModified = false;
                    }
                })
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    if (!entries.isEmpty() && wasModified) {
                        new AlertDialog.Builder(MainActivity.this, R.style.DefaultAlertDialogTheme)
                                .setTitle(getResources().getString(R.string.conversion))
                                .setIcon(R.drawable.ic_settings)
                                .setMessage(R.string.conversionMsg)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (unit.equals(getResources().getString(R.string.lbs))) {
                                            for (Entry e : entries) {
                                                e.setY(e.getY() * kgToLbsMultiplier);
                                            }
                                            multiplier = kgToLbsMultiplier;
                                        } else {
                                            for (Entry e : entries) {
                                                e.setY(e.getY() / kgToLbsMultiplier);
                                            }
                                            multiplier = 1 / kgToLbsMultiplier;
                                        }
                                        max *= multiplier;
                                        min *= multiplier;
                                        Utils.getInstance(MainActivity.this).setMax(max);
                                        Utils.getInstance(MainActivity.this).setMin(min);
                                        if (goal != 0) {
                                            goal *= multiplier;
                                            startingWeight *= multiplier;
                                            Utils.getInstance(MainActivity.this).setStartingWeight(startingWeight);
                                            Utils.getInstance(MainActivity.this).setGoal(goal);
                                        }
                                        scaleYAxis(0);
                                        scaleYAxis(0);
                                        dataSet = new ArrayList<>();
                                        dataSet.add(newSet(entries));
                                        data = new LineData(dataSet);
                                        weightChart.setData(data);
                                        updateGoal();
                                        drawGoal(goal);
                                        weightChart.invalidate();
                                    }
                                })
                                .setNegativeButton(R.string.no, null)
                                .show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void handleDeleteAll() {
        new AlertDialog.Builder(MainActivity.this, R.style.DefaultAlertDialogTheme)
                .setTitle(R.string.deleteAll)
                .setIcon(R.drawable.ic_delete)
                .setMessage(R.string.deleteAllMsg)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    entries = new ArrayList<>();
                    notes = new ArrayList<>();
                    Utils.getInstance(MainActivity.this).updateNotes(notes);
                    Utils.getInstance(MainActivity.this).updateEntries(entries);
                    dataSet = new ArrayList<>();
                    dataSet.add(newSet(entries));
                    data = new LineData(dataSet);
                    weightChart.setData(data);
                    weightChart.getAxisLeft().removeAllLimitLines();
                    Utils.getInstance(MainActivity.this).setGoal(0);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void handleLoad() {
        new AlertDialog.Builder(MainActivity.this, R.style.DefaultAlertDialogTheme)
                .setTitle(R.string.fileLocation)
                .setIcon(R.drawable.ic_info)
                .setMessage(R.string.fileLocationInfo)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    LayoutInflater inflater = getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.til_dialog, null);
                    final EditText exportName = dialogView.findViewById(R.id.edtDialog);
                    final TextInputLayout exportTil=dialogView.findViewById(R.id.tilDialog);
                    exportTil.setHint(R.string.fileName);
                    exportName.requestFocus();
                    new AlertDialog.Builder(MainActivity.this, R.style.DefaultAlertDialogTheme)
                            .setTitle(R.string.load)
                            .setIcon(R.drawable.ic_load)
                            .setView(dialogView)
                            .setPositiveButton(R.string.load, (dialog1, which1) -> {
                                File directory = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)));
                                String filename = exportName.getText().toString();
                                ObjectInput in;

                                try {
                                    in = new ObjectInputStream(new FileInputStream(directory
                                            + File.separator + filename));
                                    ArrayList<SerializableEntry> serializableEntries = (ArrayList<SerializableEntry>) in.readObject();
                                    in.close();
                                    if (serializableEntries == null || serializableEntries.isEmpty()) {
                                        Toast.makeText(MainActivity.this, R.string.noEntriesToLoad, Toast.LENGTH_SHORT).show();
                                    } else {
                                        refTime = (long) serializableEntries.get(0).getX();
                                        Utils.getInstance(MainActivity.this).setRefTime(refTime);
                                        serializableEntries.remove(0);
                                        entries = new ArrayList<>();
                                        notes = new ArrayList<>();
                                        for (SerializableEntry se : serializableEntries) {
                                            entries.add(new Entry(se.getX(), se.getY()));
                                            notes.add(new Note(se.getX(), se.getNote()));
                                        }
                                        Utils.getInstance(MainActivity.this).updateNotes(notes);
                                        Utils.getInstance(MainActivity.this).updateEntries(entries);
                                        for (Entry e : entries) {
                                            e.setIcon(ContextCompat.getDrawable(this,R.drawable.ic_hexagon_icon));
                                        }
                                        dataSet = new ArrayList<>();
                                        dataSet.add(newSet(entries));
                                        data = new LineData(dataSet);
                                        weightChart.setData(data);
                                        if (!entries.isEmpty()) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                max = entries.stream().max(comparing(Entry::getY)).get().getY();
                                                min = entries.stream().min(comparing(Entry::getY)).get().getY();
                                            }
                                        }
                                        Utils.getInstance(MainActivity.this).setMax(max);
                                        Utils.getInstance(MainActivity.this).setMin(min);
                                        scaleYAxis(0);
                                        if (Utils.getInstance(MainActivity.this).getGoal() != 0) {
                                            clearGoal();
                                        }
                                    }
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
//                                            Toast.makeText(MainActivity.this, e + "", Toast.LENGTH_LONG).show();
                                    new AlertDialog.Builder(MainActivity.this, R.style.DefaultAlertDialogTheme)
                                            .setTitle(R.string.fileNotFound)
                                            .setIcon(R.drawable.ic_info)
                                            .setMessage(R.string.checkLocationName)
                                            .setPositiveButton(R.string.ok, null)
                                            .show();
                                } catch (IOException | ClassNotFoundException e) {
                                    e.printStackTrace();
                                    Toast.makeText(MainActivity.this, "" + e, Toast.LENGTH_SHORT).show();
                                }

                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                })
                .show();
    }

    private void handleSave() {
        File directory = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)));
        if (!directory.exists()) {
            directory.mkdirs();
        }

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.til_dialog, null);
        final EditText exportName = dialogView.findViewById(R.id.edtDialog);
        final TextInputLayout exportTil=dialogView.findViewById(R.id.tilDialog);
        exportTil.setHint(R.string.fileName);
        exportName.requestFocus();

        new AlertDialog.Builder(MainActivity.this, R.style.DefaultAlertDialogTheme)
                .setTitle(R.string.save)
                .setIcon(R.drawable.ic_save)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    if (exportName.getText().toString().isEmpty()) {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.insertName), Toast.LENGTH_SHORT).show();
                    } else {
                        ArrayList<SerializableEntry> serializableEntries = new ArrayList<>();
                        for (int i = 0; i < entries.size(); i++) {
                            serializableEntries.add(new SerializableEntry(entries.get(i).getX(), entries.get(i).getY(), notes.get(i).getNote()));
                        }

                        serializableEntries.add(0, new SerializableEntry(refTime, 0, "refTime"));

                        String filename = exportName.getText().toString();
                        ObjectOutput out;

                        try {
                            out = new ObjectOutputStream(new FileOutputStream(directory
                                    + File.separator + filename));
                            out.writeObject(serializableEntries);
                            out.close();
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.dataSavedIn) + " " + directory, Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, e + "", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void handleSettings() {
        new AlertDialog.Builder(this, R.style.DefaultAlertDialogTheme)
                .setTitle(R.string.settings)
                .setIcon(R.drawable.ic_settings)
                .setMultiChoiceItems(settingsNames, areEnabled, (dialog, which, isChecked) -> areEnabled[which] = isChecked)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    weightChart.getAxisLeft().setDrawGridLines(areEnabled[1]);
                    weightChart.getXAxis().setDrawGridLines(areEnabled[0]);
                    set1.setDrawHorizontalHighlightIndicator(areEnabled[2]);
                    set1.setDrawVerticalHighlightIndicator(areEnabled[2]);
                    weightChart.invalidate();

                    Utils.getInstance(MainActivity.this).setSetting1(areEnabled[0]);
                    Utils.getInstance(MainActivity.this).setSetting2(areEnabled[1]);
                    Utils.getInstance(MainActivity.this).setSetting3(areEnabled[2]);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void handleSetGoal() {
        new AlertDialog.Builder(MainActivity.this, R.style.DefaultAlertDialogTheme)
                .setTitle(R.string.startingWeight)
                .setMessage(R.string.startingWeightMsg)
                .setIcon(R.drawable.ic_info)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    LayoutInflater inflater = getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.til_dialog, null);
                    final EditText exportName = dialogView.findViewById(R.id.edtDialog);
                    final TextInputLayout exportTil=dialogView.findViewById(R.id.tilDialog);
                    exportTil.setHint(R.string.weight);
                    exportName.requestFocus();
                    exportName.setInputType(InputType.TYPE_CLASS_NUMBER);
                    exportName.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    exportName.setKeyListener(DigitsKeyListener.getInstance(false, true));
                    exportName.getBackground().setColorFilter(Color.parseColor("#B1FF35"), PorterDuff.Mode.SRC_IN);
                    new AlertDialog.Builder(MainActivity.this, R.style.DefaultAlertDialogTheme)
                            .setTitle(R.string.setNewGoal)
                            .setIcon(R.drawable.ic_goal)
                            .setView(dialogView)
                            .setPositiveButton(R.string.set, (dialog1, which1) -> {
                                if (exportName.getText().toString().isEmpty()) {
                                    Toast.makeText(MainActivity.this, R.string.insertWeight, Toast.LENGTH_SHORT).show();
                                    handleSetGoal();
                                } else if (Float.parseFloat(exportName.getText().toString()) == 0) {
                                    Toast.makeText(MainActivity.this, R.string.goalZero, Toast.LENGTH_SHORT).show();
                                    handleSetGoal();
                                } else {
                                    goal = Float.parseFloat(exportName.getText().toString());
                                    setGoal(goal);
                                    Utils.getInstance(MainActivity.this).setStartingDate(startingDate);
                                    Utils.getInstance(MainActivity.this).setStartingWeight(startingWeight);
                                    Utils.getInstance(MainActivity.this).setGoal(goal);
                                    setGoal(Float.parseFloat(exportName.getText().toString()));
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();

    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;

        Toast.makeText(this, R.string.pressAgain, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_GymBuddyWeightTracker);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        unit = Utils.getInstance(this).getUnit();
        refTime = Utils.getInstance(this).getRefTime();
        areEnabled[0] = Utils.getInstance(this).getSetting1();
        areEnabled[1] = Utils.getInstance(this).getSetting2();
        areEnabled[2] = Utils.getInstance(this).getSetting3();
        settingsNames[0] = getResources().getString(R.string.drawVerticalGrid);
        settingsNames[1] = getResources().getString(R.string.drawHorizontalGrid);
        settingsNames[2] = getResources().getString(R.string.drawHighlight);

        notes = Utils.getInstance(this).getAllNotes();
        entries = Utils.getInstance(this).getAllEntries();
        max = Utils.getInstance(this).getMax();
        min = Utils.getInstance(this).getMin();

        for (Entry e : entries) {
            e.setIcon(ContextCompat.getDrawable(this,R.drawable.ic_hexagon_icon));
        }

        goal = Utils.getInstance(this).getGoal();
        startingDate = Utils.getInstance(this).getStartingDate();
        startingWeight = Utils.getInstance(this).getStartingWeight();

        initViews();
        updateCurrentWeight();
        comparedWeightOffset = 1;
        updateLastProgress(comparedWeightOffset);
        setGoal(goal);
        updateGoal();

        MobileAds.initialize(this);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mainAd.loadAd(adRequest);
        Helpers.handleAds(mainAdContainer,this);

        addEntry.setOnClickListener(v -> {
            inputOngoing = true;
            input.setVisibility(View.VISIBLE);
            addEntry.setVisibility(View.GONE);
            isBlurred=Helpers.setLayoutAlpha(goalDetails);
            edtWeight.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        });

        btnDelete.setOnClickListener(v -> new AlertDialog.Builder(MainActivity.this, R.style.DefaultAlertDialogTheme)
                .setTitle(R.string.deleting)
                .setIcon(R.drawable.ic_delete)
                .setMessage(R.string.deletingMsg)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    addEntry.show();
                    handleDeleteEntry();
                    isBlurred=Helpers.resetLayoutAlpha(goalDetails);
                })
                .setNegativeButton(R.string.cancel, null)
                .show());

        btnAdd.setOnClickListener(v -> {
            hideKeyboard();
            isBlurred=Helpers.resetLayoutAlpha(goalDetails);
            if (edtWeight.getText().toString().isEmpty()) {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.insertWeight), Toast.LENGTH_SHORT).show();
                Helpers.shake(imgWeight);
                imgWeight.setImageResource(R.drawable.ic_hexagon_single_red);
            } else {
                switch (spinnerWhen.getSelectedItemPosition()) {
                    case 1:
                        setBack = 1;
                        break;
                    case 2:
                        setBack = 2;
                        break;
                    default:
                        setBack = 0;
                        break;
                }
                input.setVisibility(View.GONE);
                addEntry.setVisibility(View.VISIBLE);
                imgWeight.setImageResource(R.drawable.ic_hexagon_single_empty);
                float time;
                if (refTime == 0 || entries.isEmpty()) {
                    refTime = System.currentTimeMillis() / 1000;
                    Utils.getInstance(MainActivity.this).setRefTime(refTime);
                    time = 0;
                } else {
                    time = (System.currentTimeMillis() / 1000) - Helpers.daysToSeconds(setBack) - refTime;
                }
                if (time < 0) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.notBeforeFirst), Toast.LENGTH_LONG).show();
                } else {
                    Entry newEntry = new Entry(time, Float.parseFloat(edtWeight.getText().toString()));
                    Note newNote;
                    if (edtNote.getText().toString().isEmpty()) {
                        newNote = new Note(time, "---");
                    } else {
                        newNote = new Note(time, edtNote.getText().toString());
                    }
                    if (!entries.isEmpty()) {

                        if (time < entries.get(entries.size() - 1).getX()) {
                            addEntryOutsideOrder(newEntry, newNote);
                        } else {
                            addEntryOrdered(newEntry, newNote);
                            updateGoal();
                        }
                    } else {
                        addEntryOrdered(newEntry, newNote);
                    }
                    if (entries.size() == 1) {
                        max = entries.get(0).getY();
                        min = max;
                        Utils.getInstance(MainActivity.this).setMax(max);
                        Utils.getInstance(MainActivity.this).setMin(min);
                    } else {
                        scaleYAxis(newEntry.getY());
                    }
                }
            }

        });

        dataSet = new ArrayList<>();
        dataSet.add(newSet(entries));

        data = new LineData(dataSet);

        weightChart.setData(data);
        weightChart.getAxisRight().setEnabled(false);
        weightChart.getLegend().setEnabled(false);
        weightChart.getDescription().setEnabled(false);
        weightChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        weightChart.getXAxis().setTextColor(getResources().getColor(R.color.white));
        weightChart.getXAxis().setAxisLineWidth(2);
        weightChart.getAxisLeft().setDrawGridLines(areEnabled[1]);
        weightChart.getAxisLeft().setTextColor(getResources().getColor(R.color.lime_500));
        weightChart.getAxisLeft().setTextSize(15);
        weightChart.getAxisLeft().setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        weightChart.getXAxis().setDrawGridLines(areEnabled[0]);
        weightChart.getAxisLeft().setAxisLineWidth(2);
        weightChart.getXAxis().setValueFormatter(new LineChartXAxisValueFormatter(refTime));
        MarkerImage markerImage = new MarkerImage(this, R.drawable.ic_hexagon_marker);
        markerImage.setOffset(-13, -13);

        weightChart.setMarker(markerImage);
        weightChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                markedEntry = e;
                if (!inputOngoing) {
                    addEntry.setVisibility(View.GONE);
                    details.setVisibility(View.VISIBLE);
                    if (!isBlurred) isBlurred=Helpers.setLayoutAlpha(goalDetails);
                    txtWeight.setText(Helpers.stringFormat(e.getY()) + unit);
                    timeMilliseconds = new Date(((long) e.getX() + refTime) * 1000);
                    txtDate.setText(dateTimeFormatWithHrs.format(timeMilliseconds));
                    for (int i = 0; i < entries.size(); i++) {
                        if (entries.get(i).getX() == e.getX()) {
                            txtNote.setText(notes.get(i).getNote());
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected() {
                details.setVisibility(View.GONE);
                isBlurred=Helpers.resetLayoutAlpha(goalDetails);
                addEntry.setVisibility(View.VISIBLE);

            }
        });

        weightChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartLongPressed(MotionEvent me) {
                if (weightChart.getViewPortHandler().getScaleX() != 1) {
                    weightChart.fitScreen();
                }
            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {

            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {
            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {

            }
        });

        scaleYAxis(0);

        imgClose.setOnClickListener(v -> {
            details.setVisibility(View.GONE);
            isBlurred=Helpers.resetLayoutAlpha(goalDetails);
            addEntry.show();
        });

        imgCloseInput.setOnClickListener(v -> {
            hideKeyboard();
            isBlurred=Helpers.resetLayoutAlpha(goalDetails);
            input.setVisibility(View.GONE);
            addEntry.show();
            inputOngoing = false;

        });

        imgSwipe.setOnTouchListener(new OnSwipeListener(MainActivity.this) {
            public void onSwipeRight() {
                if (entries.size() > 1 && comparedWeightOffset < entries.size() - 1) {
                    comparedWeightOffset++;
                    updateLastProgress(comparedWeightOffset);
                    ObjectAnimator textViewAnimation = ObjectAnimator.ofFloat(txtProgress, "X", txtProgress.getX() - 40f, txtProgress.getX());
                    textViewAnimation.setDuration(200);
                    textViewAnimation.start();
                }
            }

            public void onSwipeLeft() {
                if (entries.size() > 2 && comparedWeightOffset > 1) {
                    comparedWeightOffset--;
                    updateLastProgress(comparedWeightOffset);
                    ObjectAnimator textViewAnimation = ObjectAnimator.ofFloat(txtProgress, "X", txtProgress.getX() + 40f, txtProgress.getX());
                    textViewAnimation.setDuration(200);
                    textViewAnimation.start();
                }
            }

            public void onSwipeTop() {
                if (entries.size() > 2 && comparedWeightOffset != entries.size() - 1) {
                    comparedWeightOffset = entries.size() - 1;
                    updateLastProgress(comparedWeightOffset);
                    ObjectAnimator textViewAnimation = ObjectAnimator.ofFloat(txtProgress, "Y", txtProgress.getY() + 40f, txtProgress.getY());
                    textViewAnimation.setDuration(200);
                    textViewAnimation.start();
                }

            }

            public void onSwipeBottom() {
                if (entries.size() > 2 && comparedWeightOffset != 1) {
                    comparedWeightOffset = 1;
                    updateLastProgress(comparedWeightOffset);
                    ObjectAnimator textViewAnimation = ObjectAnimator.ofFloat(txtProgress, "Y", txtProgress.getY() - 40f, txtProgress.getY());
                    textViewAnimation.setDuration(200);
                    textViewAnimation.start();
                }
            }

        });
    }

    private void updateLastProgress(int offset) {
        if (entries.size() > 1) {
            txtProgress.setText(Helpers.stringFormat(entries.get(entries.size() - 1).getY() - entries.get(entries.size() - 1 - offset).getY()) + unit);
            float period = entries.get(entries.size() - 1).getX() - entries.get(entries.size() - 1 - offset).getX();
            String periodTxt;
            String unit = getResources().getString(R.string.days);
            if (Helpers.secsToDays(period) == 1) unit = getResources().getString(R.string.day);
            periodTxt = String.valueOf(Helpers.secsToDays(period));

            txtProgressDate.setText(periodTxt + unit);
        } else {
            txtProgress.setText("---");
        }
    }

    private void setGoal(float weight) {
        goal = weight;
        if (goal == 0) {
            clearGoal();
        } else {
            startingWeight = entries.get(entries.size() - 1).getY();
            startingDate = entries.get(entries.size() - 1).getX();
            drawGoal(goal);
            txtStartingWeight.setText(startingWeight + unit);
            txtGoal.setText(goal + unit);
//        timeMilliseconds = new Date((long) ((startingDate + refTime) * 1000));
            txtStartingDate.setText(dateTimeFormat.format((startingDate + refTime) * 1000));
        }
    }

    private void updateGoal() {
        if (goal != 0 && entries.size() > 1) {
            txtProgressGoal.setText(Helpers.stringFormat(entries.get(entries.size() - 1).getY() - startingWeight) + unit);

//            if (entries.get(entries.size() - 1).getY() > entries.get(entries.size() - 2).getY() &&
//                    goal > entries.get(entries.size() - 1).getY()) {
//                txtProgress.setTextColor(getResources().getColor(R.color.lime_500));
//            } else if (entries.get(entries.size() - 1).getY() < entries.get(entries.size() - 2).getY() &&
//                    goal < entries.get(entries.size() - 1).getY()) {
//                txtProgress.setTextColor(getResources().getColor(R.color.lime_500));
//            } else {
//                txtProgress.setTextColor(getResources().getColor(R.color.white));
//            }
        }
    }

    private void clearGoal() {
        txtGoal.setText("---");
        txtStartingWeight.setText("---");
        txtStartingDate.setText("---");
        txtProgressGoal.setText("---");
        weightChart.getAxisLeft().removeAllLimitLines();
        Utils.getInstance(this).setGoal(0);
    }

    private void updateCurrentWeight() {
        if (!entries.isEmpty()) {
            txtCurrent.setText(entries.get(entries.size() - 1).getY() + unit);
            txtCurrentDate.setText(dateTimeFormat.format((entries.get(entries.size() - 1).getX() + refTime) * 1000));
        } else {
            txtCurrent.setText("---");
            txtCurrentDate.setText("---");
        }
    }

    private void addEntryOutsideOrder(Entry newEntry, Note newNote) {
        for (int i = entries.size() - 1; i > 0; i--) {
            if (entries.get(i).getX() < newEntry.getX()) {
                Utils.getInstance(MainActivity.this).addToAllEntriesOrdered(newEntry, i + 1);
                Utils.getInstance(MainActivity.this).addToAllNotesOrdered(newNote, i + 1);
                newEntry.setIcon(ContextCompat.getDrawable(this,R.drawable.ic_hexagon_icon));
                entries.add(i + 1, newEntry);
                notes.add(i + 1, newNote);
                weightChart.notifyDataSetChanged();
                updateGoal();
                break;
            }
        }
    }

    private void addEntryOrdered(Entry newEntry, Note newNote) {
        Utils.getInstance(MainActivity.this).addToAllEntries(newEntry);
        Utils.getInstance(MainActivity.this).addToAllNotes(newNote);
        notes.add(newNote);
        addEntry(newEntry);
        inputOngoing = false;
        if (!txtCurrent.isShown()) txtCurrent.setVisibility(View.VISIBLE);
        updateCurrentWeight();
        comparedWeightOffset = 1;
        updateLastProgress(comparedWeightOffset);
        hideKeyboard();

    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void handleDeleteEntry() {
        boolean lastEntry = false;
        LineData data = weightChart.getData();
        weightChart.notifyDataSetChanged();
        for (int i = entries.size() - 1; i >= 0; i--) {
            if (markedEntry.getX() == entries.get(i).getX()) {
                Utils.getInstance(this).removeFromAllNotes(notes.get(i));
                notes.remove(i);
                if (i == entries.size() - 1) {
                    lastEntry = true;
                }
            }
        }
        Utils.getInstance(this).removeFromAllEntries(markedEntry);
        data.removeEntry(markedEntry, 0);
        entries = Utils.getInstance(this).getAllEntries();
        if (lastEntry) {
            updateCurrentWeight();
            comparedWeightOffset = 1;
            updateLastProgress(comparedWeightOffset);
        }
        if (goal != 0 && markedEntry.getY() == startingWeight && markedEntry.getX() == startingDate) {
            clearGoal();
        }
        details.setVisibility(View.GONE);
        if (!entries.isEmpty()) {
            if (markedEntry.getY() == max) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    max = entries.stream().max(comparing(Entry::getY)).get().getY();
                    Utils.getInstance(this).setMax(max);
                    scaleYAxis(0);
                }
            }
            if (markedEntry.getY() == min) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    min = entries.stream().min(comparing(Entry::getY)).get().getY();
                    Utils.getInstance(this).setMin(min);
                    scaleYAxis(0);
                }
            }
        }
    }

    private void scaleYAxis(float newY) {

        if (newY != 0) {
            if (entries.size() == 1) {
                min = entries.get(0).getY();
                max = entries.get(0).getY();
            } else if (newY > max) {
                max = newY;
                Utils.getInstance(this).setMax(max);
            } else if (newY < min) {
                min = newY;
                Utils.getInstance(this).setMin(min);
            }
        }
        weightChart.getAxisLeft().setAxisMaximum(1.1f * max);
        weightChart.getAxisLeft().setAxisMinimum(0.8f * min);
        weightChart.invalidate();
    }

    private void addEntry(Entry entry) {
        LineData data = weightChart.getData();
        entry.setIcon(ContextCompat.getDrawable(this,R.drawable.ic_hexagon_icon));
        data.addEntry(entry, 0);
        data.notifyDataChanged();
        scaleYAxis(entry.getY());
        weightChart.notifyDataSetChanged();
        weightChart.moveViewToAnimated(entries.get(entries.size() - 1).getX(), 0, weightChart.getAxisLeft().getAxisDependency(), 500);
    }

    private void drawGoal(float weight) {
        LimitLine ll1 = new LimitLine(weight, "");
        ll1.setLineWidth(0.5f);
        ll1.setLineColor(getResources().getColor(R.color.lime_700));
        ll1.enableDashedLine(20f, 10f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        weightChart.getAxisLeft().removeAllLimitLines();
        weightChart.getAxisLeft().addLimitLine(ll1);
    }

    private LineDataSet newSet(ArrayList<Entry> yValues) {
        set1 = new LineDataSet(yValues, "Dataset1");
        set1.setHighLightColor(getResources().getColor(R.color.lime_500));
        set1.setDrawHorizontalHighlightIndicator(areEnabled[2]);
        set1.setDrawVerticalHighlightIndicator(areEnabled[2]);
        set1.setHighlightLineWidth(0f);
        set1.setFillAlpha(110);
        set1.setColor(getResources().getColor(R.color.lime_500));
        set1.setCircleRadius(2);
        set1.setCircleColor(getResources().getColor(R.color.lime_700));
        set1.setCircleHoleColor(getResources().getColor(R.color.grey_500));
        set1.setCircleHoleRadius(1);
        set1.setDrawValues(false);
        set1.setValueTextColor(getResources().getColor(R.color.grey_200));
        set1.setLineWidth(4);
        set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set1.setCubicIntensity(0.2f);
        return set1;
    }

    private void initViews() {
        Helpers.setupActionBar(getResources().getString(R.string.gbWeightTracker), "",getSupportActionBar(),this);
        weightChart = findViewById(R.id.weightChart);
        addEntry = findViewById(R.id.addEntry);
        edtWeight = findViewById(R.id.edtWeight);
        edtNote = findViewById(R.id.edtNote);
        btnAdd = findViewById(R.id.btnAdd);
        imgWeight = findViewById(R.id.imgWeight);
        details = findViewById(R.id.details);
        input = findViewById(R.id.input);
        txtNote = findViewById(R.id.txtNote);
        txtWeight = findViewById(R.id.txtWeight);
        txtDate = findViewById(R.id.txtDate);
        imgClose = findViewById(R.id.imgClose);
        imgCloseInput = findViewById(R.id.imgCloseInput);
        weightChart.setDragEnabled(true);
        weightChart.setScaleYEnabled(false);
        goalDetails = findViewById(R.id.goalDetails);
        btnDelete = findViewById(R.id.btnDelete);
        txtStartingDate = findViewById(R.id.txtStartingDate);
        txtStartingDate.setTextColor(getResources().getColor(R.color.white));
        txtStartingWeight = findViewById(R.id.txtStartingWeight);
        txtStartingWeight.setTextColor(getResources().getColor(R.color.white));
        txtGoal = findViewById(R.id.txtGoal);
        txtGoal.setTextColor(getResources().getColor(R.color.white));
        txtProgressGoal = findViewById(R.id.txtProgressGoal);
        txtProgressGoal.setTextColor(getResources().getColor(R.color.white));
        txtProgress = findViewById(R.id.txtProgress);
        txtProgress.setTextColor(getResources().getColor(R.color.white));
        txtCurrent = findViewById(R.id.txtCurrent);
        txtCurrentDate = findViewById(R.id.txtCurrentDate);
        spinnerWhen = findViewById(R.id.spinnerWhen);
        imgSwipe = findViewById(R.id.imgSwipe);
        txtProgressDate = findViewById(R.id.txtProgressDate);
        mainAdContainer = findViewById(R.id.mainAdContainer);
    }
}