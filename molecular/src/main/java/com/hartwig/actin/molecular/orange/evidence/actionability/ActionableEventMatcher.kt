package com.hartwig.actin.molecular.orange.evidence.actionability

import com.hartwig.hmftools.datamodel.linx.LinxBreakend
import com.hartwig.hmftools.datamodel.linx.LinxFusion
import com.hartwig.hmftools.datamodel.linx.LinxHomozygousDisruption
import com.hartwig.hmftools.datamodel.purple.PurpleGainLoss
import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.hmftools.datamodel.virus.VirusInterpreterEntry

class ActionableEventMatcher internal constructor(private val personalizedActionabilityFactory: PersonalizedActionabilityFactory,
                                                  private val signatureEvidence: SignatureEvidence, private val variantEvidence: VariantEvidence,
                                                  private val copyNumberEvidence: CopyNumberEvidence, private val homozygousDisruptionEvidence: HomozygousDisruptionEvidence,
                                                  private val breakendEvidence: BreakendEvidence, private val fusionEvidence: FusionEvidence,
                                                  private val virusEvidence: VirusEvidence) {
    fun matchForMicrosatelliteStatus(isMicrosatelliteUnstable: Boolean): ActionabilityMatch {
        return personalizedActionabilityFactory.create(signatureEvidence.findMicrosatelliteMatches(isMicrosatelliteUnstable))
    }

    fun matchForHomologousRepairStatus(isHomologousRepairDeficient: Boolean): ActionabilityMatch {
        return personalizedActionabilityFactory.create(signatureEvidence.findHomologousRepairMatches(isHomologousRepairDeficient))
    }

    fun matchForHighTumorMutationalBurden(hasHighTumorMutationalBurden: Boolean): ActionabilityMatch {
        return personalizedActionabilityFactory.create(signatureEvidence.findTumorBurdenMatches(hasHighTumorMutationalBurden))
    }

    fun matchForHighTumorMutationalLoad(hasHighTumorMutationalLoad: Boolean): ActionabilityMatch {
        return personalizedActionabilityFactory.create(signatureEvidence.findTumorLoadMatches(hasHighTumorMutationalLoad))
    }

    fun matchForVariant(variant: PurpleVariant): ActionabilityMatch {
        return personalizedActionabilityFactory.create(variantEvidence.findMatches(variant))
    }

    fun matchForCopyNumber(gainLoss: PurpleGainLoss): ActionabilityMatch {
        return personalizedActionabilityFactory.create(copyNumberEvidence.findMatches(gainLoss))
    }

    fun matchForHomozygousDisruption(linxHomozygousDisruption: LinxHomozygousDisruption): ActionabilityMatch {
        return personalizedActionabilityFactory.create(homozygousDisruptionEvidence.findMatches(linxHomozygousDisruption))
    }

    fun matchForBreakend(breakend: LinxBreakend): ActionabilityMatch {
        return personalizedActionabilityFactory.create(breakendEvidence.findMatches(breakend))
    }

    fun matchForFusion(fusion: LinxFusion): ActionabilityMatch {
        return personalizedActionabilityFactory.create(fusionEvidence.findMatches(fusion))
    }

    fun matchForVirus(virus: VirusInterpreterEntry): ActionabilityMatch {
        return personalizedActionabilityFactory.create(virusEvidence.findMatches(virus))
    }
}
