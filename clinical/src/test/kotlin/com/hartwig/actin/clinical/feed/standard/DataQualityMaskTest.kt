package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.datamodel.clinical.provided.ProvidedMolecularTest
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
    fun `Should add always tested genes for archer and ngs panels`() {
        val ehrPatientRecord =
            EHR_PATIENT_RECORD.copy(
                molecularTests = listOf(
                    ProvidedMolecularTest(
                        test = "Archer FusionPlex",
                        date = LocalDate.now(),
                        testedGenes = setOf("additional_gene"),
                        results = emptySet()
                    ),
                    ProvidedMolecularTest(
                        test = "NGS panel",
                        date = LocalDate.now(),
                        testedGenes = setOf("additional_gene"),
                        results = emptySet()
                    )
                )
            )
        val result = DataQualityMask().apply(ehrPatientRecord)
        val archerTest = result.molecularTests[0]
        val ngsTest = result.molecularTests[1]
        assertThat(archerTest.testedGenes).containsExactly(
            "ALK",
            "ROS1",
            "RET",
            "MET",
            "NTRK1",
            "NTRK2",
            "NTRK3",
            "NRG1",
            "additional_gene"
        )
        assertThat(ngsTest.testedGenes).containsExactly("EGFR", "BRAF", "KRAS", "additional_gene")
    }
}