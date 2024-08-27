package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.molecular.datamodel.evidence.EvidenceDirection
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceLevel
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceTier
import com.hartwig.actin.molecular.datamodel.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.molecular.datamodel.evidence.TestClinicalEvidenceFactory.treatment
import com.hartwig.actin.molecular.datamodel.evidence.TreatmentEvidence
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
    fun `Should infer an evidence tier of II when A or B level evidence off-label`() {
        assertThat(evidenceTier(driverWithEvidence(EvidenceLevel.A, false))).isEqualTo(EvidenceTier.II)
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

    private fun driverWithEvidence(evidenceLevel: EvidenceLevel, onLabel: Boolean = true): Driver {
        return mockDriver(
            setOf(
                treatment(
                    "on-label",
                    evidenceLevel,
                    EvidenceDirection(hasPositiveResponse = true, isCertain = true),
                    onLabel
                )
            )
        )
    }

    private fun mockDriver(
        treatments: Set<TreatmentEvidence>
    ): Driver {
        val driver = mockk<Driver>()
        every { driver.evidence } returns TestClinicalEvidenceFactory.createEmpty()
            .copy(treatmentEvidence = treatments)
        return driver
    }
}