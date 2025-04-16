package com.hartwig.actin.datamodel.molecular

import com.hartwig.actin.datamodel.molecular.driver.Driver
import com.hartwig.actin.datamodel.molecular.driver.evidenceTier
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevelDetails
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceTier
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceType
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestEvidenceDirectionFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestTreatmentEvidenceFactory
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
        assertThat(evidenceTier(driverWithEvidence(EvidenceLevel.A, isOnLabel = false))).isEqualTo(EvidenceTier.II)
        assertThat(evidenceTier(driverWithEvidence(EvidenceLevel.A, isOnLabel = true, evidenceType = EvidenceType.ACTIVATION))).isEqualTo(EvidenceTier.II)
        assertThat(evidenceTier(driverWithEvidence(EvidenceLevel.B, isOnLabel = false))).isEqualTo(EvidenceTier.II)
    }

    @Test
    fun `Should infer an evidence tier of II when C or D level evidence off-label`() {
        assertThat(evidenceTier(driverWithEvidence(EvidenceLevel.C, isOnLabel = false))).isEqualTo(EvidenceTier.II)
        assertThat(evidenceTier(driverWithEvidence(EvidenceLevel.D, isOnLabel = false))).isEqualTo(EvidenceTier.II)
    }

    @Test
    fun `Should infer an evidence tier of III when no evidence found`() {
        assertThat(evidenceTier(mockDriver())).isEqualTo(EvidenceTier.III)
    }

    @Test
    fun `Should ignore category variants`() {
        assertThat(evidenceTier(driverWithEvidence(EvidenceLevel.A))).isEqualTo(EvidenceTier.I)
    }

    private fun driverWithEvidence(
        evidenceLevel: EvidenceLevel,
        isOnLabel: Boolean = true,
        evidenceLevelDetails: EvidenceLevelDetails = EvidenceLevelDetails.CLINICAL_STUDY,
        evidenceType: EvidenceType = EvidenceType.HOTSPOT_MUTATION,
    ): Driver {
        return mockDriver(
            TestTreatmentEvidenceFactory.create(
                treatment = "mock treatment",
                isOnLabel = isOnLabel,
                evidenceType = evidenceType,
                evidenceLevel = evidenceLevel,
                evidenceLevelDetails = evidenceLevelDetails,
                evidenceDirection = TestEvidenceDirectionFactory.certainPositiveResponse()
            )
        )
    }

    private fun mockDriver(treatmentEvidence: TreatmentEvidence? = null): Driver {
        val driver = mockk<Driver>()

        if (treatmentEvidence != null) {
            every { driver.evidence } returns TestClinicalEvidenceFactory.withEvidence(treatmentEvidence = treatmentEvidence)
        } else {
            every { driver.evidence } returns TestClinicalEvidenceFactory.createEmpty()
        }

        return driver
    }
}