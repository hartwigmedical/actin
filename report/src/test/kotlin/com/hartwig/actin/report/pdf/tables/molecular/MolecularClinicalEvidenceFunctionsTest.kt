package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val CLINICAL_EVIDENCE = TestClinicalEvidenceFactory.createEmpty()
private val BASE_MOLECULAR_TEST = TestMolecularFactory.createMinimalTestOrangeRecord()

class MolecularClinicalEvidenceFunctionsTest {

    @Test
    fun `Should return driver events and associated evidence`() {
        val variant = TestMolecularFactory.createProperVariant().copy(evidence = CLINICAL_EVIDENCE)
        val events =
            MolecularClinicalEvidenceFunctions.molecularEvidenceByEvent(
                molecularHistory(BASE_MOLECULAR_TEST.copy(drivers = Drivers(variants = listOf(variant))))
            )
        assertThat(events).containsExactly(variant.event to CLINICAL_EVIDENCE)
    }

    @Test
    fun `Should return mss and msi events and associated evidence`() {
        assertThat(
            MolecularClinicalEvidenceFunctions.molecularEvidenceByEvent(
                molecularHistory(
                    BASE_MOLECULAR_TEST.copy(
                        characteristics = MolecularCharacteristics(
                            isMicrosatelliteUnstable = true,
                            microsatelliteEvidence = CLINICAL_EVIDENCE
                        )
                    )
                )
            )
        ).containsExactly("MS Unstable" to CLINICAL_EVIDENCE)
        assertThat(
            MolecularClinicalEvidenceFunctions.molecularEvidenceByEvent(
                molecularHistory(
                    BASE_MOLECULAR_TEST.copy(
                        characteristics = MolecularCharacteristics(
                            isMicrosatelliteUnstable = false,
                            microsatelliteEvidence = CLINICAL_EVIDENCE
                        )
                    )
                )
            )
        ).containsExactly("MS Stable" to CLINICAL_EVIDENCE)
    }

    @Test
    fun `Should return hrd and hrp events and associated evidence`() {
        assertThat(
            MolecularClinicalEvidenceFunctions.molecularEvidenceByEvent(
                MolecularHistory(
                    molecularTests = listOf(
                        BASE_MOLECULAR_TEST.copy(
                            characteristics = MolecularCharacteristics(
                                isHomologousRepairDeficient = true,
                                homologousRepairScore = 1.0,
                                homologousRepairEvidence = CLINICAL_EVIDENCE
                            )
                        )
                    )
                )
            )
        ).containsExactly("HR Deficient (1)" to CLINICAL_EVIDENCE)
    }

    @Test
    fun `Should return tml and tmb events and associated evidence`() {
        assertThat(
            MolecularClinicalEvidenceFunctions.molecularEvidenceByEvent(
                MolecularHistory(
                    molecularTests = listOf(
                        BASE_MOLECULAR_TEST.copy(
                            characteristics = MolecularCharacteristics(
                                tumorMutationalBurden = 61.0,
                                hasHighTumorMutationalBurden = true,
                                tumorMutationalBurdenEvidence = CLINICAL_EVIDENCE
                            )
                        )
                    )
                )
            )
        ).containsExactly("TMB High" to CLINICAL_EVIDENCE)

        assertThat(
            MolecularClinicalEvidenceFunctions.molecularEvidenceByEvent(
                MolecularHistory(
                    molecularTests = listOf(
                        BASE_MOLECULAR_TEST.copy(
                            characteristics = MolecularCharacteristics(
                                tumorMutationalLoad = 10,
                                hasHighTumorMutationalLoad = true,
                                tumorMutationalLoadEvidence = CLINICAL_EVIDENCE
                            )
                        )
                    )
                )
            )
        ).containsExactly("TML High" to CLINICAL_EVIDENCE)
    }

    @Test
    fun `Should remove any duplicate events and evidence`() {
        assertThat(
            MolecularClinicalEvidenceFunctions.molecularEvidenceByEvent(
                MolecularHistory(
                    molecularTests = listOf(
                        BASE_MOLECULAR_TEST.copy(
                            characteristics = MolecularCharacteristics(
                                tumorMutationalBurden = 61.0,
                                hasHighTumorMutationalBurden = true,
                                tumorMutationalBurdenEvidence = CLINICAL_EVIDENCE
                            )
                        ),
                        BASE_MOLECULAR_TEST.copy(
                            characteristics = MolecularCharacteristics(
                                tumorMutationalBurden = 61.0,
                                hasHighTumorMutationalBurden = true,
                                tumorMutationalBurdenEvidence = CLINICAL_EVIDENCE
                            )
                        )
                    )
                )
            )
        ).containsExactly("TMB High" to CLINICAL_EVIDENCE)
    }

    private fun molecularHistory(molecularRecord: MolecularRecord) = MolecularHistory(
        molecularTests = listOf(molecularRecord)
    )
}