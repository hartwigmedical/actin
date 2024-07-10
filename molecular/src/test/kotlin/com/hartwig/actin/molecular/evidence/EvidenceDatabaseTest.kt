package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.molecular.TestMolecularFactory
import com.hartwig.actin.molecular.TestMolecularFactory.minimalCopyNumber
import com.hartwig.actin.molecular.TestMolecularFactory.minimalDisruption
import com.hartwig.actin.molecular.TestMolecularFactory.minimalVirus
import com.hartwig.actin.molecular.datamodel.CodingEffect
import com.hartwig.actin.molecular.datamodel.VariantType
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.orange.driver.FusionDriverType
import com.hartwig.actin.molecular.datamodel.orange.driver.VirusType
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EvidenceDatabaseTest {

    @Test
    fun `Should match evidence to signatures`() {
        // TODO (KZ): review EvidenceDatabase api to see if reasonable to remove nullability, then clean up !!'s here
        val database = TestEvidenceDatabaseFactory.createProperDatabase()
        assertThat(database.evidenceForMicrosatelliteStatus(null)).isNull()
        assertThat(evidenceCount(database.evidenceForMicrosatelliteStatus(false)!!)).isEqualTo(0)
        assertThat(evidenceCount(database.evidenceForMicrosatelliteStatus(true)!!)).isEqualTo(1)

        assertThat(database.evidenceForHomologousRepairStatus(null)).isNull()
        assertThat(evidenceCount(database.evidenceForHomologousRepairStatus(false)!!)).isEqualTo(0)
        assertThat(evidenceCount(database.evidenceForHomologousRepairStatus(true)!!)).isEqualTo(1)

        assertThat(database.evidenceForTumorMutationalBurdenStatus(null)).isNull()
        assertThat(evidenceCount(database.evidenceForTumorMutationalBurdenStatus(false)!!)).isEqualTo(0)
        assertThat(evidenceCount(database.evidenceForTumorMutationalBurdenStatus(true)!!)).isEqualTo(1)

        assertThat(database.evidenceForTumorMutationalLoadStatus(null)).isNull()
        assertThat(evidenceCount(database.evidenceForTumorMutationalLoadStatus(false)!!)).isEqualTo(0)
        assertThat(evidenceCount(database.evidenceForTumorMutationalLoadStatus(true)!!)).isEqualTo(1)
    }

    @Test
    fun `Should match evidence to drivers`() {
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
        assertThat(evidenceCount(database.evidenceForVariant(variant))).isEqualTo(1)

        val gainLoss = minimalCopyNumber().copy(type = CopyNumberType.LOSS)
        assertThat(database.geneAlterationForCopyNumber(gainLoss)).isNotNull()
        assertThat(evidenceCount(database.evidenceForCopyNumber(gainLoss))).isEqualTo(1)

        val homozygousDisruption = TestMolecularFactory.minimalHomozygousDisruption()
        assertThat(database.geneAlterationForHomozygousDisruption(homozygousDisruption)).isNotNull()
        assertThat(evidenceCount(database.evidenceForHomozygousDisruption(homozygousDisruption))).isEqualTo(2)

        val disruption = minimalDisruption().copy(isReportable = true)
        assertThat(database.geneAlterationForBreakend(disruption)).isNotNull()
        assertThat(evidenceCount(database.evidenceForBreakend(disruption))).isEqualTo(1)

        val fusion = FusionMatchCriteria(
            geneStart = "",
            fusedExonUp = 0,
            geneEnd = "",
            fusedExonDown = 0,
            driverType = FusionDriverType.NONE,
            isReportable = true,
        )
        assertThat(database.lookupKnownFusion(fusion)).isNotNull()
        assertThat(evidenceCount(database.evidenceForFusion(fusion))).isEqualTo(2)

        val virus = minimalVirus().copy(isReportable = true, type = VirusType.HUMAN_PAPILLOMA_VIRUS)
        assertThat(evidenceCount(database.evidenceForVirus(virus))).isEqualTo(1)
    }

    private fun evidenceCount(match: ActionabilityMatch): Int {
        return match.onLabelEvents.size + match.offLabelEvents.size
    }
}