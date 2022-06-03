package com.example.gymbuddy_weighttracker.view

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.gymbuddy_weighttracker.R
import com.example.gymbuddy_weighttracker.databinding.FragmentWeightBinding
import com.example.gymbuddy_weighttracker.helpers.*
import com.example.gymbuddy_weighttracker.helpers.Helpers.WEIGHT_FRAGMENT_TAG
import com.example.gymbuddy_weighttracker.helpers.Helpers.blank
import com.example.gymbuddy_weighttracker.helpers.Helpers.cmToInMultiplier
import com.example.gymbuddy_weighttracker.helpers.Helpers.dataToArrayOfCoordinates
import com.example.gymbuddy_weighttracker.helpers.Helpers.hideKeyboard
import com.example.gymbuddy_weighttracker.helpers.Helpers.hideKeyboardForced
import com.example.gymbuddy_weighttracker.helpers.Helpers.kgToLbsMultiplier
import com.example.gymbuddy_weighttracker.helpers.Helpers.secsToDays
import com.example.gymbuddy_weighttracker.helpers.Helpers.showKeyboard
import com.example.gymbuddy_weighttracker.helpers.Helpers.toPrettyString
import com.example.gymbuddy_weighttracker.helpers.Helpers.toPrettyStringDecimals
import com.example.gymbuddy_weighttracker.model.Data
import com.example.gymbuddy_weighttracker.model.Note
import com.example.gymbuddy_weighttracker.model.SerializableEntry
import com.example.gymbuddy_weighttracker.viewModel.MainViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.MarkerImage
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.*
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.text.DateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit

class WeightFragment : Fragment() {
    private lateinit var entries: ArrayList<Entry>
    private lateinit var notes: ArrayList<Note>
    private var data: LineData? = null
    private var unit: String? = null
    private var isBlurred = false
    private var refTime: Long = 0
    private var startingWeight = 0f
    private var startingDate = 0f
    private var setBack = 0
    private var goal = 0f
//    private var inputOngoing = false
    private var markedEntry: Entry? = null
    private var areEnabled = BooleanArray(3)
    private var settingsNames = arrayOfNulls<String>(areEnabled.size)
    private lateinit var timeMilliseconds: Date
    private var dateTimeFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
    private var dateTimeFormatWithHrs = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())
    private var selectedMenuItem: MenuItem? = null
    private var wasModified = false
    private var multiplier = 0f
    private var max = 0f
    private var min = 0f
    private val gson = Gson()
    private var dataToSave: Data? = null
    private lateinit var returnData: Data
    private lateinit var binding: FragmentWeightBinding
    private var googleFitAuthLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> onGoogleFitAuthResult(result) }
    private var createFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> onCreateFileResult(result) }
    private var openFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> onOpenFileResult(result) }
    private val viewModel: MainViewModel by activityViewModels()
    private var user: FirebaseUser? = null
    private var authMenuItem: MenuItem? = null
    private var googleFitMenuItem: MenuItem? = null
    private val signInLauncher = registerForActivityResult(FirebaseAuthUIActivityResultContract()) { result: FirebaseAuthUIAuthenticationResult -> onSignInResult(result) }
    private val weightFragmentTag = "WeightFragment"
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_WRITE)
        .build()
    private var accessToGoogleFitGranted = false
    private var account: GoogleSignInAccount? = null

    private fun onGoogleFitAuthResult(result: ActivityResult?) {
        result?.let {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                accessToGoogleFitGranted = true
                googleFitMenuItem?.setTitle(R.string.connectedToGoogleFit)
                Toast.makeText(requireContext(), R.string.connectedToGoogleFit, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onCreateFileResult(result: ActivityResult?) {
        result?.let {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                saveToFile(it.data?.data)
            }
        }
    }

    private fun onOpenFileResult(result: ActivityResult?) {
        result?.let {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                loadFromFile(it.data?.data)
            }
        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        when (resultCode) {
//            Activity.RESULT_OK -> when (requestCode) {
//                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE -> {
//                }
//                else -> {
//                    // Result wasn't from Google Fit
//                }
//            }
//            else -> {
//                // Permission not granted
//            }
//        }
//    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        requireActivity().menuInflater.inflate(R.menu.main_menu, menu)
        authMenuItem = menu.findItem(R.id.auth)
        googleFitMenuItem = menu.findItem(R.id.googleFit)

        if (FirebaseAuth.getInstance().currentUser != null) authMenuItem?.setTitle(R.string.signOut)

        account = GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions)
        if (GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            accessToGoogleFitGranted = true
            googleFitMenuItem?.setTitle(R.string.connectedToGoogleFit)
        }



        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        selectedMenuItem = item
        return when (item.itemId) {
            R.id.setGoal -> {
                if (entries.isEmpty()) {
                    Toast.makeText(context, resources.getString(R.string.readigFirst), Toast.LENGTH_SHORT).show()
                } else {
                    handleSetGoal()
                }
                true
            }
            R.id.settings -> {
                handleSettings()
                true
            }
            R.id.delete -> {
                handleDeleteAll()
                true
            }
            R.id.save -> {
                handleSave()
                true
            }
            R.id.load -> {
                openFile()
                true
            }
            R.id.units -> {
                handleUnits()
                true
            }
            R.id.googleFit -> {
                if (!accessToGoogleFitGranted) requestPermissions()
                true
            }
            R.id.auth -> {
                handleAuth()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun requestPermissions() {
//        GoogleSignIn.requestPermissions(this, GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, account, fitnessOptions)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .addExtension(fitnessOptions)
            .build()
        val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        googleFitAuthLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun handleAuth() {
        if (user == null) {
            val providers = listOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.PhoneBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build()
            )
            val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()
            signInLauncher.launch(signInIntent)
        } else {
            AuthUI.getInstance()
                .signOut(requireContext())
                .addOnCompleteListener {
                    Toast.makeText(requireContext(), resources.getString(R.string.signedOut), Toast.LENGTH_SHORT).show()
                    user = null
                }
            authMenuItem!!.title = resources.getString(R.string.signIn)
        }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            user = FirebaseAuth.getInstance().currentUser
            authMenuItem!!.title = resources.getString(R.string.signOut)
            buildAndShowSignedInMessage(user)
        } else {
            user = null
            Toast.makeText(requireContext(), resources.getString(R.string.errorSigningIn), Toast.LENGTH_SHORT).show()
            Log.d(weightFragmentTag, "onSignInResult: $response")
            authMenuItem!!.title = resources.getString(R.string.signIn)
        }
    }

    private fun buildAndShowSignedInMessage(user: FirebaseUser?) {
        val text = StringBuilder()
        text.append(resources.getString(R.string.signedInAs))
        text.append(" ")
        if (user!!.phoneNumber != null && user.phoneNumber!!.isNotEmpty()) text.append(user.phoneNumber)
        if (user.email != null && user.email!!.isNotEmpty()) text.append(user.email)
        Toast.makeText(requireContext(), text.toString(), Toast.LENGTH_SHORT).show()
    }

    private fun createFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "myWeightAndMeasurements.txt")
        }
        createFileLauncher.launch(intent)
    }

    private fun saveToFile(uri: Uri?) {
        try {
            val pfd = requireContext().contentResolver.openFileDescriptor(uri!!, "w")
            FileOutputStream(pfd!!.fileDescriptor).apply {
                write(gson.toJson(dataToSave).toByteArray())
                close()
            }
            dataToSave = null
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(WEIGHT_FRAGMENT_TAG, "saveToFile: $e")
        }
    }

    private fun loadFromFile(uri: Uri?) {
        val stringBuilder = StringBuilder()
        try {
            requireContext().contentResolver.openInputStream(uri!!).use { inputStream ->
                BufferedReader(
                    InputStreamReader(Objects.requireNonNull(inputStream))
                ).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                    val type = object : TypeToken<Data?>() {}.type
                    returnData = gson.fromJson(stringBuilder.toString(), type)
                    handleLoad(returnData)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun openFile() {
        AlertDialog.Builder(context, R.style.DefaultAlertDialogTheme)
            .setTitle(R.string.overwrite)
            .setIcon(R.drawable.ic_load)
            .setMessage(R.string.overwriteInfo)
            .setPositiveButton(R.string.yes) { _: DialogInterface?, _: Int ->
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/plain"
                }
                openFileLauncher.launch(intent)
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun handleUnits() {
        val units = arrayOf(resources.getString(R.string.kgs), resources.getString(R.string.lbs))
        var checkedItem = 0
        if (unit == resources.getString(R.string.lbs)) {
            checkedItem = 1
        }
        AlertDialog.Builder(context, R.style.DefaultAlertDialogTheme)
            .setTitle(resources.getString(R.string.selectUnits))
            .setIcon(R.drawable.ic_settings)
            .setSingleChoiceItems(units, checkedItem) { _: DialogInterface?, which: Int ->
                if (unit != units[which]) {
                    unit = units[which]
                    Utils.getInstance(requireContext()).setUnit(unit)
                    wasModified = true
                } else {
                    wasModified = false
                }
            }
            .setPositiveButton(R.string.ok) { _: DialogInterface?, _: Int ->
                if (entries.isNotEmpty() && wasModified) {
                    AlertDialog.Builder(context, R.style.DefaultAlertDialogTheme)
                        .setTitle(resources.getString(R.string.conversion))
                        .setIcon(R.drawable.ic_settings)
                        .setMessage(R.string.conversionMsg)
                        .setPositiveButton(R.string.yes) { _: DialogInterface?, _: Int -> convertValues() }
                        .setNegativeButton(R.string.no, null)
                        .show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun convertValues() {
        if (unit == resources.getString(R.string.lbs)) {
            for (e in entries) {
                e.y = e.y * kgToLbsMultiplier
            }
            convertMeasurements(cmToInMultiplier)
            multiplier = kgToLbsMultiplier
        } else {
            for (e in entries) {
                e.y = e.y / kgToLbsMultiplier
            }
            convertMeasurements(1 / cmToInMultiplier)
            multiplier = 1 / kgToLbsMultiplier
        }
        max *= multiplier
        min *= multiplier
        Utils.getInstance(requireContext()).setMax(max)
        Utils.getInstance(requireContext()).setMin(min)
        if (goal != 0f) {
            goal *= multiplier
            startingWeight *= multiplier
            Utils.getInstance(requireContext()).setStartingWeight(startingWeight)
            Utils.getInstance(requireContext()).setGoal(goal)
        }
        scaleYAxis(0f)
        viewModel.setDataset(ArrayList())
        viewModel.addDataset(newSet(entries))
        data = LineData(viewModel.dataset.value)
        binding.weightChart.data = data
        updateGoal()
        drawGoal(goal)
        binding.weightChart.invalidate()
    }

    private fun convertMeasurements(multiplier: Float) {
        val bodyParts = Utils.getInstance(requireContext()).allBodyParts ?: ArrayList()
        if (bodyParts.isNotEmpty()) for (b in bodyParts) {
            if (b.measurements.isNotEmpty()) for (m in b.measurements) {
                m.value = m.value * multiplier
            }
        }
        Utils.getInstance(requireContext()).updateBodyParts(bodyParts)
    }

    private fun handleDeleteAll() {
        AlertDialog.Builder(context, R.style.DefaultAlertDialogTheme)
            .setTitle(R.string.deleteAll)
            .setIcon(R.drawable.ic_delete)
            .setMessage(R.string.deleteAllMsg)
            .setPositiveButton(R.string.yes) { _: DialogInterface?, _: Int ->
                entries = ArrayList()
                notes = ArrayList()
                Utils.getInstance(requireContext()).updateNotes(notes)
                Utils.getInstance(requireContext()).updateEntries(entries)
                viewModel.setDataset(ArrayList())
                viewModel.addDataset(newSet(entries))
                data = LineData(viewModel.dataset.value)
                binding.weightChart.data = data
                binding.weightChart.axisLeft.removeAllLimitLines()
                Utils.getInstance(requireContext()).setGoal(0f)
                binding.weightChart.invalidate()
                updateCurrentWeight()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun handleLoad(savedData: Data?) {
        val serializableEntries = savedData!!.serializableEntries
        if (serializableEntries.isEmpty()) {
            Toast.makeText(context, R.string.noEntriesToLoad, Toast.LENGTH_SHORT).show()
        } else {
            refTime = serializableEntries[0].X.toLong()
            Utils.getInstance(requireContext()).setRefTime(refTime)
            serializableEntries.removeAt(0)
            entries = ArrayList()
            notes = ArrayList()
            for (se in serializableEntries) {
                entries.add(Entry(se.X, se.Y))
                notes.add(Note(se.X, se.note))
            }
            Utils.getInstance(requireContext()).updateNotes(notes)
            Utils.getInstance(requireContext()).updateEntries(entries)
            for (e in entries) {
                e.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_hexagon_icon)
            }
            viewModel.setDataset(ArrayList())
            viewModel.addDataset(newSet(entries))
            data = LineData(viewModel.dataset.value)
            binding.weightChart.data = data
            if (entries.isNotEmpty()) {
                max = entries.maxByOrNull { it.y }?.y ?: 100F
                min = entries.minByOrNull { it.y }?.y ?: 40F
            }
            updateCurrentWeight()
            viewModel.setOffset(1)
            Utils.getInstance(requireContext()).setMax(max)
            Utils.getInstance(requireContext()).setMin(min)
            scaleYAxis(0f)
            if (Utils.getInstance(requireContext()).goal != 0f) {
                clearGoal()
            }
        }
        if (savedData.bodyParts.isEmpty()) {
            Toast.makeText(context, R.string.noBodyPartsToLoad, Toast.LENGTH_SHORT).show()
        } else {
            Utils.getInstance(requireContext()).updateBodyParts(savedData.bodyParts)
        }
    }

    private fun handleSave() {
        if (entries.isEmpty() && Utils.getInstance(requireContext()).allBodyParts!!.isEmpty()) {
            Toast.makeText(requireContext(), R.string.noDataToSave, Toast.LENGTH_SHORT).show()
        }
        val serializableEntries = ArrayList<SerializableEntry>()
        for (i in entries.indices) {
            serializableEntries.add(SerializableEntry(entries[i].x, entries[i].y, notes[i].note))
        }
        serializableEntries.add(0, SerializableEntry(refTime.toFloat(), 0F, "refTime"))
        dataToSave = Data(serializableEntries, Utils.getInstance(requireContext()).allBodyParts!!)
        createFile()
    }

    private fun handleSettings() {
        AlertDialog.Builder(context, R.style.DefaultAlertDialogTheme)
            .setTitle(R.string.settings)
            .setIcon(R.drawable.ic_settings)
            .setMultiChoiceItems(settingsNames, areEnabled) { _: DialogInterface?, which: Int, isChecked: Boolean -> areEnabled[which] = isChecked }
            .setPositiveButton(R.string.save) { _: DialogInterface?, _: Int ->
                binding.weightChart.apply {
                    axisLeft.setDrawGridLines(areEnabled[1])
                    xAxis.setDrawGridLines(areEnabled[0])
                    data.isHighlightEnabled = areEnabled[2]
                    invalidate()
                }
                Utils.getInstance(requireContext()).apply {
                    setSetting1(areEnabled[0])
                    setSetting2(areEnabled[1])
                    setSetting3(areEnabled[2])
                }

            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun handleSetGoal() {
        AlertDialog.Builder(context, R.style.DefaultAlertDialogTheme)
            .setTitle(R.string.startingWeight)
            .setMessage(R.string.startingWeightMsg)
            .setIcon(R.drawable.ic_info)
            .setPositiveButton(R.string.ok) { _: DialogInterface?, _: Int ->
                val inflater = layoutInflater
                val dialogView = inflater.inflate(R.layout.til_dialog, null)
                val exportName = dialogView.findViewById<EditText>(R.id.edtDialog).apply {
                    requestFocus()
                    filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(3, 1))
                    inputType = InputType.TYPE_CLASS_NUMBER
                    inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
                    keyListener = DigitsKeyListener.getInstance(Locale.US, false, true)
                }
                val exportTil: TextInputLayout = dialogView.findViewById(R.id.tilDialog)
                exportTil.setHint(R.string.weight)

                AlertDialog.Builder(context, R.style.DefaultAlertDialogTheme)
                    .setTitle(R.string.setNewGoal)
                    .setIcon(R.drawable.ic_goal)
                    .setView(dialogView)
                    .setPositiveButton(R.string.set) { _: DialogInterface?, _: Int ->
                        hideKeyboardForced(requireActivity())
                        when {
                            exportName.text.toString().isEmpty() -> {
                                Toast.makeText(context, R.string.insertWeight, Toast.LENGTH_SHORT).show()
                                handleSetGoal()
                            }
                            exportName.text.toString().toFloat() == 0f -> {
                                Toast.makeText(context, R.string.goalZero, Toast.LENGTH_SHORT).show()
                                handleSetGoal()
                            }
                            else -> {
                                goal = exportName.text.toString().toFloat()
                                setGoal(goal)
                                Utils.getInstance(requireContext()).apply {
                                    setStartingDate(startingDate)
                                    setStartingWeight(startingWeight)
                                    setGoal(goal)
                                }

                                setGoal(exportName.text.toString().toFloat())
                            }
                        }
                    }
                    .setNegativeButton(R.string.cancel) { _: DialogInterface?, _: Int -> hideKeyboardForced(requireActivity()) }
                    .show()
                showKeyboard(requireActivity())
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun handleAdding() {
        if (binding.edtWeight.text.toString().isEmpty()) {
            Toast.makeText(context, resources.getString(R.string.insertWeight), Toast.LENGTH_SHORT).show()
            Helpers.shake(binding.imgWeight)
            binding.imgWeight.setImageResource(R.drawable.ic_hexagon_single_red)
        } else {
            hideKeyboard(requireActivity())
            isBlurred = Helpers.resetLayoutAlpha(binding.goalDetails)
            binding.weightChart.alpha = 1.0f
            setBack = when (binding.spinnerWhen.selectedItemPosition) {
                1 -> 1
                2 -> 2
                else -> 0
            }
            binding.input.visibility = View.GONE
//            binding.addEntry.visibility = View.VISIBLE
            binding.imgWeight.setImageResource(R.drawable.ic_hexagon_single_empty)
            val time: Float
            if (refTime == 0L || entries.isEmpty()) {
                refTime = System.currentTimeMillis() / 1000
                Utils.getInstance(requireContext()).setRefTime(refTime)
                time = 0f
            } else {
                time = (System.currentTimeMillis() / 1000 - Helpers.daysToSeconds(setBack) - refTime).toFloat()
            }
            if (time < 0) {
                Toast.makeText(context, resources.getString(R.string.notBeforeFirst), Toast.LENGTH_LONG).show()
            } else {
                val newEntry = Entry(time, binding.edtWeight.text.toString().toFloat())
                val newNote: Note = if (binding.edtNote.text.toString().isEmpty()) {
                    Note(time, "---")
                } else {
                    Note(time, binding.edtNote.text.toString())
                }
                if (entries.isNotEmpty()) {
                    if (time < entries[entries.size - 1].x) {
                        addEntryOutsideOrder(newEntry, newNote)
                    } else {
                        addEntryOrdered(newEntry, newNote)
                        updateGoal()
                    }
                } else {
                    addEntryOrdered(newEntry, newNote)
                }
                if (entries.size == 1) {
                    max = entries[0].y
                    min = max
                    Utils.getInstance(requireContext()).setMax(max)
                    Utils.getInstance(requireContext()).setMin(min)
                } else {
                    scaleYAxis(newEntry.y)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    private fun insertToGoogleFit(weight: Float) {
        val account = GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions)
        val timeStamp = LocalDateTime.now().atZone(ZoneId.systemDefault())
        val dataSource = DataSource.Builder()
            .setAppPackageName(requireContext())
            .setDataType(DataType.TYPE_WEIGHT)
            .setStreamName("$weightFragmentTag - weight")
            .setType(DataSource.TYPE_RAW)
            .build()
        val dataPoint =
            DataPoint.builder(dataSource)
                .setField(Field.FIELD_WEIGHT, weight)
                .setTimestamp(timeStamp.toEpochSecond(), TimeUnit.SECONDS)
                .build()
        val dataSet = DataSet.builder(dataSource)
            .add(dataPoint)
            .build()
        Fitness.getHistoryClient(requireContext(), account)
            .insertData(dataSet)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), resources.getString(R.string.addedToGoogleFit), Toast.LENGTH_SHORT).show()
                Log.i(weightFragmentTag, resources.getString(R.string.addedToGoogleFit))
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), resources.getString(R.string.problemAddingToGoogleFit), Toast.LENGTH_SHORT).show()
                Log.w(weightFragmentTag, resources.getString(R.string.problemAddingToGoogleFit), e)
            }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews()
        user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            buildAndShowSignedInMessage(user)
        }
//        viewModel = ViewModelProvider(requireActivity()).get(WeightFragmentViewModel::class.java)

        unit = Utils.getInstance(requireContext()).unit
        refTime = Utils.getInstance(requireContext()).refTime
        areEnabled[0] = Utils.getInstance(requireContext()).setting1
        areEnabled[1] = Utils.getInstance(requireContext()).setting2
        areEnabled[2] = Utils.getInstance(requireContext()).setting3
        settingsNames[0] = resources.getString(R.string.drawVerticalGrid)
        settingsNames[1] = resources.getString(R.string.drawHorizontalGrid)
        settingsNames[2] = resources.getString(R.string.drawHighlight)
        notes = Utils.getInstance(requireContext()).allNotes!!
        entries = Utils.getInstance(requireContext()).allEntries!!
        max = Utils.getInstance(requireContext()).max
        min = Utils.getInstance(requireContext()).min
        for (e in entries) {
            e.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_hexagon_icon)
        }
        goal = Utils.getInstance(requireContext()).goal
        startingDate = Utils.getInstance(requireContext()).startingDate
        startingWeight = Utils.getInstance(requireContext()).startingWeight

        viewModel.offset.observe(requireActivity()) {
            if (isAdded) updateLastProgress(it)
        }

        viewModel.addValueClicked.observe(requireActivity()) { clicked ->
            if (clicked && this.isResumed) {
                viewModel.setInputOngoing(true)
                binding.input.visibility = View.VISIBLE
//                binding.addEntry.visibility = View.GONE
                isBlurred = Helpers.setLayoutAlpha(binding.goalDetails)
                binding.weightChart.alpha = 0.3f
                binding.edtWeight.requestFocus()
                requireActivity().currentFocus?.let { view ->
                    val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.hideSoftInputFromWindow(view.windowToken, 0)
                }
                viewModel.setAddValueClicked(false)
            }
        }

        binding.imgDelete.setOnClickListener {
            AlertDialog.Builder(context, R.style.DefaultAlertDialogTheme)
                .setTitle(R.string.deleting)
                .setIcon(R.drawable.ic_delete)
                .setMessage(R.string.deletingMsg)
                .setPositiveButton(R.string.yes) { _: DialogInterface?, _: Int ->
//                    binding.addEntry.show()
                    handleDeleteEntry()
                    isBlurred = Helpers.resetLayoutAlpha(binding.goalDetails)
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
        binding.imgAccept.setOnClickListener { handleAdding() }
        val markerImage = MarkerImage(context, R.drawable.ic_hexagon_marker)
        markerImage.setOffset(-13f, -13f)
        binding.weightChart.apply {
            setNoDataText("")
            renderer.paintRender.setShadowLayer(3f, -4f, 5f, Color.BLACK)
            axisRight.isEnabled = false
            legend.isEnabled = false
            description.isEnabled = false
            xAxis.apply {
                labelCount = 4
                position = XAxis.XAxisPosition.BOTTOM
                textColor = ContextCompat.getColor(requireContext(), R.color.white)
                axisLineWidth = 2f
                setDrawGridLines(areEnabled[0])
                valueFormatter = LineChartXAxisValueFormatter(refTime)
            }
            axisLeft.apply {
                setDrawGridLines(areEnabled[1])
                textColor = ContextCompat.getColor(requireContext(), R.color.lime_500)
                textSize = 15f
                typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                axisLineWidth = 2f
            }
            marker = markerImage
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry, h: Highlight) {
                    markedEntry = e
                    if (!viewModel.inputOngoing.value!!) {
//                        binding.addEntry.visibility = View.GONE
                        binding.details.visibility = View.VISIBLE
                        if (!isBlurred) isBlurred = Helpers.setLayoutAlpha(binding.goalDetails)
                        binding.txtWeight.text = e.y.toPrettyString(unit)
                        timeMilliseconds = Date((e.x.toLong() + refTime) * 1000)
                        binding.txtDate.text = dateTimeFormatWithHrs.format(timeMilliseconds)
                        for (i in entries.indices) {
                            if (entries[i].x == e.x) {
                                binding.txtNote.text = notes[i].note
                            }
                        }
                    }
                }

                override fun onNothingSelected() {
                    if (!viewModel.inputOngoing.value!!) {
                        binding.details.visibility = View.GONE
                        isBlurred = Helpers.resetLayoutAlpha(binding.goalDetails)
//                        binding.addEntry.visibility = View.VISIBLE
                    }
                }
            })

        }
        scaleYAxis(0f)
        lifecycleScope.launch {
            updateCurrentWeight()
            viewModel.setOffset(1)
            setGoal(goal)
            updateGoal()
            viewModel.setDataset(ArrayList())
            viewModel.addDataset(newSet(entries))
            data = LineData(viewModel.dataset.value)
            binding.weightChart.data = data
            binding.progressBar.visibility = View.GONE
            setXAxisRange()
            calculateWeeklyValues(binding.weightChart.data)
        }
        binding.imgClose.setOnClickListener {
            binding.details.visibility = View.GONE
            isBlurred = Helpers.resetLayoutAlpha(binding.goalDetails)
//            binding.addEntry.show()
        }
        binding.imgCloseInput.setOnClickListener {
            hideKeyboard(requireActivity())
            isBlurred = Helpers.resetLayoutAlpha(binding.goalDetails)
            binding.weightChart.alpha = 1.0f
            binding.input.visibility = View.GONE
//            binding.addEntry.show()
            viewModel.setInputOngoing(false)
        }
        binding.imgSwipe.setOnTouchListener(object : OnSwipeListener(context) {
            override fun onSwipeRight() {
                viewModel.offset.value?.let { offset ->
                    if (entries.size > 1 && offset < entries.size - 1) {
                        viewModel.setOffset(offset.inc())
                        ObjectAnimator.ofFloat(binding.txtProgress, "X", binding.txtProgress.x - 40f, binding.txtProgress.x).apply {
                            duration = 200
                            start()
                        }
                    }
                }
            }

            override fun onSwipeLeft() {
                viewModel.offset.value?.let { offset ->
                    if (entries.size > 2 && offset > 1) {
                        viewModel.setOffset(offset.dec())
                        ObjectAnimator.ofFloat(binding.txtProgress, "X", binding.txtProgress.x + 40f, binding.txtProgress.x).apply {
                            duration = 200
                            start()
                        }
                    }
                }
            }

            override fun onSwipeTop() {
                viewModel.offset.value?.let { offset ->
                    if (entries.size > 2 && offset != entries.size - 1) {
                        viewModel.setOffset(entries.size - 1)
                        ObjectAnimator.ofFloat(binding.txtProgress, "Y", binding.txtProgress.y + 40f, binding.txtProgress.y).apply {
                            duration = 200
                            start()
                        }
                    }
                }
            }


            override fun onSwipeBottom() {
                viewModel.offset.value?.let { offset ->
                    if (entries.size > 2 && offset != 1) {
                        viewModel.setOffset(1)
                        ObjectAnimator.ofFloat(binding.txtProgress, "Y", binding.txtProgress.y - 40f, binding.txtProgress.y).apply {
                            duration = 200
                            start()
                        }
                    }
                }
            }
        })
        super.onViewCreated(view, savedInstanceState)
    }

    private fun calculateWeeklyValues(data: LineData?) {
        data?.let {
            val count = calculateOutputSize()
            val xs = dataToArrayOfCoordinates(data, "x", count)
            val ys = dataToArrayOfCoordinates(data, "y", count)
            calculateOutputSize()
            Helpers.regressionSlope(xs, ys)?.let {
                binding.txtWeekly.text = if (it > 1 && it > -1) it.toPrettyString(unit) else it.toPrettyStringDecimals(unit)
                binding.txtWeeklyPercentage.text = (it / entries.last().y * 100).toPrettyString("%")
            } ?: {
                binding.txtWeekly.blank()
                binding.txtWeeklyPercentage.blank()
            }
        }
    }

    private fun calculateOutputSize(): Int {
        return if (entries.size < 5) entries.size else {
            val intervals = FloatArray(4)
            for (i in intervals.indices) {
                intervals[i] = entries[entries.lastIndex - i].x - entries[entries.lastIndex - i - 1].x
            }
            val averageInterval = secsToDays(intervals.average().toFloat())
            if (averageInterval > 2) 3 else 5
        }
    }

    private fun setXAxisRange() {
        binding.weightChart.apply {
            val visibleEntries = 20
            if (entries.size > visibleEntries) {
                val range = entries.last().x - entries[entries.lastIndex - visibleEntries + 1].x
                setVisibleXRangeMaximum(range)
                moveViewToX(entries[entries.lastIndex - visibleEntries + 1].x)
                moveViewToAnimated(entries[entries.lastIndex - visibleEntries + 1].x, 0f, binding.weightChart.axisLeft.axisDependency, 500)
            } else {
                fitScreen()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWeightBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun updateLastProgress(offset: Int) {
        if (entries.size > 1) {
            binding.txtProgress.text = (entries[entries.size - 1].y - entries[entries.size - 1 - offset].y).toPrettyString(unit)
            val period = entries[entries.size - 1].x - entries[entries.size - 1 - offset].x
            var unit = resources.getString(R.string.days)
            if (secsToDays(period) == 1) unit = resources.getString(R.string.day)
            val periodTxt: String = secsToDays(period).toString()
            binding.txtProgressDate.text = String.format("%s%s", periodTxt, unit)
        } else {
            binding.txtProgress.blank()
        }
    }

    private fun setGoal(weight: Float) {
        goal = weight
        if (goal == 0f) {
            clearGoal()
        } else {
            startingWeight = entries[entries.size - 1].y
            startingDate = entries[entries.size - 1].x
            drawGoal(goal)
            binding.txtStartingWeight.text = startingWeight.toPrettyString(unit)
            binding.txtGoal.text = goal.toPrettyString(unit)
            binding.txtStartingDate.text = dateTimeFormat.format((startingDate + refTime) * 1000)
        }
    }

    private fun updateGoal() {
        if (goal != 0f && entries.size > 1) {
            binding.txtProgressGoal.text = (entries[entries.size - 1].y - startingWeight).toPrettyString(unit)
        }
    }

    private fun clearGoal() {
        binding.apply {
            txtGoal.blank()
            txtStartingWeight.blank()
            txtStartingDate.blank()
            txtProgressGoal.blank()
            weightChart.axisLeft.removeAllLimitLines()
        }
        Utils.getInstance(requireContext()).setGoal(0F)
    }

    private fun updateCurrentWeight() {
        binding.apply {
            if (entries.isNotEmpty()) {
                txtCurrent.text = entries[entries.size - 1].y.toPrettyString(unit)
                txtCurrentDate.text = dateTimeFormat.format((entries[entries.size - 1].x + refTime) * 1000)
            } else {
                txtCurrent.blank()
                txtCurrentDate.blank()
                txtProgress.blank()
                txtProgressDate.blank()
                txtWeekly.blank()
                txtWeeklyPercentage.blank()
            }
        }
    }

    private fun addEntryOutsideOrder(newEntry: Entry, newNote: Note) {
        for (i in entries.size - 1 downTo 1) {
            if (entries[i].x < newEntry.x) {
                Utils.getInstance(requireContext()).addToAllEntriesOrdered(newEntry, i + 1)
                Utils.getInstance(requireContext()).addToAllNotesOrdered(newNote, i + 1)
                newEntry.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_hexagon_icon)
                entries.add(i + 1, newEntry)
                notes.add(i + 1, newNote)
                binding.weightChart.notifyDataSetChanged()
                updateGoal()
                break
            }
        }
    }

    private fun addEntryOrdered(newEntry: Entry, newNote: Note) {
        Utils.getInstance(requireContext()).addToAllEntries(newEntry)
        Utils.getInstance(requireContext()).addToAllNotes(newNote)
        notes.add(newNote)
        addEntry(newEntry)
        viewModel.setInputOngoing(false)
        if (!binding.txtCurrent.isShown) binding.txtCurrent.visibility = View.VISIBLE
        updateCurrentWeight()
        viewModel.setOffset(1)
        hideKeyboard(requireActivity())
    }

    private fun handleDeleteEntry() {
        var lastEntry = false
        val data = binding.weightChart.data
        binding.weightChart.notifyDataSetChanged()
        for (i in entries.indices.reversed()) {
            if (markedEntry!!.x == entries[i].x) {
                Utils.getInstance(requireContext()).removeFromAllNotes(notes[i])
                notes.removeAt(i)
                if (i == entries.size - 1) {
                    lastEntry = true
                }
            }
        }
        Utils.getInstance(requireContext()).removeFromAllEntries(markedEntry!!)
        data.removeEntry(markedEntry, 0)
        if (lastEntry) {
            updateCurrentWeight()
            viewModel.setOffset(1)
        }
        if (goal != 0f && markedEntry!!.y == startingWeight && markedEntry!!.x == startingDate) {
            clearGoal()
        }
        binding.details.visibility = View.GONE
        if (entries.isNotEmpty()) {
            if (markedEntry!!.y == max) {
                max = entries.maxByOrNull { it.y }?.y ?: 100F
                Utils.getInstance(requireContext()).setMax(max)
                scaleYAxis(0f)
            }
            if (markedEntry!!.y == min) {
                min = entries.minByOrNull { it.y }?.y ?: 40F
                Utils.getInstance(requireContext()).setMin(min)
                scaleYAxis(0f)
            }
        } else {
            clearGoal()
            updateCurrentWeight()
            binding.weightChart.axisLeft.apply {
                resetAxisMaximum()
                resetAxisMinimum()
            }
        }
        binding.weightChart.invalidate()
    }

    private fun scaleYAxis(newY: Float) {
        if (newY != 0f) {
            when {
                entries.size == 1 -> {
                    min = entries[0].y
                    max = entries[0].y
                }
                newY > max -> {
                    max = newY
                    Utils.getInstance(requireContext()).setMax(max)
                }
                newY < min -> {
                    min = newY
                    Utils.getInstance(requireContext()).setMin(min)
                }
            }
        }
        binding.weightChart.apply {
            axisLeft.axisMaximum = 1.1f * max
            axisLeft.axisMinimum = 0.8f * min
            invalidate()
        }
    }

    private fun addEntry(entry: Entry) {
        val data = binding.weightChart.data
        entry.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_hexagon_icon)
        if (accessToGoogleFitGranted) insertToGoogleFit(entry.y)
        data.addEntry(entry, 0)
        data.notifyDataChanged()
        scaleYAxis(entry.y)
        binding.weightChart.notifyDataSetChanged()
        binding.weightChart.moveViewToAnimated(entries[entries.size - 1].x, 0f, binding.weightChart.axisLeft.axisDependency, 500)
        calculateWeeklyValues(data)
    }

    private fun drawGoal(weight: Float) {
        val ll1 = LimitLine(weight, "").apply {
            lineWidth = 0.5f
            lineColor = ContextCompat.getColor(requireContext(), R.color.lime_700)
            enableDashedLine(20f, 10f, 0f)
            labelPosition = LimitLine.LimitLabelPosition.LEFT_TOP
        }
        binding.weightChart.axisLeft.apply {
            removeAllLimitLines()
            addLimitLine(ll1)
        }
    }

    private fun newSet(yValues: ArrayList<Entry>?): LineDataSet {
        return LineDataSet(yValues, "Dataset1").apply {
            highLightColor = ContextCompat.getColor(requireContext(), R.color.lime_500)
            setDrawHorizontalHighlightIndicator(areEnabled[2])
            setDrawVerticalHighlightIndicator(areEnabled[2])
            highlightLineWidth = 0f
            fillAlpha = 110
            color = ContextCompat.getColor(requireContext(), R.color.lime_500)
            circleRadius = 2f
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.lime_700))
            circleHoleColor = ContextCompat.getColor(requireContext(), R.color.grey_500)
            circleHoleRadius = 1f
            setDrawValues(false)
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.grey_200)
            lineWidth = 4f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.2f
        }
    }

    private fun initViews() {
        binding.apply {
            edtWeight.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(3, 1))
            weightChart.isDragEnabled = true
            weightChart.isScaleYEnabled = false
            val color = ContextCompat.getColor(requireContext(), R.color.white)
            txtWeekly.setTextColor(color)
            txtStartingDate.setTextColor(color)
            txtStartingWeight.setTextColor(color)
            txtGoal.setTextColor(color)
            txtProgressGoal.setTextColor(color)
            txtProgress.setTextColor(color)
            edtNote.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    handleAdding()
                }
                true
            }
        }
    }
}
