package com.example.digitallevel

import android.content.SharedPreferences
import com.example.digitallevel.data.PreferencesManager
import com.example.digitallevel.sensor.MeasurementMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PreferencesManagerTest {

    private lateinit var fakeSharedPreferences: FakeSharedPreferences
    private lateinit var preferencesManager: PreferencesManager

    private class FakeSharedPreferences : SharedPreferences {
        private val map = mutableMapOf<String, Any>()

        override fun getAll(): MutableMap<String, *> = map

        override fun getString(key: String?, defValue: String?): String? =
            (map[key] as? String) ?: defValue

        override fun getStringSet(
            key: String?,
            defValues: MutableSet<String>?
        ): MutableSet<String>? =
            (map[key] as? Set<*>)?.mapNotNull { it as? String }?.toMutableSet() ?: defValues

        override fun getInt(key: String?, defValue: Int): Int =
            (map[key] as? Int) ?: defValue

        override fun getLong(key: String?, defValue: Long): Long =
            (map[key] as? Long) ?: defValue

        override fun getFloat(key: String?, defValue: Float): Float =
            (map[key] as? Float) ?: defValue

        override fun getBoolean(key: String?, defValue: Boolean): Boolean =
            (map[key] as? Boolean) ?: defValue

        override fun contains(key: String?): Boolean = map.containsKey(key)

        override fun edit(): SharedPreferences.Editor = FakeEditor(map)

        override fun registerOnSharedPreferenceChangeListener(
            listener: SharedPreferences.OnSharedPreferenceChangeListener?
        ) {}

        override fun unregisterOnSharedPreferenceChangeListener(
            listener: SharedPreferences.OnSharedPreferenceChangeListener?
        ) {}

        private class FakeEditor(private val storage: MutableMap<String, Any>) :
            SharedPreferences.Editor {

            private val changes = mutableMapOf<String, Any?>()
            private var clearAll = false

            override fun putString(key: String, value: String?): SharedPreferences.Editor {
                changes[key] = value
                return this
            }

            override fun putStringSet(
                key: String,
                values: MutableSet<String>?
            ): SharedPreferences.Editor {
                changes[key] = values
                return this
            }

            override fun putInt(key: String, value: Int): SharedPreferences.Editor {
                changes[key] = value
                return this
            }

            override fun putLong(key: String, value: Long): SharedPreferences.Editor {
                changes[key] = value
                return this
            }

            override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
                changes[key] = value
                return this
            }

            override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
                changes[key] = value
                return this
            }

            override fun remove(key: String): SharedPreferences.Editor {
                changes[key] = null
                return this
            }

            override fun clear(): SharedPreferences.Editor {
                clearAll = true
                return this
            }

            override fun commit(): Boolean {
                apply()
                return true
            }

            override fun apply() {
                if (clearAll) {
                    storage.clear()
                }
                for ((key, value) in changes) {
                    if (value == null) {
                        storage.remove(key)
                    } else {
                        storage[key] = value
                    }
                }
            }
        }
    }

    @Before
    fun setUp() {
        fakeSharedPreferences = FakeSharedPreferences()
        preferencesManager = PreferencesManager(fakeSharedPreferences)
    }

    @Test
    fun testDefaultValues() {
        assertFalse(preferencesManager.isDarkMode)
        assertTrue(preferencesManager.keepScreenAwake)
        assertTrue(preferencesManager.levelFeedbackEnabled)
        assertEquals(0f, preferencesManager.offsetX, 0.001f)
        assertEquals(0f, preferencesManager.offsetY, 0.001f)
        assertFalse(preferencesManager.isCalibrated)
    }

    @Test
    fun testKeepScreenAwakeAndFeedbackToggles() {
        preferencesManager.keepScreenAwake = false
        assertFalse(preferencesManager.keepScreenAwake)

        preferencesManager.levelFeedbackEnabled = false
        assertFalse(preferencesManager.levelFeedbackEnabled)
    }

    @Test
    fun testDarkModeToggle() {
        preferencesManager.isDarkMode = true
        assertTrue(preferencesManager.isDarkMode)

        preferencesManager.isDarkMode = false
        assertFalse(preferencesManager.isDarkMode)
    }

    @Test
    fun testDirectOffsetSetters() {
        preferencesManager.offsetX = 2.5f
        preferencesManager.offsetY = -1.2f

        assertEquals(2.5f, preferencesManager.offsetX, 0.001f)
        assertEquals(-1.2f, preferencesManager.offsetY, 0.001f)
    }

    @Test
    fun testSaveCalibration() {
        preferencesManager.saveCalibration(3.14f, -2.71f)

        assertEquals(3.14f, preferencesManager.offsetX, 0.001f)
        assertEquals(-2.71f, preferencesManager.offsetY, 0.001f)
        assertTrue(preferencesManager.isCalibrated)
    }

    @Test
    fun testSeparateFlatAndEdgeModeCalibrations() {
        preferencesManager.saveCalibrationForMode(1.0f, 2.0f, MeasurementMode.FLAT)
        preferencesManager.saveCalibrationForMode(5.0f, 6.0f, MeasurementMode.EDGE)

        assertEquals(1.0f, preferencesManager.getOffsetX(MeasurementMode.FLAT), 0.001f)
        assertEquals(2.0f, preferencesManager.getOffsetY(MeasurementMode.FLAT), 0.001f)
        assertTrue(preferencesManager.isCalibratedForMode(MeasurementMode.FLAT))

        assertEquals(5.0f, preferencesManager.getOffsetX(MeasurementMode.EDGE), 0.001f)
        assertEquals(6.0f, preferencesManager.getOffsetY(MeasurementMode.EDGE), 0.001f)
        assertTrue(preferencesManager.isCalibratedForMode(MeasurementMode.EDGE))

        preferencesManager.resetCalibrationForMode(MeasurementMode.EDGE)
        assertTrue(preferencesManager.isCalibratedForMode(MeasurementMode.FLAT))
        assertFalse(preferencesManager.isCalibratedForMode(MeasurementMode.EDGE))
    }

    @Test
    fun testResetCalibration() {
        preferencesManager.saveCalibration(5.0f, 2.5f)
        assertTrue(preferencesManager.isCalibrated)

        preferencesManager.resetCalibration()

        assertEquals(0f, preferencesManager.offsetX, 0.001f)
        assertEquals(0f, preferencesManager.offsetY, 0.001f)
        assertFalse(preferencesManager.isCalibrated)
    }

    @Test
    fun testPersistenceAcrossInstances() {
        preferencesManager.saveCalibration(1.5f, 0.5f)
        preferencesManager.isDarkMode = true
        preferencesManager.keepScreenAwake = false
        preferencesManager.levelFeedbackEnabled = false

        val newManagerInstance = PreferencesManager(fakeSharedPreferences)
        assertEquals(1.5f, newManagerInstance.offsetX, 0.001f)
        assertEquals(0.5f, newManagerInstance.offsetY, 0.001f)
        assertTrue(newManagerInstance.isCalibrated)
        assertTrue(newManagerInstance.isDarkMode)
        assertFalse(newManagerInstance.keepScreenAwake)
        assertFalse(newManagerInstance.levelFeedbackEnabled)
    }
}
