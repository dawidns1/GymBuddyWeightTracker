package com.example.gymbuddy_weighttracker.recyclerViewAdapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymbuddy_weighttracker.databinding.ItemListBodyPartBinding
import com.example.gymbuddy_weighttracker.model.BodyPart
import java.util.*

class BodyPartsRVAdapter(
    private val mContext: Context,
    private val unit: String,
    private val menuListener: (position: Int, view: View) -> Unit,
    private val addListener: (position: Int, measurementsRV: RecyclerView) -> Unit,
    private val deleteListener: (parentPosition: Int, childPosition: Int,adapterInner: MeasurementsRVAdapter, view:View) -> Unit
) : RecyclerView.Adapter<BodyPartsRVAdapter.ViewHolder>() {
    private var bodyParts = ArrayList<BodyPart>()
    fun setBodyParts(bodyParts: ArrayList<BodyPart>) {
        this.bodyParts = bodyParts
    }
    private lateinit var binding: ItemListBodyPartBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemListBodyPartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val adapterInner = MeasurementsRVAdapter(bodyParts[position].measurements, mContext, position, unit,deleteListener)
        holder.binding.apply {
            measurementsRV.adapter = adapterInner
            measurementsRV.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
            txtBodyPart.text = bodyParts[position].name
            imgAddMeasurement.setOnClickListener { addListener.invoke(position, measurementsRV) }
            divider.visibility=View.GONE
            imgAddMeasurement.visibility=View.GONE
//            measurementsRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                    super.onScrolled(recyclerView, dx, dy)
//                    if (dx > 0) {
//                        if (imgAddMeasurement.isShown) {
//                            imgAddMeasurement.visibility = View.GONE
//                            divider.visibility = View.GONE
//                        }
//                    } else if (dx < 0) {
//                        if (!imgAddMeasurement.isShown) {
//                            imgAddMeasurement.visibility = View.VISIBLE
//                            divider.visibility = View.VISIBLE
//                        }
//                    }
//                }
//            })
        }

    }

    override fun getItemCount(): Int {
        return bodyParts.size
    }

    inner class ViewHolder(var binding: ItemListBodyPartBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.txtBodyPart.setOnLongClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) menuListener.invoke(adapterPosition, binding.txtBodyPart)
                true
            }
        }
    }
}