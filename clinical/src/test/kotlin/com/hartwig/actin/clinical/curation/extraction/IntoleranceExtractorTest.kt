package com.hartwig.actin.clinical.curation.extraction

import com.google.common.io.Resources
import com.hartwig.actin.clinical.WhoAtcModel
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.config.IntoleranceConfig
import com.hartwig.actin.clinical.feed.emc.intolerance.IntoleranceEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val PATIENT_ID = "patient1"

private const val INTOLERANCE_INPUT = "Intolerance input"

private const val CURATED_INTOLERANCE = "Curated intolerance"

private const val INTOLERANCE_MEDICATION_INPUT = "Paracetamol"

private const val CURATED_MEDICATION_INTOLERANCE = "Paracetamol"

private const val DOID = "1"

private const val ATC = "N02BE01"

private const val CANNOT_CURATE = "Cannot curate"

class IntoleranceExtractorTest {
    private val atcModel = WhoAtcModel.createFromFiles(
        Resources.getResource("atc_config/atc_tree.tsv").path,
        Resources.getResource("atc_config/atc_overrides.tsv").path
    )
    private val extractor = IntoleranceExtractor(
        TestCurationFactory.curationDatabase(
            IntoleranceConfig(
                input = INTOLERANCE_INPUT,
                ignore = false,
                name = CURATED_INTOLERANCE,
                doids = setOf(DOID),
                treatmentCategories = emptySet()
            ),
            IntoleranceConfig(
                input = INTOLERANCE_MEDICATION_INPUT,
                ignore = false,
                name = CURATED_MEDICATION_INTOLERANCE,
                doids = setOf(DOID),
                treatmentCategories = emptySet()
            )
        ), atcModel
    )

    @Test
    fun `Should extract curated intolerances`() {
        val inputs = listOf(INTOLERANCE_INPUT, CANNOT_CURATE)
        val (curated, evaluation) = extractor.extract(PATIENT_ID, inputs.map { entry.copy(codeText = it) })
        assertThat(curated).hasSize(2)
        assertThat(curated[0].name).isEqualTo(CURATED_INTOLERANCE)
        assertThat(curated[0].doids).contains(DOID)

        assertThat(curated[1].name).isEqualTo(CANNOT_CURATE)

        assertThat(evaluation.warnings).containsOnly(
            CurationWarning(
                PATIENT_ID, CurationCategory.INTOLERANCE, CANNOT_CURATE, "Could not find intolerance config for input 'Cannot curate'"
            )
        )
        assertThat(evaluation.intoleranceEvaluatedInputs).isEqualTo(inputs.map(String::lowercase).toSet())

    }

    @Test
    fun `Should extract curated medication intolerance`() {
        val (curated, _) = extractor.extract(
            PATIENT_ID,
            listOf(entry.copy(codeText = INTOLERANCE_MEDICATION_INPUT, category = "medication"))
        )
        assertThat(curated).hasSize(1)
        assertThat(curated[0].name).isEqualTo(CURATED_MEDICATION_INTOLERANCE)
        assertThat(curated[0].subcategories).isEqualTo(setOf(ATC))
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