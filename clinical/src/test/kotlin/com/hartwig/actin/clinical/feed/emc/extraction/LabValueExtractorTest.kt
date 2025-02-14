package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.config.LabMeasurementConfig
import com.hartwig.actin.clinical.feed.emc.lab.LabEntry
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabUnit
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val PATIENT_ID = "patient1"
private const val CANNOT_CURATE_NAME = "Cannot curate"
private const val CANNOT_CURATE_CODE = "Cannot curate"

class LabValueExtractorTest {

    val extractor = LabValueExtractor(
        TestCurationFactory.curationDatabase(
            LabMeasurementConfig(
                input = "Hb | Hemoglobine",
                labMeasurement = LabMeasurement.HEMOGLOBIN
            )
        )
    )

    @Test
    fun `Should extract and translate laboratory values`() {
        val labEntry1 = LabEntry(
            subject = PATIENT_ID,
            valueQuantityComparator = "",
            codeCodeOriginal = "Hb",
            codeDisplayOriginal = "Hemoglobine",
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
        assertThat(extractedValues[0].measurement).isEqualTo(LabMeasurement.HEMOGLOBIN)
        assertThat(extractedValues[0].unit).isEqualTo(LabUnit.GRAMS_PER_DECILITER)
        assertThat(extractedValues[0].isOutsideRef).isEqualTo(true)

        assertThat(evaluation.warnings).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.LABORATORY,
                "$CANNOT_CURATE_CODE | $CANNOT_CURATE_NAME",
                "Could not find laboratory config for input '$CANNOT_CURATE_CODE | $CANNOT_CURATE_NAME'"
            )
        )
    }
}