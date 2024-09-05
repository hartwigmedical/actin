package com.hartwig.actin.datamodel.molecular

import com.hartwig.actin.datamodel.molecular.evidence.EvidenceDirection
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceTier
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory.treatment
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EvidenceTierTest {

    @Test
    fun `Should infer an evidence tier of I when A or B level evidence on-label`() {
        assertThat(evidenceTier(driverWithEvidence(EvidenceLevel.A))).isEqualTo(EvidenceTier.I)
        assertThat(evidenceTier(driverWithEvidence(EvidenceLevel.B))).isEqualTo(EvidenceTier.I)
    }

    @Test
    fun `Should infer an evidence tier of II when A or B level evidence off-label or is category variant`() {
        assertThat(evidenceTier(driverWithEvidence(EvidenceLevel.A, false))).isEqualTo(EvidenceTier.II)
        assertThat(evidenceTier(driverWithEvidence(EvidenceLevel.A, onLabel = true, isCategoryEvent = true))).isEqualTo(EvidenceTier.II)
        assertThat(evidenceTier(driverWithEvidence(EvidenceLevel.B, false))).isEqualTo(EvidenceTier.II)
    }

    @Test
    fun `Should infer an evidence tier of II when C or D level evidence off-label`() {
        assertThat(evidenceTier(driverWithEvidence(EvidenceLevel.C, false))).isEqualTo(EvidenceTier.II)
        assertThat(evidenceTier(driverWithEvidence(EvidenceLevel.D, false))).isEqualTo(EvidenceTier.II)
    }

    @Test
    fun `Should infer an evidence tier of III when no evidence found`() {
        assertThat(evidenceTier(mockDriver(emptySet()))).isEqualTo(EvidenceTier.III)
    }

    @Test
    fun `Should ignore category variants`() {
        assertThat(evidenceTier(driverWithEvidence(EvidenceLevel.A))).isEqualTo(EvidenceTier.I)
    }

    private fun driverWithEvidence(
        evidenceLevel: EvidenceLevel,
        onLabel: Boolean = true,
        approvalStatus: String = "CLINICAL STUDY",
        isCategoryEvent: Boolean = false
    ): Driver {
        return mockDriver(
            setOf(
                treatment(
                    "on-label",
                    evidenceLevel,
                    approvalStatus,
                    EvidenceDirection(hasPositiveResponse = true, isCertain = true),
                    onLabel,
                    isCategoryEvent
                )
            )
        )
    }

    private fun mockDriver(
        treatments: Set<TreatmentEvidence>
    ): Driver {
        val driver = mockk<Driver>()
        every { driver.evidence } returns TestClinicalEvidenceFactory.createEmptyClinicalEvidence()
            .copy(treatmentEvidence = treatments)
        return driver
    }
}