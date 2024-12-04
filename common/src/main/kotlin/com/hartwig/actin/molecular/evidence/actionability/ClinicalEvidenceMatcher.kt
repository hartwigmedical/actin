package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.orange.driver.Disruption
import com.hartwig.actin.datamodel.molecular.orange.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.orange.driver.Virus
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria

class ClinicalEvidenceMatcher internal constructor(
    private val personalizedActionabilityFactory: PersonalizedActionabilityFactory,
    private val variantEvidence: VariantEvidence,
    private val copyNumberEvidence: CopyNumberEvidence,
    private val breakendEvidence: BreakendEvidence,
    private val homozygousDisruptionEvidence: HomozygousDisruptionEvidence,
    private val fusionEvidence: FusionEvidence,
    private val virusEvidence: VirusEvidence,
    private val signatureEvidence: SignatureEvidence
) {

    fun matchForMicrosatelliteStatus(isMicrosatelliteUnstable: Boolean): ClinicalEvidence {
        return personalizedActionabilityFactory.create(signatureEvidence.findMicrosatelliteMatches(isMicrosatelliteUnstable))
    }

    fun matchForHomologousRepairStatus(isHomologousRepairDeficient: Boolean): ClinicalEvidence {
        return personalizedActionabilityFactory.create(signatureEvidence.findHomologousRepairMatches(isHomologousRepairDeficient))
    }

    fun matchForHighTumorMutationalBurden(hasHighTumorMutationalBurden: Boolean): ClinicalEvidence {
        return personalizedActionabilityFactory.create(signatureEvidence.findTumorBurdenMatches(hasHighTumorMutationalBurden))
    }

    fun matchForHighTumorMutationalLoad(hasHighTumorMutationalLoad: Boolean): ClinicalEvidence {
        return personalizedActionabilityFactory.create(signatureEvidence.findTumorLoadMatches(hasHighTumorMutationalLoad))
    }

    fun matchForVariant(variant: VariantMatchCriteria): ClinicalEvidence {
        return personalizedActionabilityFactory.create(variantEvidence.findMatches(variant))
    }

    fun matchForCopyNumber(copyNumber: CopyNumber): ClinicalEvidence {
        return personalizedActionabilityFactory.create(copyNumberEvidence.findMatches(copyNumber))
    }

    fun matchForHomozygousDisruption(homozygousDisruption: HomozygousDisruption): ClinicalEvidence {
        return personalizedActionabilityFactory.create(homozygousDisruptionEvidence.findMatches(homozygousDisruption))
    }

    fun matchForBreakend(disruption: Disruption): ClinicalEvidence {
        return personalizedActionabilityFactory.create(breakendEvidence.findMatches(disruption))
    }

    fun matchForFusion(fusion: FusionMatchCriteria): ClinicalEvidence {
        return personalizedActionabilityFactory.create(fusionEvidence.findMatches(fusion))
    }

    fun matchForVirus(virus: Virus): ClinicalEvidence {
        return personalizedActionabilityFactory.create(virusEvidence.findMatches(virus))
    }
}
