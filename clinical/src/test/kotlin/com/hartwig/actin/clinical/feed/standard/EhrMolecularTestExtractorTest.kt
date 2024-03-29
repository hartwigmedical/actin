package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.config.MolecularTestConfig
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.clinical.feed.standard.EhrTestData.createEhrPatientRecord
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val IHC_LINE = "HER2 immunohistochemie: negative"
private val PRIOR_IHC_TEST =
    PriorMolecularTest(test = "IHC", item = "HER2", measure = "negative", impliesPotentialIndeterminateStatus = true)
private val PATHOLOGY_REPORT =
    "Microscopie:\n\test\n\nConclusie:\n\nunrelated.\r\n\r\n\r\n$IHC_LINE\n\n"
private val PRIOR_MOLECULAR_TEST =
    PriorMolecularTest(test = "Archer FP Lung Target", item = "EGFR", measure = "c.2573T>G", measureDate = LocalDate.parse("2024-03-25"), impliesPotentialIndeterminateStatus = false)
private val EHR_OTHER_MOLECULAR_TEST =
    EhrMolecularTest(type = "Archer FP Lung Target", measure = "EGFR", result = "c.2573T>G", resultDate = LocalDate.parse("2024-03-25"))

private val EHR_PATIENT_RECORD = createEhrPatientRecord()
private val EHR_PATIENT_RECORD_WITH_PATHOLOGY =
    EHR_PATIENT_RECORD.copy(tumorDetails = EHR_PATIENT_RECORD.tumorDetails.copy(tumorGradeDifferentiation = PATHOLOGY_REPORT))
private val EHR_PATIENT_RECORD_WITH_OTHER_MOLECULAR_TEST =
    EHR_PATIENT_RECORD.copy(molecularTestHistory = listOf(EHR_OTHER_MOLECULAR_TEST))
private val EHR_PATIENT_RECORD_WITH_PATHOLOGY_AND_MOLECULAR =
    EHR_PATIENT_RECORD_WITH_PATHOLOGY.copy(molecularTestHistory = listOf(EHR_OTHER_MOLECULAR_TEST))

class EhrMolecularTestExtractorTest {

    private val molecularTestIhcCuration = mockk<CurationDatabase<MolecularTestConfig>>()
    private val extractor = EhrMolecularTestExtractor(molecularTestIhcCuration)

    @Test
    fun `Should extract and curate IHC lines from tumor grade differentiation`() {
        every { molecularTestIhcCuration.find(IHC_LINE) } returns setOf(
            MolecularTestConfig(
                input = IHC_LINE,
                curated = PRIOR_IHC_TEST
            )
        )
        val result = extractor.extract(EHR_PATIENT_RECORD_WITH_PATHOLOGY)
        assertThat(result.extracted).containsExactly(PRIOR_IHC_TEST)
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should extract other molecular tests`() {
        val result = extractor.extract(EHR_PATIENT_RECORD_WITH_OTHER_MOLECULAR_TEST)
        assertThat(result.extracted).containsExactly(PRIOR_MOLECULAR_TEST)
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should be able to extract IHC and other molecular tests simultaneously`() {
        every { molecularTestIhcCuration.find(IHC_LINE) } returns setOf(
            MolecularTestConfig(
                input = IHC_LINE,
                curated = PRIOR_IHC_TEST
            )
        )
        val result = extractor.extract(EHR_PATIENT_RECORD_WITH_PATHOLOGY_AND_MOLECULAR)
        assertThat(result.extracted).containsExactly(PRIOR_IHC_TEST, PRIOR_MOLECULAR_TEST)
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should ignore lines if ignored in curation`() {
        every { molecularTestIhcCuration.find(IHC_LINE) } returns setOf(
            MolecularTestConfig(
                input = IHC_LINE,
                ignore = true
            )
        )
        val result =
            extractor.extract(EHR_PATIENT_RECORD_WITH_PATHOLOGY)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should give curation warnings for uncurated lines`() {
        every { molecularTestIhcCuration.find(IHC_LINE) } returns emptySet()
        val result =
            extractor.extract(EHR_PATIENT_RECORD_WITH_PATHOLOGY)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                patientId = HASHED_ID_IN_BASE64,
                category = CurationCategory.MOLECULAR_TEST_IHC,
                feedInput = "HER2 immunohistochemie: negative",
                message = "Could not find molecular test ihc config for input 'HER2 immunohistochemie: negative'"
            )
        )
    }
}