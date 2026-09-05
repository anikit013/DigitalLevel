package com.example.digitallevel.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurements")
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long,
    val angleX: Float,
    val angleY: Float,
    val overallTilt: Float,
    val lightLevel: Float,
    val status: String,
    val mode: String = "FLAT"
)
