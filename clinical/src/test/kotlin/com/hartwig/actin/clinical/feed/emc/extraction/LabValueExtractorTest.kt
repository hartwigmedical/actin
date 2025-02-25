package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.config.LabMeasurementConfig
import com.hartwig.actin.clinical.feed.emc.lab.LabEntry
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabUnit
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.Test
import java.time.LocalDate

private const val PATIENT_ID = "patient1"
private const val CANNOT_CURATE_NAME = "Cannot curate"
private const val CANNOT_CURATE_CODE = "Cannot curate"
private const val EPSILON = 1.0E-10
private const val LAB_CODE = "Hb"
private const val LAB_NAME = "Hemoglobine"
private val LAB_MEASUREMENT = LabMeasurement.HEMOGLOBIN

class LabValueExtractorTest {

    val extractor = LabValueExtractor(
        TestCurationFactory.curationDatabase(
            LabMeasurementConfig(
                input = "$LAB_CODE | $LAB_NAME",
                labMeasurement = LAB_MEASUREMENT
            )
        )
    )

    @Test
    fun `Should extract laboratory values`() {
        val labEntry1 = LabEntry(
            subject = PATIENT_ID,
            valueQuantityComparator = "",
            codeCodeOriginal = LAB_CODE,
            codeDisplayOriginal = LAB_NAME,
            valueQuantityValue = 19.0,
            valueQuantityUnit = "g/dl",
            referenceRangeText = "14 - 18",
            effectiveDateTime = LocalDate.of(2020, 1, 1),
        )
        val labEntry2 = LabEntry(
            subject = PATIENT_ID,
            valueQuantityComparator = "",
            codeCodeOriginal = CANNOT_CURATE_CODE,
            codeDisplayOriginal = CANNOT_CURATE_NAME,
            valueQuantityValue = 0.0,
            valueQuantityUnit = "",
            referenceRangeText = "",
            effectiveDateTime = LocalDate.of(2020, 1, 1),
        )
        val (extractedValues, evaluation) = extractor.extract(PATIENT_ID, listOf(labEntry1, labEntry2))
        assertThat(extractedValues).hasSize(1)
        assertThat(extractedValues[0].date).isEqualTo(LocalDate.of(2020, 1, 1))
        assertThat(extractedValues[0].comparator).isEqualTo("")
        assertThat(extractedValues[0].measurement).isEqualTo(LAB_MEASUREMENT)
        assertThat(extractedValues[0].value).isEqualTo(19.0)
        assertThat(extractedValues[0].refLimitLow).isEqualTo(14.0)
        assertThat(extractedValues[0].refLimitUp).isEqualTo(18.0)
        assertThat(extractedValues[0].unit).isEqualTo(LabUnit.GRAMS_PER_DECILITER)
        assertThat(extractedValues[0].isOutsideRef).isTrue()

        assertThat(evaluation.warnings).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.LAB_MEASUREMENT,
                "$CANNOT_CURATE_CODE | $CANNOT_CURATE_NAME",
                "Could not find lab measurement config for input '$CANNOT_CURATE_CODE | $CANNOT_CURATE_NAME'"
            )
        )
    }

    @Test
    fun `Should extract limits from referenceRangeString`() {
        assertLimits("12 - 14", 12.0, 14.0)
        assertLimits("-3 - 3", -3.0, 3.0)
        assertLimits("-6 - -3", -6.0, -3.0)
        assertLimits("> 50", 50.0, null)
        assertLimits("> -6", -6.0, null)
        assertLimits("<90", null, 90.0)
        assertLimits("not a limit", null, null)
        assertLimits("3,1 - 5,1", 3.1, 5.1)
        assertLimits("-3-5", -3.0, 5.0)
        assertLimits("-3--5", -3.0, -5.0)
    }

    private fun assertLimits(referenceRangeText: String, lower: Double?, upper: Double?) {
        val (extractedValues, _) = extractor.extract(PATIENT_ID, labEntryWithRange(referenceRangeText))
        listOf(
            extractedValues[0].refLimitLow to lower,
            extractedValues[0].refLimitUp to upper
        ).forEach { (actual, expected) ->
            if (expected == null) {
                assertThat(actual).isNull()
            } else {
                assertThat(actual).isEqualTo(expected, Offset.offset(EPSILON))
            }
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw for lab entry with leading hyphen but no measurement`() {
        extractor.extract(PATIENT_ID, labEntryWithRange("-Nope"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw for lab entry with internal hyphen but no measurement`() {
        extractor.extract(PATIENT_ID, labEntryWithRange("not a reference-range-text"))
    }

    private fun labEntryWithRange(referenceRangeText: String): List<LabEntry> {
        return listOf(
            LabEntry(
                subject = PATIENT_ID,
                codeCodeOriginal = LAB_CODE,
                codeDisplayOriginal = LAB_NAME,
                valueQuantityComparator = "test",
                valueQuantityValue = 0.0,
                valueQuantityUnit = "g/dL",
                referenceRangeText = referenceRangeText,
                effectiveDateTime = LocalDate.of(2024, 11, 22)
            )
        )
    }
}