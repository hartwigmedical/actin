package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationWarning
import com.hartwig.actin.clinical.curation.translation.Translation
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.clinical.feed.emc.digitalfile.DigitalFileEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val PATIENT_ID = "patient1"
private const val CANNOT_TRANSLATE = "cannot translate"

private const val BLOOD_TRANSFUSION_INPUT = "Blood transfusion input"
private const val TRANSLATED_BLOOD_TRANSFUSION = "Curated blood transfusion"

class BloodTransfusionsExtractorTest {

    @Test
    fun `Should translate blood transfusions`() {

        val extractor = BloodTransfusionsExtractor(
            TranslationDatabase(
                mapOf(BLOOD_TRANSFUSION_INPUT to Translation(BLOOD_TRANSFUSION_INPUT, TRANSLATED_BLOOD_TRANSFUSION)),
                CurationCategory.BLOOD_TRANSFUSION_TRANSLATION
            ) { emptySet() }
        )
        val inputs = listOf(BLOOD_TRANSFUSION_INPUT, CANNOT_TRANSLATE)
        val entry = DigitalFileEntry(
            subject = PATIENT_ID,
            authored = LocalDate.of(2019, 9, 9),
            description = "Aanvraag bloedproducten_test",
            itemText = "",
            itemAnswerValueValueString = ""
        )
        val (extracted, evaluation) = extractor.extract(PATIENT_ID, inputs.map { entry.copy(itemAnswerValueValueString = it) })
        assertThat(extracted).hasSize(2)
        assertThat(extracted[0].product).isEqualTo(TRANSLATED_BLOOD_TRANSFUSION)
        assertThat(extracted[1].product).isEqualTo(CANNOT_TRANSLATE)

        assertThat(evaluation.warnings).containsExactly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.BLOOD_TRANSFUSION_TRANSLATION,
                CANNOT_TRANSLATE,
                "No translation found for blood transfusion with product: '$CANNOT_TRANSLATE'"
            )
        )
    }
}