package com.example.gymbuddy_weighttracker.recyclerViewAdapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.gymbuddy_weighttracker.R
import com.example.gymbuddy_weighttracker.databinding.ItemListMeasurementBinding
import com.example.gymbuddy_weighttracker.helpers.Helpers
import com.example.gymbuddy_weighttracker.model.Measurement
import com.example.gymbuddy_weighttracker.recyclerViewAdapters.MeasurementsRVAdapter
import java.util.*

class MeasurementsRVAdapter(
    var measurements: ArrayList<Measurement>,
    private var mContext: Context,
    private val parentPosition: Int,
    private val unit: String,
    private val deleteListener: (parentPosition: Int, childPosition: Int, adapterInner: MeasurementsRVAdapter, view: View) -> Unit
) : RecyclerView.Adapter<MeasurementsRVAdapter.ViewHolder>() {
    fun setmContext(mContext: Context) {
        this.mContext = mContext
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListMeasurementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.txtMeasurementDate.text = measurements[position].date
        holder.binding.txtMeasurement.text = if (measurements[position].value != 0F) {
            String.format("%s%s", Helpers.stringFormat(measurements[position].value.toDouble()), unit)
        } else {
            "---"
        }
    }

    override fun getItemCount(): Int {
        return measurements.size
    }

    inner class ViewHolder(var binding: ItemListMeasurementBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.parentMeasurement.setOnLongClickListener {
                deleteListener.invoke(parentPosition, adapterPosition, this@MeasurementsRVAdapter, it)
                true
            }
        }
    }
}