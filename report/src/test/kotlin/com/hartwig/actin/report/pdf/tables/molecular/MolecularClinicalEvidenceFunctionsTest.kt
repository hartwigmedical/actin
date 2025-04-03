package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombination
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombinationType
import com.hartwig.actin.datamodel.molecular.characteristics.MicrosatelliteStability
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalBurden
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalLoad
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val CLINICAL_EVIDENCE = TestClinicalEvidenceFactory.createEmpty()
private val BASE_MOLECULAR_TEST = TestMolecularFactory.createMinimalTestMolecularRecord()

class MolecularClinicalEvidenceFunctionsTest {

    @Test
    fun `Should return driver events and associated evidence`() {
        val variant = TestMolecularFactory.createProperVariant().copy(evidence = CLINICAL_EVIDENCE)
        val events =
            MolecularClinicalEvidenceFunctions.molecularEvidenceByEvent(
                molecularHistory(
                    BASE_MOLECULAR_TEST.copy(
                        drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = listOf(variant))
                    )
                )
            )
        assertThat(events).containsExactly(variant.event to CLINICAL_EVIDENCE)
    }

    @Test
    fun `Should return mss and msi events and associated evidence`() {
        assertThat(
            MolecularClinicalEvidenceFunctions.molecularEvidenceByEvent(
                molecularHistory(
                    BASE_MOLECULAR_TEST.copy(
                        characteristics = BASE_MOLECULAR_TEST.characteristics.copy(
                            microsatelliteStability = MicrosatelliteStability(
                                microsatelliteIndelsPerMb = null,
                                isUnstable = true,
                                evidence = CLINICAL_EVIDENCE
                            )
                        )
                    )
                )
            )
        ).containsExactly("MS Unstable" to CLINICAL_EVIDENCE)

        assertThat(
            MolecularClinicalEvidenceFunctions.molecularEvidenceByEvent(
                molecularHistory(
                    BASE_MOLECULAR_TEST.copy(
                        characteristics = BASE_MOLECULAR_TEST.characteristics.copy(
                            microsatelliteStability = MicrosatelliteStability(
                                microsatelliteIndelsPerMb = null,
                                isUnstable = false,
                                evidence = CLINICAL_EVIDENCE
                            )
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
                            characteristics = BASE_MOLECULAR_TEST.characteristics.copy(
                                homologousRecombination = HomologousRecombination(
                                    score = 1.0,
                                    isDeficient = true,
                                    type = HomologousRecombinationType.BRCA1_TYPE,
                                    brca1Value = 1.0,
                                    brca2Value = 0.0,
                                    evidence = CLINICAL_EVIDENCE
                                )
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
                            characteristics = BASE_MOLECULAR_TEST.characteristics.copy(
                                tumorMutationalBurden = TumorMutationalBurden(
                                    score = 61.0,
                                    isHigh = true,
                                    evidence = CLINICAL_EVIDENCE
                                )
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
                            characteristics = BASE_MOLECULAR_TEST.characteristics.copy(
                                tumorMutationalLoad = TumorMutationalLoad(
                                    score = 10,
                                    isHigh = true,
                                    evidence = CLINICAL_EVIDENCE
                                )
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
                            characteristics = BASE_MOLECULAR_TEST.characteristics.copy(
                                tumorMutationalBurden = TumorMutationalBurden(
                                    score = 61.0,
                                    isHigh = true,
                                    evidence = CLINICAL_EVIDENCE
                                )
                            )
                        ),
                        BASE_MOLECULAR_TEST.copy(
                            characteristics = BASE_MOLECULAR_TEST.characteristics.copy(
                                tumorMutationalBurden = TumorMutationalBurden(
                                    score = 61.0,
                                    isHigh = true,
                                    evidence = CLINICAL_EVIDENCE
                                )
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