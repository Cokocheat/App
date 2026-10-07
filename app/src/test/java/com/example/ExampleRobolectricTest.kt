package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("RecStudio 120 FPS", appName)
    }

    @Test
    fun `verify 120 fps option configuration`() {
        val ultraFps = FrameRateOption.FPS_120
        assertEquals(120, ultraFps.fps)
        assertTrue(ultraFps.isUltra)
    }

    @Test
    fun `verify resolution dimensions calculation`() {
        val res = VideoResolution.FHD_1080P
        val portraitDims = res.getDimensions(1080, 2400)
        assertEquals(1080, portraitDims.first)
        assertEquals(1920, portraitDims.second)
    }

    @Test
    fun `verify duration and size formatting`() {
        val state = RecordingState(
            status = RecordStatus.RECORDING,
            durationSeconds = 125L,
            estimatedBytes = 50 * 1024 * 1024L
        )
        assertEquals("02:05", state.formattedDuration)
        assertTrue(state.formattedSize.contains("50"))
    }
}
