package com.example.digitallevel.data

import kotlinx.coroutines.flow.Flow

class MeasurementRepository(private val measurementDao: MeasurementDao) {

    fun getAllMeasurements(): Flow<List<MeasurementEntity>> {
        return measurementDao.getAllMeasurements()
    }

    suspend fun insert(measurement: MeasurementEntity): Long {
        return measurementDao.insert(measurement)
    }

    suspend fun getMeasurementById(id: Int): MeasurementEntity? {
        return measurementDao.getMeasurementById(id)
    }

    suspend fun delete(measurement: MeasurementEntity) {
        measurementDao.delete(measurement)
    }

    suspend fun deleteAll() {
        measurementDao.deleteAll()
    }
}
