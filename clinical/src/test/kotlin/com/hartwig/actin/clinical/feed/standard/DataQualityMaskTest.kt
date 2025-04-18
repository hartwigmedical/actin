package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.configuration.ClinicalConfiguration
import com.hartwig.actin.datamodel.clinical.provided.ProvidedMolecularTest
import com.hartwig.actin.datamodel.clinical.provided.ProvidedMolecularTestResult
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val EHR_PATIENT_RECORD = EhrTestData.createEhrPatientRecord()
private val EHR_TREATMENT_HISTORY = EhrTestData.createEhrTreatmentHistory()

class DataQualityMaskTest {

    private val panelGeneList = mockk<PanelGeneList>()
    private val dataQualityMask = DataQualityMask(panelGeneList, ClinicalConfiguration())

    @Test
    fun `Should remove all modifications from treatment history`() {
        val ehrPatientRecord =
            EHR_PATIENT_RECORD.copy(treatmentHistory = listOf(EHR_TREATMENT_HISTORY.copy(modifications = listOf(EhrTestData.createEhrModification()))))
        val result = dataQualityMask.apply(ehrPatientRecord)
        assertThat(result.treatmentHistory.flatMap { it.modifications!! }).isEmpty()
    }

    @Test
    fun `Should add always tested genes for archer and ngs panels`() {
        val ehrPatientRecord = EHR_PATIENT_RECORD.copy(
            molecularTests = listOf(
                ProvidedMolecularTest(
                    test = "NGS panel", date = LocalDate.now(), testedGenes = setOf("additional_gene"), results = emptySet()
                )
            )
        )
        every { panelGeneList.listGenesForPanel("NGS panel") } returns setOf("EGFR")
        val result = dataQualityMask.apply(ehrPatientRecord)
        val ngsTest = result.molecularTests[0]
        assertThat(ngsTest.testedGenes).containsExactly("EGFR", "additional_gene")
    }

    @Test
    fun `Should filter molecular test results when results are empty`() {
        val nonEmptyVariant = ProvidedMolecularTestResult(gene = "KRAS", hgvsProteinImpact = "G12C")
        every { panelGeneList.listGenesForPanel("test") } returns setOf("KRAS")
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
        val result = DataQualityMask(panelGeneList, ClinicalConfiguration(useOnlyPriorOtherConditions = true)).apply(EHR_PATIENT_RECORD)
        assertThat(result.treatmentHistory).isEmpty()
        assertThat(result.complications).isEmpty()
        assertThat(result.surgeries).isEmpty()
        assertThat(result.toxicities).isEmpty()
        assertThat(result.priorPrimaries).isEmpty()
        assertThat(result.tumorDetails.diagnosisDate).isNull()
        assertThat(result.tumorDetails.lesionSite).isNull()
    }
}