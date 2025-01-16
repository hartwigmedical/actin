package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.config.ComorbidityConfig
import com.hartwig.actin.clinical.feed.emc.intolerance.IntoleranceEntry
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val PATIENT_ID = "patient1"

private const val INTOLERANCE_INPUT = "Intolerance input"

private const val CURATED_INTOLERANCE = "Curated intolerance"

private const val INTOLERANCE_MEDICATION_INPUT = "Paracetamol"

private const val CURATED_MEDICATION_INTOLERANCE = "Paracetamol"

private const val ICD = "ICD"

private const val CANNOT_CURATE = "Cannot curate"

class IntoleranceExtractorTest {
    private val extractor = IntoleranceExtractor(
        TestCurationFactory.curationDatabase(
            ComorbidityConfig(
                input = INTOLERANCE_INPUT,
                ignore = false,
                curated = Intolerance(
                    name = CURATED_INTOLERANCE,
                    icdCodes = setOf(IcdCode(ICD, null))
                )
            ),
            ComorbidityConfig(
                input = INTOLERANCE_MEDICATION_INPUT,
                ignore = false,
                curated = Intolerance(
                    name = CURATED_MEDICATION_INTOLERANCE,
                    icdCodes = setOf(IcdCode(ICD, null))
                )
            )
        )
    )

    @Test
    fun `Should extract curated intolerances`() {
        val inputs = listOf(INTOLERANCE_INPUT, CANNOT_CURATE)
        val (curated, evaluation) = extractor.extract(PATIENT_ID, inputs.map { entry.copy(codeText = it) })
        assertThat(curated).hasSize(2)
        assertThat(curated[0].name).isEqualTo(CURATED_INTOLERANCE)
        assertThat(curated[0].icdCodes.first().mainCode).isEqualTo(ICD)
        assertThat(curated[0].icdCodes.first().extensionCode).isNull()

        assertThat(curated[1].name).isEqualTo(CANNOT_CURATE)

        assertThat(evaluation.warnings).containsOnly(
            CurationWarning(
                PATIENT_ID, CurationCategory.INTOLERANCE, CANNOT_CURATE, "Could not find intolerance config for input 'Cannot curate'"
            )
        )
        assertThat(evaluation.comorbidityEvaluatedInputs).isEqualTo(inputs.map(String::lowercase).toSet())

    }
    companion object {
        val entry = IntoleranceEntry(
            subject = PATIENT_ID,
            assertedDate = LocalDate.now(),
            category = "",
            categoryAllergyCategoryDisplay = "",
            codeText = "",
            isSideEffect = "",
            clinicalStatus = "",
            verificationStatus = "",
            criticality = ""
        )
    }
}