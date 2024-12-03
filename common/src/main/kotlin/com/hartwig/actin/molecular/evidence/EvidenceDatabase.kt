package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.orange.driver.Disruption
import com.hartwig.actin.datamodel.molecular.orange.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.orange.driver.Virus
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventMatcher
import com.hartwig.actin.molecular.evidence.known.KnownEventResolver
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.serve.datamodel.molecular.common.GeneAlteration
import com.hartwig.serve.datamodel.molecular.fusion.KnownFusion

class EvidenceDatabase(
    private val knownEventResolver: KnownEventResolver,
    private val actionableEventMatcher: ActionableEventMatcher
) {

    fun evidenceForMicrosatelliteStatus(isMicrosatelliteUnstable: Boolean): ClinicalEvidence {
        return actionableEventMatcher.matchForMicrosatelliteStatus(isMicrosatelliteUnstable)
    }

    fun evidenceForHomologousRepairStatus(isHomologousRepairDeficient: Boolean): ClinicalEvidence {
        return actionableEventMatcher.matchForHomologousRepairStatus(isHomologousRepairDeficient)
    }

    fun evidenceForTumorMutationalBurdenStatus(hasHighTumorMutationalBurden: Boolean): ClinicalEvidence {
        return actionableEventMatcher.matchForHighTumorMutationalBurden(hasHighTumorMutationalBurden)
    }

    fun evidenceForTumorMutationalLoadStatus(hasHighTumorMutationalLoad: Boolean): ClinicalEvidence {
        return actionableEventMatcher.matchForHighTumorMutationalLoad(hasHighTumorMutationalLoad)
    }

    fun geneAlterationForVariant(variant: VariantMatchCriteria): GeneAlteration? {
        return knownEventResolver.resolveForVariant(variant)
    }

    fun evidenceForVariant(variant: VariantMatchCriteria): ClinicalEvidence {
        return actionableEventMatcher.matchForVariant(variant)
    }

    fun geneAlterationForCopyNumber(copyNumber: CopyNumber): GeneAlteration? {
        return knownEventResolver.resolveForCopyNumber(copyNumber)
    }

    fun evidenceForCopyNumber(copyNumber: CopyNumber): ClinicalEvidence {
        return actionableEventMatcher.matchForCopyNumber(copyNumber)
    }

    fun geneAlterationForHomozygousDisruption(homozygousDisruption: HomozygousDisruption): GeneAlteration? {
        return knownEventResolver.resolveForHomozygousDisruption(homozygousDisruption)
    }

    fun evidenceForHomozygousDisruption(homozygousDisruption: HomozygousDisruption): ClinicalEvidence {
        return actionableEventMatcher.matchForHomozygousDisruption(homozygousDisruption)
    }

    fun geneAlterationForBreakend(disruption: Disruption): GeneAlteration? {
        return knownEventResolver.resolveForBreakend(disruption)
    }

    fun evidenceForBreakend(disruption: Disruption): ClinicalEvidence {
        return actionableEventMatcher.matchForBreakend(disruption)
    }

    fun lookupKnownFusion(fusion: FusionMatchCriteria): KnownFusion? {
        return knownEventResolver.resolveForFusion(fusion)
    }

    fun evidenceForFusion(fusion: FusionMatchCriteria): ClinicalEvidence {
        return actionableEventMatcher.matchForFusion(fusion)
    }

    fun evidenceForVirus(virus: Virus): ClinicalEvidence {
        return actionableEventMatcher.matchForVirus(virus)
    }
}
