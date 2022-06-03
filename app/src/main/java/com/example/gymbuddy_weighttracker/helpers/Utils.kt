package com.example.gymbuddy_weighttracker.helpers

import android.content.Context
import android.content.SharedPreferences
import com.example.gymbuddy_weighttracker.model.BodyPart
import com.example.gymbuddy_weighttracker.model.Note
import com.github.mikephil.charting.data.Entry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class Utils(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("weight_db", Context.MODE_PRIVATE)

    var lastAdShown: Long
        get() = sharedPreferences.getLong(LAST_AD_SHOWN_KEY, 0)
        set(lastAdShown) {
            val editor = sharedPreferences.edit()
            editor.putLong(LAST_AD_SHOWN_KEY, lastAdShown)
            editor.apply()
        }

    val lastAppRating: Long
        get() = sharedPreferences.getLong(APP_RATING_KEY, 0)

    fun setLastAppRating(lastAppRating: Long): Boolean {
        val editor = sharedPreferences.edit()
        editor.putLong(APP_RATING_KEY, lastAppRating)
        editor.apply()
        return true
    }

    val unit: String?
        get() = sharedPreferences.getString(UNIT_KEY, " kg")

    fun setUnit(unit: String?): Boolean {
        val editor = sharedPreferences.edit()
        editor.putString(UNIT_KEY, unit)
        editor.apply()
        return true
    }

    val refTime: Long
        get() = sharedPreferences.getLong(REF_TIME_KEY, 0)

    fun setRefTime(refTime: Long): Boolean {
        val editor = sharedPreferences.edit()
        editor.putLong(REF_TIME_KEY, refTime)
        editor.apply()
        return true
    }

    val startingWeight: Float
        get() = sharedPreferences.getFloat(STARTING_WEIGHT, 0f)

    fun setStartingWeight(startingWeight: Float): Boolean {
        val editor = sharedPreferences.edit()
        editor.putFloat(STARTING_WEIGHT, startingWeight)
        editor.apply()
        return true
    }

    val max: Float
        get() = sharedPreferences.getFloat(MAX_KEY, 0f)

    fun setMax(max: Float): Boolean {
        val editor = sharedPreferences.edit()
        editor.putFloat(MAX_KEY, max)
        editor.apply()
        return true
    }

    val min: Float
        get() = sharedPreferences.getFloat(MIN_KEY, 0f)

    fun setMin(min: Float): Boolean {
        val editor = sharedPreferences.edit()
        editor.putFloat(MIN_KEY, min)
        editor.apply()
        return true
    }

    val startingDate: Float
        get() = sharedPreferences.getFloat(STARTING_DATE, 0f)

    fun setStartingDate(startingDate: Float): Boolean {
        val editor = sharedPreferences.edit()
        editor.putFloat(STARTING_DATE, startingDate)
        editor.apply()
        return true
    }

    val goal: Float
        get() = sharedPreferences.getFloat(GOAL_KEY, 0f)

    fun setGoal(goal: Float): Boolean {
        val editor = sharedPreferences.edit()
        editor.putFloat(GOAL_KEY, goal)
        editor.apply()
        return true
    }

    val setting1: Boolean
        get() = sharedPreferences.getBoolean(SETTING1_KEY, false)

    fun setSetting1(setting1: Boolean): Boolean {
        val editor = sharedPreferences.edit()
        editor.putBoolean(SETTING1_KEY, setting1)
        editor.apply()
        return true
    }

    val setting2: Boolean
        get() = sharedPreferences.getBoolean(SETTING2_KEY, false)

    fun setSetting2(setting2: Boolean): Boolean {
        val editor = sharedPreferences.edit()
        editor.putBoolean(SETTING2_KEY, setting2)
        editor.apply()
        return true
    }

    val setting3: Boolean
        get() = sharedPreferences.getBoolean(SETTING3_KEY, false)

    fun setSetting3(setting3: Boolean): Boolean {
        val editor = sharedPreferences.edit()
        editor.putBoolean(SETTING3_KEY, setting2)
        editor.apply()
        return true
    }

    private fun initData() {
        val entries = ArrayList<Entry>()
        val notes = ArrayList<String>()
        val bodyParts = ArrayList<BodyPart>()
        val editor = sharedPreferences.edit()
        val gson = Gson()
        editor.putString(ALL_NOTES_KEY, gson.toJson(notes))
        editor.putString(ALL_ENTRIES_KEY, gson.toJson(entries))
        editor.putString(ALL_BODY_PARTS_KEY, gson.toJson(bodyParts))
        editor.apply()
    }

    val allEntries: ArrayList<Entry>?
        get() {
            val gson = Gson()
            val type = object : TypeToken<ArrayList<Entry?>?>() {}.type
            return gson.fromJson(sharedPreferences.getString(ALL_ENTRIES_KEY, null), type)
        }

    val allNotes: ArrayList<Note>?
        get() {
            val gson = Gson()
            val type = object : TypeToken<ArrayList<Note?>?>() {}.type
            return gson.fromJson(sharedPreferences.getString(ALL_NOTES_KEY, null), type)
        }

    val allBodyParts: ArrayList<BodyPart>?
        get() {
            val gson = Gson()
            val type = object : TypeToken<ArrayList<BodyPart>?>() {}.type
            return gson.fromJson(sharedPreferences.getString(ALL_BODY_PARTS_KEY, null), type)
        }

    fun updateBodyParts(bodyParts: ArrayList<BodyPart>?): Boolean {
        if (null != bodyParts) {
            val gson = Gson()
            val editor = sharedPreferences.edit()
            editor.remove(ALL_BODY_PARTS_KEY)
            editor.putString(ALL_BODY_PARTS_KEY, gson.toJson(bodyParts))
            editor.apply()
        }
        return false
    }

    fun updateEntries(entries: ArrayList<Entry>?): Boolean {
        if (null != entries) {
            val gson = Gson()
            val editor = sharedPreferences.edit()
            editor.remove(ALL_ENTRIES_KEY)
            editor.putString(ALL_ENTRIES_KEY, gson.toJson(entries))
            editor.apply()
        }
        return false
    }

    fun updateNotes(notes: ArrayList<Note>?): Boolean {
        if (null != notes) {
            val gson = Gson()
            val editor = sharedPreferences.edit()
            editor.remove(ALL_NOTES_KEY)
            editor.putString(ALL_NOTES_KEY, gson.toJson(notes))
            editor.apply()
        }
        return false
    }

    fun addToAllEntries(entry: Entry): Boolean {
        val entries = allEntries
        entries?.let {
            if (entries.add(entry)) {
                val gson = Gson()
                val editor = sharedPreferences.edit()
                editor.remove(ALL_ENTRIES_KEY)
                editor.putString(ALL_ENTRIES_KEY, gson.toJson(entries))
                editor.apply()
            }
            return true
        } ?: run { return false }
    }

    fun addToAllNotes(note: Note): Boolean {
        val notes = allNotes
        notes?.let {
            if (notes.add(note)) {
                val gson = Gson()
                val editor = sharedPreferences.edit()
                editor.remove(ALL_NOTES_KEY)
                editor.putString(ALL_NOTES_KEY, gson.toJson(notes))
                editor.apply()
            }
            return true
        } ?: run { return false }

    }

    fun addToAllEntriesOrdered(entry: Entry, position: Int): Boolean {
        val entries = allEntries
        entries?.let {
            entries.add(position, entry)
            val gson = Gson()
            val editor = sharedPreferences.edit()
            editor.remove(ALL_ENTRIES_KEY)
            editor.putString(ALL_ENTRIES_KEY, gson.toJson(entries))
            editor.apply()
            return true
        } ?: run { return false }

    }

    fun addToAllNotesOrdered(note: Note, position: Int): Boolean {
        val notes = allNotes
        notes?.let {
            notes.add(position, note)
            val gson = Gson()
            val editor = sharedPreferences.edit()
            editor.remove(ALL_NOTES_KEY)
            editor.putString(ALL_NOTES_KEY, gson.toJson(notes))
            editor.apply()
            return true
        } ?: run { return false }

    }

    fun removeFromAllEntries(entry: Entry): Boolean {
        val entries = allEntries
        entries?.let {
            for (e in entries) {
                if (entry.x == e.x) {
                    if (entries.remove(e)) {
                        val gson = Gson()
                        val editor = sharedPreferences.edit()
                        editor.remove(ALL_ENTRIES_KEY)
                        editor.putString(ALL_ENTRIES_KEY, gson.toJson(entries))
                        editor.apply()
                        break
                    }
                    return true
                }
            }
        }
        return false
    }

    fun removeFromAllNotes(note: Note): Boolean {
        val notes = allNotes
        notes?.let {
            for (n in notes) {
                if (note.id == n.id) {
                    if (notes.remove(n)) {
                        val gson = Gson()
                        val editor = sharedPreferences.edit()
                        editor.remove(ALL_NOTES_KEY)
                        editor.putString(ALL_NOTES_KEY, gson.toJson(notes))
                        editor.apply()
                        break
                    }
                    return true
                }
            }
            return false
        } ?: run { return false }
    }

    companion object INSTANCE {
        private var instance: Utils? = null
        private const val ALL_ENTRIES_KEY = "all entries"
        const val ALL_NOTES_KEY = "all notes"
        const val REF_TIME_KEY = "ref time"
        const val GOAL_KEY = "goal"
        const val SETTING1_KEY = "setting1"
        const val SETTING2_KEY = "setting2"
        const val SETTING3_KEY = "setting3"
        const val STARTING_WEIGHT = "starting weight"
        const val STARTING_DATE = "starting date"
        const val UNIT_KEY = "unit"
        const val MAX_KEY = "max"
        const val MIN_KEY = "min"
        const val APP_RATING_KEY = "app rating"
        const val ALL_BODY_PARTS_KEY = "all body parts"
        const val LAST_AD_SHOWN_KEY = "last ad"
        var refTime = 0f
        var goal = 0f
        var startingWeight = 0f
        var startingDate = 0f
        var max = 0f
        var min = 0f
        var setting1 = false
        var setting2 = false
        var setting3 = false
        var unit: String? = null
        var lastAppRating: Long = 0
        var lastAdShown: Long = 0
        fun getInstance(context: Context): Utils {
            if (null == instance) {
                instance = Utils(context)
            }
            return instance as Utils
        }
    }

    init {
        if (null == allEntries) {
            initData()
            INSTANCE.max = max
            INSTANCE.min = min
            INSTANCE.refTime = refTime.toFloat()
            INSTANCE.goal = goal
            INSTANCE.setting1 = setting1
            INSTANCE.setting2 = setting2
            INSTANCE.setting3 = setting3
            INSTANCE.startingDate = startingDate
            INSTANCE.startingWeight = startingDate
            INSTANCE.unit = unit
            INSTANCE.lastAppRating = lastAppRating
            INSTANCE.lastAdShown = lastAdShown
        }
    }
}