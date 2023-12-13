package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.WhoAtcModel
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.feed.intolerance.IntoleranceEntry
import org.assertj.core.api.Assertions.assertThat
import com.google.common.io.Resources
import org.junit.Test
import java.time.LocalDate

private const val PATIENT_ID = "patient1"
private const val CANNOT_CURATE = "Cannot curate"

class IntoleranceExtractorTest {
    private val atcModel = WhoAtcModel.createFromFile(Resources.getResource("atc_config/atc_tree.tsv").path)
    private val extractor = IntoleranceExtractor(TestCurationFactory.createProperTestCurationDatabase(), atcModel)

    @Test
    fun `Should curate intolerances`() {
        val inputs = listOf("Latex type 1", CANNOT_CURATE)
        val (curated, evaluation) = extractor.extract(PATIENT_ID, inputs.map { entry.copy(codeText = it) })
        assertThat(curated).hasSize(2)
        assertThat(curated[0].name()).isEqualTo("Latex (type 1)")
        assertThat(curated[0].doids()).contains("0060532")

        assertThat(curated[1].name()).isEqualTo(CANNOT_CURATE)

        assertThat(evaluation.warnings).containsOnly(
            CurationWarning(
                PATIENT_ID, CurationCategory.INTOLERANCE, CANNOT_CURATE, "Could not find intolerance config for input 'Cannot curate'"
            )
        )
        assertThat(evaluation.intoleranceEvaluatedInputs).isEqualTo(inputs.map(String::lowercase).toSet())

    }

    @Test
    fun `Should curate medication intolerance`() {
        val (curated, _) = extractor.extract(PATIENT_ID, listOf(entry.copy(codeText = "Paracetamol", category= "medication")))
        assertThat(curated).hasSize(1)
        assertThat(curated[0].name()).isEqualTo("Paracetamol")
        assertThat(curated[0].subcategories()).isEqualTo(setOf("N02BE01"))
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