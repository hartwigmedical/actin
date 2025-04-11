package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.configuration.ClinicalConfiguration
import com.hartwig.actin.datamodel.clinical.provided.ProvidedMolecularTest
import com.hartwig.actin.datamodel.clinical.provided.ProvidedMolecularTestResult
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val EHR_PATIENT_RECORD = EhrTestData.createEhrPatientRecord()
private val EHR_TREATMENT_HISTORY = EhrTestData.createEhrTreatmentHistory()

class DataQualityMaskTest {
    private val dataQualityMask = DataQualityMask(ClinicalConfiguration())

    @Test
    fun `Should remove all modifications from treatment history`() {
        val ehrPatientRecord =
            EHR_PATIENT_RECORD.copy(treatmentHistory = listOf(EHR_TREATMENT_HISTORY.copy(modifications = listOf(EhrTestData.createEhrModification()))))
        val result = dataQualityMask.apply(ehrPatientRecord)
        assertThat(result.treatmentHistory.flatMap { it.modifications!! }).isEmpty()
    }

    @Test
    fun `Should filter molecular test results when results are empty`() {
        val nonEmptyVariant = ProvidedMolecularTestResult(gene = "KRAS", hgvsProteinImpact = "G12C")
        val ehrPatientRecord =
            EHR_PATIENT_RECORD.copy(
                molecularTests = listOf(
                    ProvidedMolecularTest(
                        test = "test",
                        date = LocalDate.now(),
                        results = setOf(ProvidedMolecularTestResult(gene = "ALK"), nonEmptyVariant)
                    )
                )
            )
        val result = dataQualityMask.apply(ehrPatientRecord)
        assertThat(result.molecularTests[0].results).containsOnly(nonEmptyVariant)
    }

    @Test
    fun `Should scrub fields likely to be duplicated in prior other conditions when enabled in config`() {
        val result = DataQualityMask(ClinicalConfiguration(useOnlyPriorOtherConditions = true)).apply(EHR_PATIENT_RECORD)
        assertThat(result.treatmentHistory).isEmpty()
        assertThat(result.complications).isEmpty()
        assertThat(result.surgeries).isEmpty()
        assertThat(result.toxicities).isEmpty()
        assertThat(result.priorPrimaries).isEmpty()
        assertThat(result.tumorDetails.diagnosisDate).isNull()
        assertThat(result.tumorDetails.lesionSite).isNull()
    }
}