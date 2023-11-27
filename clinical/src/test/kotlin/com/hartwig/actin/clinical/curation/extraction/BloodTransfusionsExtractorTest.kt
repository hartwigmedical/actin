package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.feed.digitalfile.DigitalFileEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val PATIENT_ID = "patient1"
private const val CANNOT_CURATE = "cannot curate"

class BloodTransfusionsExtractorTest {

    @Test
    fun `Should translate blood transfusions`() {
        val extractor = BloodTransfusionsExtractor(TestCurationFactory.createProperTestCurationDatabase())
        val inputs = listOf("Product", CANNOT_CURATE)
        val entry = DigitalFileEntry(
            subject = PATIENT_ID,
            authored = LocalDate.of(2019, 9, 9),
            description = "Aanvraag bloedproducten_test",
            itemText = "",
            itemAnswerValueValueString = "",
            isBloodTransfusionEntry = true,
            isToxicityEntry = false
        )
        val (extracted, evaluation) = extractor.extract(PATIENT_ID, inputs.map { entry.copy(itemAnswerValueValueString = it) })
        assertThat(extracted).hasSize(2)
        assertThat(extracted[0].product()).isEqualTo("Translated product")
        assertThat(extracted[1].product()).isEqualTo(CANNOT_CURATE)

        assertThat(evaluation.warnings).containsExactly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.BLOOD_TRANSFUSION_TRANSLATION,
                CANNOT_CURATE,
                "No translation found for blood transfusion with product: '$CANNOT_CURATE'"
            )
        )
    }
}