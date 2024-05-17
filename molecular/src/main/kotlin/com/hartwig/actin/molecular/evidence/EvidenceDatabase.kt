package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.molecular.datamodel.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.driver.Disruption
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption
import com.hartwig.actin.molecular.datamodel.driver.Virus
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventMatcher
import com.hartwig.actin.molecular.evidence.known.KnownEventResolver
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.serve.datamodel.common.GeneAlteration
import com.hartwig.serve.datamodel.fusion.KnownFusion

class EvidenceDatabase internal constructor(
    private val knownEventResolver: KnownEventResolver,
    private val actionableEventMatcher: ActionableEventMatcher
) {

    fun evidenceForMicrosatelliteStatus(isMicrosatelliteUnstable: Boolean?): ActionabilityMatch? {
        return if (isMicrosatelliteUnstable == null) {
            null
        } else {
            actionableEventMatcher.matchForMicrosatelliteStatus(isMicrosatelliteUnstable)
        }
    }

    fun evidenceForHomologousRepairStatus(isHomologousRepairDeficient: Boolean?): ActionabilityMatch? {
        return if (isHomologousRepairDeficient == null) {
            null
        } else {
            actionableEventMatcher.matchForHomologousRepairStatus(isHomologousRepairDeficient)
        }
    }

    fun evidenceForTumorMutationalBurdenStatus(hasHighTumorMutationalBurden: Boolean?): ActionabilityMatch? {
        return if (hasHighTumorMutationalBurden == null) {
            null
        } else {
            actionableEventMatcher.matchForHighTumorMutationalBurden(hasHighTumorMutationalBurden)
        }
    }

    fun evidenceForTumorMutationalLoadStatus(hasHighTumorMutationalLoad: Boolean?): ActionabilityMatch? {
        return if (hasHighTumorMutationalLoad == null) {
            null
        } else {
            actionableEventMatcher.matchForHighTumorMutationalLoad(hasHighTumorMutationalLoad)
        }
    }

    fun geneAlterationForVariant(variant: VariantMatchCriteria): GeneAlteration? {
        return knownEventResolver.resolveForVariant(variant)
    }

    fun evidenceForVariant(variant: VariantMatchCriteria): ActionabilityMatch {
        return actionableEventMatcher.matchForVariant(variant)
    }

    fun geneAlterationForCopyNumber(copyNumber: CopyNumber): GeneAlteration? {
        return knownEventResolver.resolveForCopyNumber(copyNumber)
    }

    fun evidenceForCopyNumber(copyNumber: CopyNumber): ActionabilityMatch {
        return actionableEventMatcher.matchForCopyNumber(copyNumber)
    }

    fun geneAlterationForHomozygousDisruption(homozygousDisruption: HomozygousDisruption): GeneAlteration? {
        return knownEventResolver.resolveForHomozygousDisruption(homozygousDisruption)
    }

    fun evidenceForHomozygousDisruption(homozygousDisruption: HomozygousDisruption): ActionabilityMatch {
        return actionableEventMatcher.matchForHomozygousDisruption(homozygousDisruption)
    }

    fun geneAlterationForBreakend(disruption: Disruption): GeneAlteration? {
        return knownEventResolver.resolveForBreakend(disruption)
    }

    fun evidenceForBreakend(disruption: Disruption): ActionabilityMatch {
        return actionableEventMatcher.matchForBreakend(disruption)
    }

    fun lookupKnownFusion(fusion: FusionMatchCriteria): KnownFusion? {
        return knownEventResolver.resolveForFusion(fusion)
    }

    fun evidenceForFusion(fusion: FusionMatchCriteria): ActionabilityMatch {
        return actionableEventMatcher.matchForFusion(fusion)
    }

    fun evidenceForVirus(virus: Virus): ActionabilityMatch {
        return actionableEventMatcher.matchForVirus(virus)
    }
}
