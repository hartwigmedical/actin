package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.molecular.datamodel.CodingEffect
import com.hartwig.actin.molecular.datamodel.VariantType
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.orange.driver.FusionDriverType
import com.hartwig.actin.molecular.datamodel.orange.driver.VirusType
import com.hartwig.actin.molecular.evidence.TestMolecularFactory.minimalCopyNumber
import com.hartwig.actin.molecular.evidence.TestMolecularFactory.minimalDisruption
import com.hartwig.actin.molecular.evidence.TestMolecularFactory.minimalVirus
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EvidenceDatabaseTest {

    @Test
    fun `Should match evidence for signatures`() {
        val database = TestEvidenceDatabaseFactory.createProperDatabase()
        assertEvidenceCountMatchesExpected(database.evidenceForMicrosatelliteStatus(false), 0)
        assertEvidenceCountMatchesExpected(database.evidenceForMicrosatelliteStatus(true), 1)
        assertEvidenceCountMatchesExpected(database.evidenceForHomologousRepairStatus(false), 0)
        assertEvidenceCountMatchesExpected(database.evidenceForHomologousRepairStatus(true), 1)
        assertEvidenceCountMatchesExpected(database.evidenceForTumorMutationalBurdenStatus(false), 0)
        assertEvidenceCountMatchesExpected(database.evidenceForTumorMutationalBurdenStatus(true), 1)
        assertEvidenceCountMatchesExpected(database.evidenceForTumorMutationalLoadStatus(false), 0)
        assertEvidenceCountMatchesExpected(database.evidenceForTumorMutationalLoadStatus(true), 1)
    }

    @Test
    fun `Should match evidence for drivers`() {
        val database = TestEvidenceDatabaseFactory.createProperDatabase()
        // Assume default objects match with default SERVE objects
        val variant = VariantMatchCriteria(
            chromosome = "",
            position = 0,
            ref = "",
            alt = "",
            isReportable = true,
            type = VariantType.SNV,
            gene = "",
            codingEffect = CodingEffect.NONE,
        )
        assertThat(database.geneAlterationForVariant(variant)).isNotNull
        assertEvidenceCountMatchesExpected(database.evidenceForVariant(variant), 1)

        val gainLoss = minimalCopyNumber().copy(type = CopyNumberType.LOSS)
        assertThat(database.geneAlterationForCopyNumber(gainLoss)).isNotNull
        assertEvidenceCountMatchesExpected(database.evidenceForCopyNumber(gainLoss), 1)

        val homozygousDisruption = TestMolecularFactory.minimalHomozygousDisruption()
        assertThat(database.geneAlterationForHomozygousDisruption(homozygousDisruption)).isNotNull
        assertEvidenceCountMatchesExpected(database.evidenceForHomozygousDisruption(homozygousDisruption), 2)

        val disruption = minimalDisruption().copy(isReportable = true)
        assertThat(database.geneAlterationForBreakend(disruption)).isNotNull
        assertEvidenceCountMatchesExpected(database.evidenceForBreakend(disruption), 1)

        val fusion = FusionMatchCriteria(
            geneStart = "",
            fusedExonUp = 0,
            geneEnd = "",
            fusedExonDown = 0,
            driverType = FusionDriverType.NONE,
            isReportable = true,
        )
        assertThat(database.lookupKnownFusion(fusion)).isNotNull
        assertEvidenceCountMatchesExpected(database.evidenceForFusion(fusion), 2)

        val virus = minimalVirus().copy(isReportable = true, type = VirusType.HUMAN_PAPILLOMA_VIRUS)
        assertEvidenceCountMatchesExpected(database.evidenceForVirus(virus), 1)
    }

    private fun assertEvidenceCountMatchesExpected(match: ActionabilityMatch, expectedCount: Int) {
        assertThat(match.onLabelEvents.size + match.offLabelEvents.size).isEqualTo(expectedCount)
    }
}