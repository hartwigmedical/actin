package com.hartwig.actin.molecular.orange.evidence

import com.hartwig.actin.molecular.orange.evidence.actionability.ActionabilityMatch
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionableEventMatcher
import com.hartwig.actin.molecular.orange.evidence.known.KnownEventResolver
import com.hartwig.hmftools.datamodel.linx.LinxBreakend
import com.hartwig.hmftools.datamodel.linx.LinxFusion
import com.hartwig.hmftools.datamodel.linx.LinxHomozygousDisruption
import com.hartwig.hmftools.datamodel.purple.PurpleGainLoss
import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.hmftools.datamodel.virus.VirusInterpreterEntry
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

    fun geneAlterationForVariant(variant: PurpleVariant): GeneAlteration? {
        return knownEventResolver.resolveForVariant(variant)
    }

    fun evidenceForVariant(variant: PurpleVariant): ActionabilityMatch {
        return actionableEventMatcher.matchForVariant(variant)
    }

    fun geneAlterationForCopyNumber(gainLoss: PurpleGainLoss): GeneAlteration? {
        return knownEventResolver.resolveForCopyNumber(gainLoss)
    }

    fun evidenceForCopyNumber(gainLoss: PurpleGainLoss): ActionabilityMatch {
        return actionableEventMatcher.matchForCopyNumber(gainLoss)
    }

    fun geneAlterationForHomozygousDisruption(linxHomozygousDisruption: LinxHomozygousDisruption): GeneAlteration? {
        return knownEventResolver.resolveForHomozygousDisruption(linxHomozygousDisruption)
    }

    fun evidenceForHomozygousDisruption(linxHomozygousDisruption: LinxHomozygousDisruption): ActionabilityMatch {
        return actionableEventMatcher.matchForHomozygousDisruption(linxHomozygousDisruption)
    }

    fun geneAlterationForBreakend(breakend: LinxBreakend): GeneAlteration? {
        return knownEventResolver.resolveForBreakend(breakend)
    }

    fun evidenceForBreakend(breakend: LinxBreakend): ActionabilityMatch {
        return actionableEventMatcher.matchForBreakend(breakend)
    }

    fun lookupKnownFusion(fusion: LinxFusion): KnownFusion? {
        return knownEventResolver.resolveForFusion(fusion)
    }

    fun evidenceForFusion(fusion: LinxFusion): ActionabilityMatch {
        return actionableEventMatcher.matchForFusion(fusion)
    }

    fun evidenceForVirus(virus: VirusInterpreterEntry): ActionabilityMatch {
        return actionableEventMatcher.matchForVirus(virus)
    }
}
