package com.example.gymbuddy_weighttracker.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.util.ArrayList

class MainViewModel : ViewModel() {

    private val _dataset: MutableLiveData<ArrayList<ILineDataSet>> = MutableLiveData(null)
    val dataset: LiveData<ArrayList<ILineDataSet>> = _dataset
    fun setDataset(dataset: ArrayList<ILineDataSet>){
        _dataset.value=dataset
    }

    private val _hideFAB = MutableLiveData(false)
    val hideFAB: LiveData<Boolean> = _hideFAB
    fun setHideFAB(hideFAB: Boolean){
        _hideFAB.value=hideFAB
    }
    private val _inputOngoing = MutableLiveData(false)
    val inputOngoing: LiveData<Boolean> = _inputOngoing
    fun setInputOngoing(inputOngoing: Boolean){
        _inputOngoing.value=inputOngoing
    }

    private val _addValueClicked = MutableLiveData(false)
    val addValueClicked: LiveData<Boolean> = _addValueClicked
    fun setAddValueClicked(addValueClicked: Boolean){
        _addValueClicked.value = addValueClicked
    }

    private val _addValueLongClicked = MutableLiveData(false)
    val addValueLongClicked: LiveData<Boolean> = _addValueLongClicked
    fun setAddValueLongClicked(addValueLongClicked: Boolean){
        _addValueLongClicked.value = addValueLongClicked
    }

    fun addDataset(set: LineDataSet){
        _dataset.value?.add(set)
    }

    private val _offset = MutableLiveData(1)
    val offset: LiveData<Int> = _offset
    fun setOffset(offset: Int){
        _offset.value = offset
    }
}