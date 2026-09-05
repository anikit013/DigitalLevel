package com.example.digitallevel

import com.example.digitallevel.data.MeasurementDao
import com.example.digitallevel.data.MeasurementEntity
import com.example.digitallevel.data.MeasurementRepository
import com.example.digitallevel.ui.HistoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: MeasurementRepository
    private lateinit var viewModel: HistoryViewModel
    private lateinit var fakeDao: FakeMeasurementDao

    private class FakeMeasurementDao : MeasurementDao {
        val items = mutableListOf<MeasurementEntity>()
        private val flow = MutableStateFlow<List<MeasurementEntity>>(emptyList())

        override suspend fun insert(measurement: MeasurementEntity): Long {
            val nextId = items.size + 1
            val saved = measurement.copy(id = nextId)
            items.add(saved)
            flow.value = items.toList()
            return nextId.toLong()
        }

        override fun getAllMeasurements(): Flow<List<MeasurementEntity>> = flow

        override suspend fun getMeasurementById(id: Int): MeasurementEntity? {
            return items.find { it.id == id }
        }

        override suspend fun delete(measurement: MeasurementEntity) {
            items.removeIf { it.id == measurement.id }
            flow.value = items.toList()
        }

        override suspend fun deleteAll() {
            items.clear()
            flow.value = emptyList()
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeMeasurementDao()
        repository = MeasurementRepository(fakeDao)
        viewModel = HistoryViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testDeleteAndClearAll() = runTest {
        val m1 = MeasurementEntity(id = 1, timestamp = 1000L, angleX = 0f, angleY = 0f, overallTilt = 0f, lightLevel = 100f, status = "LEVEL")
        val m2 = MeasurementEntity(id = 2, timestamp = 2000L, angleX = 2f, angleY = 2f, overallTilt = 2.8f, lightLevel = 200f, status = "SLIGHTLY TILTED")

        fakeDao.insert(m1)
        fakeDao.insert(m2)
        testDispatcher.scheduler.advanceUntilIdle()

        var list = viewModel.measurements.first()
        assertEquals(2, list.size)

        viewModel.deleteMeasurement(m1)
        testDispatcher.scheduler.advanceUntilIdle()

        list = viewModel.measurements.first()
        assertEquals(1, list.size)
        assertEquals(2, list[0].id)

        viewModel.deleteAllMeasurements()
        testDispatcher.scheduler.advanceUntilIdle()

        list = viewModel.measurements.first()
        assertEquals(0, list.size)
    }

    @Test
    fun testDeleteByIdAndModeField() = runTest {
        val m1 = MeasurementEntity(id = 1, timestamp = 1000L, angleX = 0f, angleY = 0f, overallTilt = 0f, lightLevel = 100f, status = "LEVEL", mode = "FLAT")
        val m2 = MeasurementEntity(id = 2, timestamp = 2000L, angleX = 2f, angleY = 2f, overallTilt = 2.8f, lightLevel = 200f, status = "SLIGHTLY TILTED", mode = "EDGE")

        fakeDao.insert(m1)
        fakeDao.insert(m2)
        testDispatcher.scheduler.advanceUntilIdle()

        var list = viewModel.measurements.first()
        assertEquals("FLAT", list.find { it.id == 1 }?.mode)
        assertEquals("EDGE", list.find { it.id == 2 }?.mode)

        viewModel.deleteMeasurementById(1)
        testDispatcher.scheduler.advanceUntilIdle()

        list = viewModel.measurements.first()
        assertEquals(1, list.size)
        assertEquals(2, list[0].id)
    }
}
