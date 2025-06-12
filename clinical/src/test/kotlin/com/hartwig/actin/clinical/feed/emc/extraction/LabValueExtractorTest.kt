package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.config.LabMeasurementConfig
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabUnit
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationWarning
import com.hartwig.feed.datamodel.FeedLabValue
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
        val labEntry1 = FeedLabValue(
            date = LocalDate.of(2020, 1, 1),
            measure = LAB_NAME,
            measureCode = LAB_CODE,
            value = 19.0,
            unit = "g/dl",
            refLowerBound = 14.0,
            refUpperBound = 18.0,
            comparator = "",
        )
        val labEntry2 = FeedLabValue(
            date = LocalDate.of(2020, 1, 1),
            measure = CANNOT_CURATE_NAME,
            measureCode = CANNOT_CURATE_CODE,
            value = 0.0,
            unit = "",
            refLowerBound = null,
            refUpperBound = null,
            comparator = "",
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
        assertLimits(12.0, 14.0)
        assertLimits(-3.0, 3.0)
        assertLimits(-6.0, -3.0)
        assertLimits(50.0, null)
        assertLimits(-6.0, null)
        assertLimits(null, 90.0)
        assertLimits(null, null)
        assertLimits(3.1, 5.1)
        assertLimits(-3.0, 5.0)
        assertLimits(-3.0, -5.0)
    }

    private fun assertLimits(lower: Double?, upper: Double?) {
        val (extractedValues, _) = extractor.extract(PATIENT_ID, labEntryWithRange(lower, upper))
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

    private fun labEntryWithRange(refLowerBound: Double?, refUpperBound: Double?): List<FeedLabValue> {
        return listOf(
            FeedLabValue(
                LocalDate.of(2024, 11, 22),
                measure = LAB_NAME,
                measureCode = LAB_CODE,
                value = 0.0,
                unit = "g/dl",
                refLowerBound = refLowerBound,
                refUpperBound = refUpperBound,
                comparator = "test",
            )
        )
    }
}