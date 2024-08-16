package com.hartwig.actin.clinical.feed.standard

import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val EHR_PATIENT_RECORD = EhrTestData.createEhrPatientRecord()
private val EHR_TREATMENT_HISTORY = EhrTestData.createEhrTreatmentHistory()

class DataQualityMaskTest {

    @Test
    fun `Should remove all modifications from treatment history`() {
        val ehrPatientRecord =
            EHR_PATIENT_RECORD.copy(treatmentHistory = listOf(EHR_TREATMENT_HISTORY.copy(modifications = listOf(EhrTestData.createEhrModification()))))
        val result = DataQualityMask().apply(ehrPatientRecord)
        assertThat(result.treatmentHistory.flatMap { it.modifications!! }).isEmpty()
    }

    @Test
    fun `Should remove all NOT FOUND strings from molecular history`() {
        val ehrPatientRecord =
            EHR_PATIENT_RECORD.copy(
                molecularTests = listOf(
                    ProvidedMolecularTest(
                        test = "test",
                        date = LocalDate.now(),
                        results = setOf(ProvidedMolecularTestResult(hgvsProteinImpact = "NOT FOUND", hgvsCodingImpact = "NOT FOUND"))
                    )
                )
            )
        val result = DataQualityMask().apply(ehrPatientRecord)
        val testResult = result.molecularTests[0].results.iterator().next()
        assertThat(testResult.hgvsCodingImpact).isNull()
        assertThat(testResult.hgvsProteinImpact).isNull()
    }
}