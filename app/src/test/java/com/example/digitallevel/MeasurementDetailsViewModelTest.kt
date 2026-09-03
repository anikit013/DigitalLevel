package com.example.digitallevel

import android.content.SharedPreferences
import com.example.digitallevel.data.MeasurementDao
import com.example.digitallevel.data.MeasurementEntity
import com.example.digitallevel.data.MeasurementRepository
import com.example.digitallevel.data.PreferencesManager
import com.example.digitallevel.ui.MeasurementDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MeasurementDetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDao: FakeMeasurementDao
    private lateinit var repository: MeasurementRepository
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var viewModel: MeasurementDetailsViewModel

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

    private class TestSharedPreferences : SharedPreferences {
        private val map = mutableMapOf<String, Any>()

        override fun getAll(): MutableMap<String, *> = map
        override fun getString(key: String?, defValue: String?): String? = (map[key] as? String) ?: defValue
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = null
        override fun getInt(key: String?, defValue: Int): Int = (map[key] as? Int) ?: defValue
        override fun getLong(key: String?, defValue: Long): Long = (map[key] as? Long) ?: defValue
        override fun getFloat(key: String?, defValue: Float): Float = (map[key] as? Float) ?: defValue
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = (map[key] as? Boolean) ?: defValue
        override fun contains(key: String?): Boolean = map.containsKey(key)
        override fun edit(): SharedPreferences.Editor = Editor(map)
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

        private class Editor(private val storage: MutableMap<String, Any>) : SharedPreferences.Editor {
            private val changes = mutableMapOf<String, Any?>()
            private var clearAll = false

            override fun putString(key: String, value: String?): SharedPreferences.Editor { changes[key] = value; return this }
            override fun putStringSet(key: String, values: MutableSet<String>?): SharedPreferences.Editor { return this }
            override fun putInt(key: String, value: Int): SharedPreferences.Editor { changes[key] = value; return this }
            override fun putLong(key: String, value: Long): SharedPreferences.Editor { changes[key] = value; return this }
            override fun putFloat(key: String, value: Float): SharedPreferences.Editor { changes[key] = value; return this }
            override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor { changes[key] = value; return this }
            override fun remove(key: String): SharedPreferences.Editor { changes[key] = null; return this }
            override fun clear(): SharedPreferences.Editor { clearAll = true; return this }
            override fun commit(): Boolean { apply(); return true }
            override fun apply() {
                if (clearAll) storage.clear()
                for ((k, v) in changes) {
                    if (v == null) storage.remove(k) else storage[k] = v
                }
            }
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeMeasurementDao()
        repository = MeasurementRepository(fakeDao)
        preferencesManager = PreferencesManager(TestSharedPreferences())
        viewModel = MeasurementDetailsViewModel(repository, preferencesManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadMeasurement() = runTest {
        val entity = MeasurementEntity(
            id = 1,
            timestamp = 1000L,
            angleX = 2.5f,
            angleY = 1.0f,
            overallTilt = 2.69f,
            lightLevel = 500f,
            status = "SLIGHTLY TILTED"
        )
        fakeDao.insert(entity)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMeasurement(1)
        testDispatcher.scheduler.advanceUntilIdle()

        val loaded = viewModel.measurement.value
        assertNotNull(loaded)
        assertEquals(1, loaded?.id)
        assertEquals(2.5f, loaded?.angleX)
        assertEquals("SLIGHTLY TILTED", loaded?.status)
    }

    @Test
    fun testDeleteCurrentMeasurement() = runTest {
        val entity = MeasurementEntity(
            id = 1,
            timestamp = 1000L,
            angleX = 0f,
            angleY = 0f,
            overallTilt = 0f,
            lightLevel = 100f,
            status = "LEVEL"
        )
        fakeDao.insert(entity)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMeasurement(1)
        testDispatcher.scheduler.advanceUntilIdle()

        var onDeletedCalled = false
        viewModel.deleteCurrentMeasurement {
            onDeletedCalled = true
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(onDeletedCalled)
        assertNull(fakeDao.getMeasurementById(1))
    }

    @Test
    fun testSetAsReference() {
        val entity = MeasurementEntity(
            id = 1,
            timestamp = 1000L,
            angleX = 3.5f,
            angleY = 2.1f,
            overallTilt = 4.08f,
            lightLevel = 300f,
            status = "SLIGHTLY TILTED"
        )

        viewModel.setAsReference(entity)

        assertEquals(3.5f, preferencesManager.offsetX, 0.001f)
        assertEquals(2.1f, preferencesManager.offsetY, 0.001f)
        assertTrue(preferencesManager.isCalibrated)
    }
}
