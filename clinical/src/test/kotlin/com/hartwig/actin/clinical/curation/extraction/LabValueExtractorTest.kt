package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue
import com.hartwig.actin.clinical.datamodel.LabUnit
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val PATIENT_ID = "patient1"
private const val CANNOT_CURATE = "Cannot curate"

class LabValueExtractorTest {

    @Test
    fun `Should translate laboratory values`() {
        val labValue = ImmutableLabValue.builder()
            .date(LocalDate.of(2020, 1, 1))
            .code("CO")
            .name("naam")
            .comparator("")
            .value(0.0)
            .unit(LabUnit.NONE)
            .isOutsideRef(false)
            .build()
        val rawValues = listOf(labValue, ImmutableLabValue.builder().from(labValue).code(CANNOT_CURATE).name(CANNOT_CURATE).build())
        val extractor = LabValueExtractor(TestCurationFactory.createProperTestCurationDatabase())
        val (extractedValues, evaluation) = extractor.extract(PATIENT_ID, rawValues)
        assertThat(extractedValues).hasSize(1)
        assertThat(extractedValues[0].code()).isEqualTo("CODE")
        assertThat(extractedValues[0].name()).isEqualTo("Name")

        assertThat(evaluation.warnings).containsOnly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.LABORATORY_TRANSLATION,
                CANNOT_CURATE,
                "Could not find laboratory translation for lab value with code '$CANNOT_CURATE' and name '$CANNOT_CURATE'"
            )
        )
    }
}