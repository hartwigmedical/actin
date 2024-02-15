package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.datamodel.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.driver.Disruption
import com.hartwig.actin.molecular.datamodel.driver.Fusion
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption
import com.hartwig.actin.molecular.datamodel.driver.Variant
import com.hartwig.actin.molecular.datamodel.driver.Virus

class ActionableEventMatcher internal constructor(
    private val personalizedActionabilityFactory: PersonalizedActionabilityFactory,
    private val signatureEvidence: SignatureEvidence, private val variantEvidence: VariantEvidence,
    private val copyNumberEvidence: CopyNumberEvidence, private val homozygousDisruptionEvidence: HomozygousDisruptionEvidence,
    private val breakendEvidence: BreakendEvidence, private val fusionEvidence: FusionEvidence,
    private val virusEvidence: VirusEvidence
) {

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

    fun matchForVariant(variant: Variant): ActionabilityMatch {
        return personalizedActionabilityFactory.create(variantEvidence.findMatches(variant))
    }

    fun matchForCopyNumber(copyNumber: CopyNumber): ActionabilityMatch {
        return personalizedActionabilityFactory.create(copyNumberEvidence.findMatches(copyNumber))
    }

    fun matchForHomozygousDisruption(homozygousDisruption: HomozygousDisruption): ActionabilityMatch {
        return personalizedActionabilityFactory.create(homozygousDisruptionEvidence.findMatches(homozygousDisruption))
    }

    fun matchForBreakend(disruption: Disruption): ActionabilityMatch {
        return personalizedActionabilityFactory.create(breakendEvidence.findMatches(disruption))
    }

    fun matchForFusion(fusion: Fusion): ActionabilityMatch {
        return personalizedActionabilityFactory.create(fusionEvidence.findMatches(fusion))
    }

    fun matchForVirus(virus: Virus): ActionabilityMatch {
        return personalizedActionabilityFactory.create(virusEvidence.findMatches(virus))
    }
}
