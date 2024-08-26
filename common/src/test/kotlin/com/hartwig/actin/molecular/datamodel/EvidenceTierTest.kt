package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.molecular.datamodel.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.molecular.datamodel.evidence.TreatmentEvidence
import io.mockk.every
import io.mockk.mockk

class EvidenceTierTest {

   /* @Test
    fun `Should infer an evidence tier of I when A or B level evidence on-label`() {
        assertThat(evidenceTier(driverWithEvidence(EvidenceLevel.A, ActinEvidenceCategory.APPROVED))).isEqualTo(EvidenceTier.I)
        assertThat(evidenceTier(driverWithEvidence(EvidenceLevel.B, ActinEvidenceCategory.APPROVED))).isEqualTo(EvidenceTier.I)
    }

    @Test
    fun `Should infer an evidence tier of II when A or B level evidence off-label`() {
        assertThat(
            evidenceTier(
                driverWithEvidence(
                    EvidenceLevel.A,
                    ActinEvidenceCategory.OFF_LABEL_EXPERIMENTAL
                )
            )
        ).isEqualTo(EvidenceTier.II)
        assertThat(
            evidenceTier(
                driverWithEvidence(
                    EvidenceLevel.B,
                    ActinEvidenceCategory.OFF_LABEL_EXPERIMENTAL
                )
            )
        ).isEqualTo(EvidenceTier.II)
    }

    @Test
    fun `Should infer an evidence tier of II when C or D level evidence off-label`() {
        assertThat(evidenceTier(driverWithEvidence(EvidenceLevel.C, ActinEvidenceCategory.PRE_CLINICAL))).isEqualTo(EvidenceTier.II)
        assertThat(evidenceTier(driverWithEvidence(EvidenceLevel.D, ActinEvidenceCategory.PRE_CLINICAL))).isEqualTo(EvidenceTier.II)
    }

    @Test
    fun `Should infer an evidence tier of III when no evidence found`() {
        assertThat(evidenceTier(mockDriver(emptySet()))).isEqualTo(EvidenceTier.III)
    }

    private fun driverWithEvidence(evidenceLevel: EvidenceLevel, category: ActinEvidenceCategory): Driver {
        return mockDriver(setOf(treatment("on-label", evidenceLevel, category, Label.ON)))
    }*/

    private fun mockDriver(
        treatments: Set<TreatmentEvidence>
    ): Driver {
        val driver = mockk<Driver>()
        every { driver.evidence } returns TestClinicalEvidenceFactory.createEmpty()
            .copy(treatmentEvidence = treatments)
        return driver
    }
}