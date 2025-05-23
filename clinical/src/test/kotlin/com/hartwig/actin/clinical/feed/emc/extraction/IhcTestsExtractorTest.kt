package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.config.IhcTestConfig
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationWarning
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val PATIENT_ID = "patient1"
private const val CANNOT_CURATE = "cannot curate"
private const val MOLECULAR_TEST_INPUT = "Molecular test input"

class IhcTestsExtractorTest {

    val extractor = IhcTestsExtractor(
        TestCurationFactory.curationDatabase(
            IhcTestConfig(
                input = MOLECULAR_TEST_INPUT,
                ignore = false,
                curated = IhcTest(
                    impliesPotentialIndeterminateStatus = false, item = "item"
                )
            )
        ),
        TestCurationFactory.curationDatabase(
            IhcTestConfig(
                input = MOLECULAR_TEST_INPUT,
                ignore = false,
                curated = IhcTest(
                    impliesPotentialIndeterminateStatus = false, item = "item 2"
                )
            )
        )
    )

    @Test
    fun `Should curate prior molecular tests`() {
        val ihcInputs = listOf(MOLECULAR_TEST_INPUT, CANNOT_CURATE)
        val pdl1Inputs = listOf(MOLECULAR_TEST_INPUT, CANNOT_CURATE)

        val questionnaire = TestCurationFactory.emptyQuestionnaire().copy(ihcTestResults = ihcInputs, pdl1TestResults = pdl1Inputs)
        val (molecularTests, evaluation) = extractor.extract(PATIENT_ID, questionnaire)
        assertThat(molecularTests).hasSize(2)

        assertThat(evaluation.warnings).containsExactly(
            CurationWarning(
                PATIENT_ID,
                CurationCategory.MOLECULAR_TEST_IHC,
                CANNOT_CURATE,
                "Could not find Molecular Test IHC config for input '$CANNOT_CURATE'"
            ),
            CurationWarning(
                PATIENT_ID,
                CurationCategory.MOLECULAR_TEST_PDL1,
                CANNOT_CURATE,
                "Could not find Molecular Test PDL1 config for input '$CANNOT_CURATE'"
            )
        )
        assertThat(evaluation.molecularTestEvaluatedInputs).isEqualTo((ihcInputs + pdl1Inputs).map(String::lowercase).toSet())
    }
}