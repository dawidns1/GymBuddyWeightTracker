package com.example.gymbuddy_weighttracker.model

import java.io.Serializable
import java.util.*

class Data(
    var serializableEntries: ArrayList<SerializableEntry>,
    var bodyParts: ArrayList<BodyPart>
) : Serializable