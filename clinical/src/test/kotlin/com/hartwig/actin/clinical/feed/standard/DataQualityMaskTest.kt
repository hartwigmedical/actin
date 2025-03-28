package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.datamodel.clinical.provided.ProvidedMolecularTest
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val EHR_PATIENT_RECORD = EhrTestData.createEhrPatientRecord()
private val EHR_TREATMENT_HISTORY = EhrTestData.createEhrTreatmentHistory()

class DataQualityMaskTest {

    private val panelGeneList = mockk<PanelGeneList>()
    private val dataQualityMask = DataQualityMask(panelGeneList)

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
        val result = DataQualityMask(panelGeneList).apply(ehrPatientRecord)
        val ngsTest = result.molecularTests[0]
        assertThat(ngsTest.testedGenes).containsExactly("EGFR", "additional_gene")
    }
}