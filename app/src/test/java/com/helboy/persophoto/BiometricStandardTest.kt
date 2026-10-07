package com.helboy.persophoto

import com.helboy.persophoto.data.BiometricStandard
import com.helboy.persophoto.data.PrintSheetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.roundToInt

class BiometricStandardTest {

    @Test
    fun testStandardAspectRatios() {
        val iran = BiometricStandard.IRAN_3X4
        assertEquals(0.75f, iran.aspectRatio, 0.001f)

        val lottery = BiometricStandard.US_LOTTERY_VISA
        assertEquals(1.0f, lottery.aspectRatio, 0.001f)

        val passport = BiometricStandard.ICAO_PASSPORT
        assertEquals(35f / 45f, passport.aspectRatio, 0.001f)

        val canada = BiometricStandard.CANADA_VISA
        assertEquals(50f / 70f, canada.aspectRatio, 0.001f)
    }

    @Test
    fun testStandard300DpiDimensions() {
        // 30mm x 40mm at 300 DPI
        val iran = BiometricStandard.IRAN_3X4
        val expectedW = (30f / 25.4f * 300f).roundToInt()
        val expectedH = (40f / 25.4f * 300f).roundToInt()
        assertEquals(expectedW, iran.targetWidthPx300Dpi)
        assertEquals(expectedH, iran.targetHeightPx300Dpi)
        assertTrue("Width should be around 354px", iran.targetWidthPx300Dpi in 350..360)
        assertTrue("Height should be around 472px", iran.targetHeightPx300Dpi in 470..480)

        // US Lottery (2x2 inch at 300 DPI = 600x600 px)
        val lottery = BiometricStandard.US_LOTTERY_VISA
        assertEquals(600, lottery.targetWidthPx300Dpi)
        assertEquals(600, lottery.targetHeightPx300Dpi)
    }

    @Test
    fun testPrintSheetDimensions() {
        // 10x15 cm sheet at 300 DPI
        val sheet6 = PrintSheetType.SHEET_6_PACK
        assertEquals(6, sheet6.photoCount)
        assertEquals(2, sheet6.columns)
        assertEquals(3, sheet6.rows)
        assertTrue("10x15cm width at 300 DPI should be around 1181 px", sheet6.paperWidthPx300Dpi in 1170..1190)
        assertTrue("10x15cm height at 300 DPI should be around 1772 px", sheet6.paperHeightPx300Dpi in 1760..1780)

        // 13x18 cm sheet at 300 DPI
        val sheet12 = PrintSheetType.SHEET_12_PACK
        assertEquals(12, sheet12.photoCount)
        assertEquals(3, sheet12.columns)
        assertEquals(4, sheet12.rows)
        assertTrue("13x18cm width at 300 DPI should be around 1535 px", sheet12.paperWidthPx300Dpi in 1520..1550)
        assertTrue("13x18cm height at 300 DPI should be around 2126 px", sheet12.paperHeightPx300Dpi in 2110..2140)
    }

    @Test
    fun testFaceHeightRatioCompliance() {
        // Verify head boundaries for passport (70-80%) and lottery (50-69%)
        val passport = BiometricStandard.ICAO_PASSPORT
        assertTrue(passport.minFaceHeightRatio >= 0.70f)
        assertTrue(passport.maxFaceHeightRatio <= 0.80f)

        val lottery = BiometricStandard.US_LOTTERY_VISA
        assertTrue(lottery.minFaceHeightRatio >= 0.50f)
        assertTrue(lottery.maxFaceHeightRatio <= 0.69f)
    }
}
