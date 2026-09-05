package com.example.digitallevel

import android.content.SharedPreferences
import com.example.digitallevel.data.MeasurementDao
import com.example.digitallevel.data.MeasurementEntity
import com.example.digitallevel.data.MeasurementRepository
import com.example.digitallevel.data.PreferencesManager
import com.example.digitallevel.sensor.MeasurementMode
import com.example.digitallevel.ui.LevelViewModel
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.sqrt

@OptIn(ExperimentalCoroutinesApi::class)
class LevelViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: MeasurementRepository
    private lateinit var viewModel: LevelViewModel

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
        val fakeDao = FakeMeasurementDao()
        repository = MeasurementRepository(fakeDao)
        viewModel = LevelViewModel(repository, null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLevelStatusBoundariesLevelCategory() {
        // 0.0° to 0.99° -> "LEVEL"
        assertEquals("LEVEL", viewModel.calculateStatus(0.0f))
        assertEquals("LEVEL", viewModel.calculateStatus(0.5f))
        assertEquals("LEVEL", viewModel.calculateStatus(0.99f))
        assertEquals("LEVEL", viewModel.calculateStatus(0.999f))
    }

    @Test
    fun testLevelStatusBoundariesSlightlyTiltedCategory() {
        // 1.0° to 4.99° -> "SLIGHTLY TILTED"
        assertEquals("SLIGHTLY TILTED", viewModel.calculateStatus(1.0f))
        assertEquals("SLIGHTLY TILTED", viewModel.calculateStatus(1.001f))
        assertEquals("SLIGHTLY TILTED", viewModel.calculateStatus(2.5f))
        assertEquals("SLIGHTLY TILTED", viewModel.calculateStatus(4.99f))
        assertEquals("SLIGHTLY TILTED", viewModel.calculateStatus(4.999f))
    }

    @Test
    fun testLevelStatusBoundariesTiltedCategory() {
        // 5.0°+ -> "TILTED"
        assertEquals("TILTED", viewModel.calculateStatus(5.0f))
        assertEquals("TILTED", viewModel.calculateStatus(5.001f))
        assertEquals("TILTED", viewModel.calculateStatus(10.0f))
        assertEquals("TILTED", viewModel.calculateStatus(45.0f))
        assertEquals("TILTED", viewModel.calculateStatus(90.0f))
    }

    @Test
    fun testCalibrationOffsetMathPositiveValues() {
        // raw tilt minus offset
        viewModel.processTilt(10.0f, 5.0f)
        viewModel.setCalibrationOffset(2.0f, 1.0f)

        val state = viewModel.uiState.value
        assertEquals(8.0f, state.calibratedX, 0.01f)
        assertEquals(4.0f, state.calibratedY, 0.01f)
        val expectedOverall = sqrt((8.0 * 8.0 + 4.0 * 4.0)).toFloat()
        assertEquals(expectedOverall, state.overallTilt, 0.01f)
        assertEquals("TILTED", state.status)
        assertTrue(state.isCalibrated)
    }

    @Test
    fun testCalibrationOffsetMathNegativeValues() {
        // raw tilt minus offset with negative raw values
        viewModel.processTilt(-5.0f, -3.0f)
        viewModel.setCalibrationOffset(-2.0f, -1.0f)

        val state = viewModel.uiState.value
        // calibratedX = -5.0 - (-2.0) = -3.0
        // calibratedY = -3.0 - (-1.0) = -2.0
        assertEquals(-3.0f, state.calibratedX, 0.01f)
        assertEquals(-2.0f, state.calibratedY, 0.01f)
        val expectedOverall = sqrt((-3.0 * -3.0 + -2.0 * -2.0)).toFloat()
        assertEquals(expectedOverall, state.overallTilt, 0.01f)
        assertEquals("SLIGHTLY TILTED", state.status)
    }

    @Test
    fun testCalibrateCurrentPosition() {
        // Set raw position
        viewModel.processTilt(3.5f, 2.0f)
        // Calibrate to current position
        viewModel.calibrateCurrentPosition()

        val state = viewModel.uiState.value
        assertEquals(3.5f, state.offsetX, 0.01f)
        assertEquals(2.0f, state.offsetY, 0.01f)
        assertEquals(0.0f, state.calibratedX, 0.01f)
        assertEquals(0.0f, state.calibratedY, 0.01f)
        assertEquals(0.0f, state.overallTilt, 0.01f)
        assertEquals("LEVEL", state.status)
        assertTrue(state.isCalibrated)
    }

    @Test
    fun testRawTiltChangesAfterCalibration() {
        viewModel.processTilt(3.5f, 2.0f)
        viewModel.calibrateCurrentPosition()

        // Device changes raw position slightly
        viewModel.processTilt(4.5f, 2.0f)

        val state = viewModel.uiState.value
        // calibratedX = 4.5 - 3.5 = 1.0
        // calibratedY = 2.0 - 2.0 = 0.0
        assertEquals(1.0f, state.calibratedX, 0.01f)
        assertEquals(0.0f, state.calibratedY, 0.01f)
        assertEquals(1.0f, state.overallTilt, 0.01f)
        assertEquals("SLIGHTLY TILTED", state.status)
    }

    @Test
    fun testResetCalibration() {
        viewModel.processTilt(10.0f, 5.0f)
        viewModel.setCalibrationOffset(2.0f, 1.0f)
        viewModel.resetCalibration()

        val state = viewModel.uiState.value
        assertEquals(0.0f, state.offsetX, 0.01f)
        assertEquals(0.0f, state.offsetY, 0.01f)
        assertEquals(10.0f, state.calibratedX, 0.01f)
        assertEquals(5.0f, state.calibratedY, 0.01f)
        assertFalse(state.isCalibrated)
    }

    @Test
    fun testPreferencesManagerIntegration() {
        val testPrefs = TestSharedPreferences()
        val prefsManager = PreferencesManager(testPrefs)
        prefsManager.saveCalibration(1.5f, 0.5f)

        // Create LevelViewModel with existing preferences manager
        val vmWithPrefs = LevelViewModel(repository, prefsManager)
        val initState = vmWithPrefs.uiState.value

        assertEquals(1.5f, initState.offsetX, 0.01f)
        assertEquals(0.5f, initState.offsetY, 0.01f)
        assertTrue(initState.isCalibrated)

        // Process raw tilt
        vmWithPrefs.processTilt(3.5f, 0.5f)
        val stateAfterTilt = vmWithPrefs.uiState.value
        assertEquals(2.0f, stateAfterTilt.calibratedX, 0.01f)
        assertEquals(0.0f, stateAfterTilt.calibratedY, 0.01f)
        assertEquals(2.0f, stateAfterTilt.overallTilt, 0.01f)
        assertEquals("SLIGHTLY TILTED", stateAfterTilt.status)

        // Reset calibration via VM
        vmWithPrefs.resetCalibration()
        assertFalse(prefsManager.isCalibrated)
        assertEquals(0.0f, prefsManager.offsetX, 0.01f)
        assertEquals(0.0f, prefsManager.offsetY, 0.01f)
    }

    @Test
    fun testUpdateSensorAvailability() {
        viewModel.updateSensorAvailability(accelAvailable = false, lightAvailable = true)
        var state = viewModel.uiState.value
        assertFalse(state.isAccelerometerAvailable)
        assertTrue(state.isLightSensorAvailable)

        viewModel.updateSensorAvailability(accelAvailable = true, lightAvailable = false)
        state = viewModel.uiState.value
        assertTrue(state.isAccelerometerAvailable)
        assertFalse(state.isLightSensorAvailable)
    }

    @Test
    fun testSaveMeasurement() = runTest {
        viewModel.processTilt(0.0f, 0.0f)
        viewModel.processLight(150.0f)

        var savedId = -1L
        viewModel.saveMeasurement { id ->
            savedId = id
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1L, savedId)
    }

    @Test
    fun testMeasurementModeSwitchingAndCalibration() {
        val testPrefs = TestSharedPreferences()
        val prefsManager = PreferencesManager(testPrefs)
        val vmWithPrefs = LevelViewModel(repository, prefsManager)

        // Default mode is FLAT
        assertEquals(MeasurementMode.FLAT, vmWithPrefs.uiState.value.currentMode)

        // Calibrate FLAT mode
        vmWithPrefs.processTilt(5.0f, 2.0f)
        vmWithPrefs.calibrateCurrentPosition()
        assertEquals(5.0f, vmWithPrefs.uiState.value.offsetX, 0.01f)
        assertTrue(vmWithPrefs.uiState.value.isCalibrated)

        // Switch to EDGE mode
        vmWithPrefs.setMeasurementMode(MeasurementMode.EDGE)
        assertEquals(MeasurementMode.EDGE, vmWithPrefs.uiState.value.currentMode)
        assertEquals(0.0f, vmWithPrefs.uiState.value.offsetX, 0.01f)
        assertFalse(vmWithPrefs.uiState.value.isCalibrated)

        // Calibrate EDGE mode
        vmWithPrefs.processTilt(10.0f, 4.0f)
        vmWithPrefs.calibrateCurrentPosition()
        assertEquals(10.0f, vmWithPrefs.uiState.value.offsetX, 0.01f)
        assertTrue(vmWithPrefs.uiState.value.isCalibrated)

        // Switch back to FLAT mode and verify FLAT offset restored
        vmWithPrefs.setMeasurementMode(MeasurementMode.FLAT)
        assertEquals(MeasurementMode.FLAT, vmWithPrefs.uiState.value.currentMode)
        assertEquals(5.0f, vmWithPrefs.uiState.value.offsetX, 0.01f)
        assertTrue(vmWithPrefs.uiState.value.isCalibrated)
    }
}
