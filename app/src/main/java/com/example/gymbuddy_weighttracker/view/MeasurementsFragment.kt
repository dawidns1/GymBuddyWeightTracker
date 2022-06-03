package com.example.gymbuddy_weighttracker.view

import android.app.AlertDialog
import android.content.DialogInterface
import android.icu.util.Calendar
import android.os.Bundle
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymbuddy_weighttracker.R
import com.example.gymbuddy_weighttracker.databinding.FragmentMeasurementsBinding
import com.example.gymbuddy_weighttracker.helpers.Helpers.toggleVisibility
import com.example.gymbuddy_weighttracker.helpers.Utils
import com.example.gymbuddy_weighttracker.model.BodyPart
import com.example.gymbuddy_weighttracker.model.Measurement
import com.example.gymbuddy_weighttracker.recyclerViewAdapters.BodyPartsRVAdapter
import com.example.gymbuddy_weighttracker.recyclerViewAdapters.MeasurementsRVAdapter
import com.example.gymbuddy_weighttracker.viewModel.MainViewModel
import com.google.android.material.textfield.TextInputLayout
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MeasurementsFragment : Fragment() {
    private lateinit var bodyParts: ArrayList<BodyPart>
    private lateinit var adapter: BodyPartsRVAdapter
    private val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
    private var weightUnit: String? = null
    private var unit: String? = null
    private lateinit var binding: FragmentMeasurementsBinding
    private val viewModel: MainViewModel by activityViewModels()
    private val measurementsTag = "MeasurementsFragment"

    override fun onPause() {
        Utils.getInstance(requireContext()).updateBodyParts(bodyParts)
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMeasurementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        weightUnit = Utils.getInstance(requireContext()).unit
        unit = if (weightUnit == " kg") " cm" else " in"
        bodyParts = Utils.getInstance(requireContext()).allBodyParts ?: ArrayList()
        adapter = BodyPartsRVAdapter(
            requireContext(),
            unit!!,
            menuListener = { position, v -> onMenuClick(position, v) },
            addListener = { position, measurementsRV -> onAddClick(position, measurementsRV) },
            deleteListener = { parentPosition, childPosition, adapterInner, v -> onDeleteClick(parentPosition, childPosition, adapterInner, v) }).apply {
            setBodyParts(bodyParts)
        }
        viewModel.addValueClicked.observe(requireActivity()) { clicked ->
            if (clicked && this.isResumed) {
                if (bodyParts.isNotEmpty()) {
                    handleAddAllMeasurements(0)
                } else {
                    Toast.makeText(requireContext(), R.string.longClickToAddBodyPart, Toast.LENGTH_SHORT).show()
                }
                viewModel.setAddValueClicked(false)
            }
        }
        viewModel.addValueLongClicked.observe(requireActivity()) {clicked ->
            if (clicked && this.isResumed) {
                handleAddBodyPart()
                viewModel.setAddValueClicked(false)
            }
        }
        binding.bodyPartsRV.adapter = adapter
        binding.bodyPartsRV.layoutManager = LinearLayoutManager(context)
        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding.bodyPartsRV)

        binding.bodyPartsRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var firstVisiblePosition: Int? = null
            var lastVisiblePosition: Int? = null
            var visibilityCondition = false
            var scrollableCondition = false
            lateinit var layoutManager: LinearLayoutManager
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                layoutManager = recyclerView.layoutManager as LinearLayoutManager
                firstVisiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition()
                visibilityCondition = firstVisiblePosition == 0 && lastVisiblePosition == bodyParts.size - 1
                scrollableCondition = !recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE
                viewModel.setHideFAB(scrollableCondition && !visibilityCondition)
//                binding.addBodyPart.toggleVisibilityEFAB(!scrollableCondition || visibilityCondition)
                binding.filler.toggleVisibility(scrollableCondition && !visibilityCondition)
            }
        })

        binding.slider.apply {
            if (bodyParts.isNotEmpty() && bodyParts[0].measurements.size > 2) {
                valueFrom = 0f
                valueTo = (bodyParts[0].measurements.size - 1).toFloat()
                stepSize = 1f
                addOnChangeListener { _, value, _ ->
                    for (i in bodyParts.indices) {
                        val viewHolder = binding.bodyPartsRV.findViewHolderForAdapterPosition(i)
                        val child = viewHolder?.itemView?.findViewById<RecyclerView>(R.id.measurementsRV)
                        child?.smoothScrollToPosition(value.toInt())
                    }
                }
            } else {
                this.visibility = View.GONE
            }
        }
    }

    private fun handleAddAllMeasurements(iteration: Int) {
        val inflater = layoutInflater
        val viewHolder = binding.bodyPartsRV.findViewHolderForAdapterPosition(iteration)
        val measurementsRV = viewHolder?.itemView?.findViewById<RecyclerView>(R.id.measurementsRV)
        measurementsRV?.let {
            val adapterInner = measurementsRV.adapter
            val dialogView = inflater.inflate(R.layout.til_dialog, null)
            val edtMeasurement = dialogView.findViewById<EditText>(R.id.edtDialog).apply {
                requestFocus()
                inputType = InputType.TYPE_CLASS_NUMBER
                inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
                keyListener = DigitsKeyListener.getInstance(Locale.US, false, true)
            }
            val exportTil: TextInputLayout = dialogView.findViewById(R.id.tilDialog)
            exportTil.setHint(R.string.measurement)
            AlertDialog.Builder(context, R.style.DefaultAlertDialogTheme)
                .setTitle(resources.getString(R.string.newMeasurement) + bodyParts[iteration].name)
                .setIcon(R.drawable.ic_add_body_part)
                .setView(dialogView)
                .setPositiveButton(R.string.add) { _: DialogInterface?, _: Int ->
                    if (edtMeasurement.text.toString().isNotEmpty()) {
                        addValue(iteration, adapterInner, measurementsRV, edtMeasurement.text.toString().toFloat())
                        if (iteration + 1 < bodyParts.size) handleAddAllMeasurements(iteration + 1)
                    } else {
                        Toast.makeText(context, R.string.errorValue, Toast.LENGTH_SHORT).show()
                        handleAddAllMeasurements(iteration)
                    }
                }
                .setNegativeButton(R.string.skip) { _: DialogInterface?, _: Int ->
                    addValue(iteration, adapterInner, measurementsRV)
                    if (iteration + 1 < bodyParts.size) handleAddAllMeasurements(iteration + 1)
                }
                .setNeutralButton(R.string.cancel) { _: DialogInterface?, _: Int ->
                    if (iteration > 0) {
                        for (i in iteration until bodyParts.size) {
                            val cancelViewHolder = binding.bodyPartsRV.findViewHolderForAdapterPosition(i)
                            val cancelMeasurementsRV = cancelViewHolder?.itemView?.findViewById<RecyclerView>(R.id.measurementsRV)
                            cancelMeasurementsRV?.let {
                                val cancelAdapterInner = cancelMeasurementsRV.adapter
                                addValue(i, cancelAdapterInner, cancelMeasurementsRV)
                            }
                        }
                    }
                }
                .show()
        }
    }

    private fun addValue(position: Int, adapterInner: RecyclerView.Adapter<RecyclerView.ViewHolder>?, measurementsRV: RecyclerView, value: Float = 0f) {
        bodyParts[position].measurements.add(0, Measurement(value, df.format(Calendar.getInstance().time)))
        bodyParts[position].measurements[0].date = df.format(Calendar.getInstance().time)
        adapterInner!!.notifyItemInserted(0)
        if (bodyParts[position].measurements.size == 1 || bodyParts[position].measurements.size == 0) adapter.notifyItemChanged(position)
        measurementsRV.smoothScrollToPosition(0)
    }

    private var simpleCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or
                ItemTouchHelper.START or ItemTouchHelper.END, 0
    ) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition
            Collections.swap(bodyParts, fromPosition, toPosition)
            adapter.notifyItemMoved(fromPosition, toPosition)
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
    }

    private fun handleAddBodyPart() {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.til_dialog, null)
        val exportName = dialogView.findViewById<EditText>(R.id.edtDialog)
        val exportTil: TextInputLayout = dialogView.findViewById(R.id.tilDialog)
        exportTil.setHint(R.string.name)
        exportName.requestFocus()
        AlertDialog.Builder(context, R.style.DefaultAlertDialogTheme)
            .setTitle(R.string.addNewBodyPart)
            .setIcon(R.drawable.ic_add_body_part)
            .setView(dialogView)
            .setPositiveButton(R.string.add) { _: DialogInterface?, _: Int ->
                if (exportName.text.toString().isNotEmpty()) {
                    bodyParts.add(BodyPart(exportName.text.toString()))
                    adapter.notifyItemInserted(bodyParts.size - 1)
                } else {
                    Toast.makeText(context, R.string.errorBodyPart, Toast.LENGTH_SHORT).show()
                    handleAddBodyPart()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun onAddClick(positionRV: Int, measurementsRV: RecyclerView) {
        val inflater = layoutInflater
        val adapterInner = measurementsRV.adapter
        val dialogView = inflater.inflate(R.layout.til_dialog, null)
        val edtMeasurement = dialogView.findViewById<EditText>(R.id.edtDialog).apply {
            requestFocus()
            inputType = InputType.TYPE_CLASS_NUMBER
            inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
            keyListener = DigitsKeyListener.getInstance(Locale.US, false, true)
        }
        val exportTil: TextInputLayout = dialogView.findViewById(R.id.tilDialog)
        exportTil.setHint(R.string.measurement)
        AlertDialog.Builder(context, R.style.DefaultAlertDialogTheme)
            .setTitle(resources.getString(R.string.newMeasurement) + bodyParts[positionRV].name)
            .setIcon(R.drawable.ic_add_body_part)
            .setView(dialogView)
            .setPositiveButton(R.string.add) { _: DialogInterface?, _: Int ->
                if (edtMeasurement.text.toString().isNotEmpty()) {
                    bodyParts[positionRV].measurements.add(0, Measurement(edtMeasurement.text.toString().toFloat(), df.format(Calendar.getInstance().time)))
//                    bodyParts[positionRV].measurements[0].date = df.format(Calendar.getInstance().time)
                    adapterInner!!.notifyItemInserted(0)
                    if (bodyParts[positionRV].measurements.size == 1 || bodyParts[positionRV].measurements.size == 0) adapter.notifyItemChanged(positionRV)
                    measurementsRV.smoothScrollToPosition(0)
                } else {
                    Toast.makeText(context, R.string.errorValue, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun onMenuClick(positionRV: Int, v: View) {
        val popupMenu = PopupMenu(requireContext(), v)
        popupMenu.inflate(R.menu.popup_menu_body_part)
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menuDeleteBodyPart -> {
                    AlertDialog.Builder(context, R.style.DefaultAlertDialogTheme)
                        .setMessage(R.string.sureDeleteThisBodyPart)
                        .setIcon(R.drawable.ic_delete)
                        .setPositiveButton(R.string.yes) { _, _ ->
                            bodyParts.remove(bodyParts[positionRV])
                            adapter.notifyItemRemoved(positionRV)
                            adapter.notifyItemRangeChanged(positionRV, bodyParts.size - 2 - positionRV)
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                    return@setOnMenuItemClickListener true
                }
                R.id.menuEditBodyPart -> {
                    val inflater = layoutInflater
                    val dialogView = inflater.inflate(R.layout.til_dialog, null)
                    val exportName = dialogView.findViewById<EditText>(R.id.edtDialog)
                    val exportTil: TextInputLayout = dialogView.findViewById(R.id.tilDialog)
                    exportTil.setHint(R.string.newName)
                    exportName.setText(bodyParts[positionRV].name)
                    exportName.requestFocus()
                    AlertDialog.Builder(context, R.style.DefaultAlertDialogTheme)
                        .setTitle(R.string.editBodyPart)
                        .setIcon(R.drawable.ic_settings)
                        .setView(dialogView)
                        .setPositiveButton(R.string.save) { _: DialogInterface?, _: Int ->
                            bodyParts[positionRV].name = exportName.text.toString()
                            adapter.notifyItemChanged(positionRV)
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
        popupMenu.show()
    }

    private fun onDeleteClick(positionParent: Int, positionChild: Int, adapter: MeasurementsRVAdapter?, view: View) {
        val inflater = layoutInflater
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.inflate(R.menu.popup_menu_measurement)
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menuEditMeasurement -> {
                    val dialogView = inflater.inflate(R.layout.til_dialog, null)
                    val edtMeasurement = dialogView.findViewById<EditText>(R.id.edtDialog).apply {
                        requestFocus()
                        inputType = InputType.TYPE_CLASS_NUMBER
                        inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
                        keyListener = DigitsKeyListener.getInstance(Locale.US, false, true)
                    }
                    val exportTil: TextInputLayout = dialogView.findViewById(R.id.tilDialog)
                    exportTil.setHint(R.string.measurement)
                    AlertDialog.Builder(context, R.style.DefaultAlertDialogTheme)
                        .setTitle(resources.getString(R.string.editMeasurement) + bodyParts[positionParent].name)
                        .setIcon(R.drawable.ic_add_body_part)
                        .setView(dialogView)
                        .setPositiveButton(R.string.add) { _: DialogInterface?, _: Int ->
                            if (edtMeasurement.text.toString().isNotEmpty()) {
                                bodyParts[positionParent].measurements[positionChild].value = edtMeasurement.text.toString().toFloat()
                                adapter!!.notifyItemChanged(positionChild)
                                this.adapter.notifyItemChanged(positionParent)
                            } else {
                                Toast.makeText(context, R.string.errorValue, Toast.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                    return@setOnMenuItemClickListener true
                }
                R.id.menuDeleteMeasurement -> {
                    AlertDialog.Builder(context, R.style.DefaultAlertDialogTheme)
                        .setMessage(R.string.sureDeleteThisMeasurement)
                        .setIcon(R.drawable.ic_delete)
                        .setPositiveButton(R.string.yes) { _, _ ->
                            bodyParts[positionParent].measurements[positionChild].value = 0f
                            adapter!!.notifyItemChanged(positionChild)
                            this.adapter.notifyItemChanged(positionParent)
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                    return@setOnMenuItemClickListener true
                }
                R.id.menuDeleteAllMeasurementsThisDay -> {
                    for (i in bodyParts.indices) {
                        val viewHolder = binding.bodyPartsRV.findViewHolderForAdapterPosition(i)
                        val measurementsRV = viewHolder?.itemView?.findViewById<RecyclerView>(R.id.measurementsRV)
                        val adapterInner = measurementsRV?.adapter
                        adapterInner?.let {
                            if (positionChild < bodyParts[i].measurements.size - 1) {
                                bodyParts[i].measurements.removeAt(positionChild)
                                adapterInner.notifyItemChanged(positionChild)
                                this.adapter.notifyItemChanged(positionParent)
                            }
                        }
                    }
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
        popupMenu.show()
    }
}