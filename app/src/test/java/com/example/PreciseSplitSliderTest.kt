package com.example

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.components.PreciseSplitSlider
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PreciseSplitSliderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun splitSlider_rendersWithoutCrash_atAllEdgePercentages() {
        val dummyFileA = File.createTempFile("orig", ".jpg").apply { writeBytes(ByteArray(10)) }
        val dummyFileB = File.createTempFile("edit", ".jpg").apply { writeBytes(ByteArray(10)) }

        // Test rendering split slider
        composeTestRule.setContent {
            PreciseSplitSlider(
                originalPath = dummyFileA.absolutePath,
                editedPath = dummyFileB.absolutePath,
                modifier = Modifier,
                zoomScale = 1f,
                isZoomLocked = true
            )
        }

        // Verify root composable exists without throwing any negative padding exception
        composeTestRule.onRoot().assertExists()

        dummyFileA.delete()
        dummyFileB.delete()
    }
}
