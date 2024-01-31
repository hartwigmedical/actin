package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.translation.LaboratoryIdentifiers
import com.hartwig.actin.clinical.curation.translation.Translation
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.datamodel.LabValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val PATIENT_ID = "patient1"
private const val LAB_CODE_INPUT = "Lab code input"
private const val LAB_NAME_INPUT = "Lab name input"
private const val CANNOT_CURATE = "Cannot curate"

private val LAB_TRANSLATION_INPUTS = LaboratoryIdentifiers(
    LAB_CODE_INPUT,
    LAB_NAME_INPUT
)

private const val LAB_CODE_TRANSLATED = "Lab code translated"

private const val LAB_NAME_TRANSLATED = "Lab name translated"

class LabValueExtractorTest {

    val extractor = LabValueExtractor(
        TranslationDatabase(
            mapOf(
                LAB_TRANSLATION_INPUTS to
                        Translation(
                            LAB_TRANSLATION_INPUTS,
                            LaboratoryIdentifiers(LAB_CODE_TRANSLATED, LAB_NAME_TRANSLATED)
                        )
            ), CurationCategory.LABORATORY_TRANSLATION
        ) { emptySet() }
    )

    @Test
    fun `Should extract and translate laboratory values`() {
        val labValue = LabValue(
            date = LocalDate.of(2020, 1, 1),
            code = LAB_CODE_INPUT,
            name = LAB_NAME_INPUT,
            comparator = "",
            value = 0.0,
            unit = LabUnit.NONE,
            isOutsideRef = false
        )
        val rawValues = listOf(labValue, labValue.copy(code = CANNOT_CURATE, name = CANNOT_CURATE))
        val (extractedValues, evaluation) = extractor.extract(PATIENT_ID, rawValues)
        assertThat(extractedValues).hasSize(1)
        assertThat(extractedValues[0].code).isEqualTo(LAB_CODE_TRANSLATED)
        assertThat(extractedValues[0].name).isEqualTo(LAB_NAME_TRANSLATED)

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