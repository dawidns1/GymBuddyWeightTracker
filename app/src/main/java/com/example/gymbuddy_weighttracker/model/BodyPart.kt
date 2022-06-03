package com.example.gymbuddy_weighttracker.model

import java.io.Serializable
import java.util.*

class BodyPart(var name: String) : Serializable {
    var measurements: ArrayList<Measurement> = ArrayList()
}