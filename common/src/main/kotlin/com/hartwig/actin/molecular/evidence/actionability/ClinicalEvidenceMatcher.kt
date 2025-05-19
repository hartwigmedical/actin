package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.Virus
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence

class ClinicalEvidenceMatcher(
    private val clinicalEvidenceFactory: ClinicalEvidenceFactory,
    private val variantEvidence: VariantEvidence,
    private val copyNumberEvidence: CopyNumberEvidence,
    private val disruptionEvidence: DisruptionEvidence,
    private val homozygousDisruptionEvidence: HomozygousDisruptionEvidence,
    private val fusionEvidence: FusionEvidence,
    private val virusEvidence: VirusEvidence,
    private val signatureEvidence: SignatureEvidence
) {

    fun matchForMicrosatelliteStatus(isMicrosatelliteUnstable: Boolean): ClinicalEvidence {
        return clinicalEvidenceFactory.create(signatureEvidence.findMicrosatelliteMatches(isMicrosatelliteUnstable))
    }

    fun matchForHomologousRecombinationStatus(isHomologousRecombinationDeficient: Boolean): ClinicalEvidence {
        return clinicalEvidenceFactory.create(signatureEvidence.findHomologousRecombinationMatches(isHomologousRecombinationDeficient))
    }

    fun matchForHighTumorMutationalBurden(hasHighTumorMutationalBurden: Boolean): ClinicalEvidence {
        return clinicalEvidenceFactory.create(signatureEvidence.findTumorBurdenMatches(hasHighTumorMutationalBurden))
    }

    fun matchForHighTumorMutationalLoad(hasHighTumorMutationalLoad: Boolean): ClinicalEvidence {
        return clinicalEvidenceFactory.create(signatureEvidence.findTumorLoadMatches(hasHighTumorMutationalLoad))
    }

    fun matchForVariant(variant: Variant): ClinicalEvidence {
        return clinicalEvidenceFactory.create(variantEvidence.findMatches(variant))
    }

    fun matchForCopyNumber(copyNumber: CopyNumber): ClinicalEvidence {
        return clinicalEvidenceFactory.create(copyNumberEvidence.findMatches(copyNumber))
    }

    fun matchForHomozygousDisruption(homozygousDisruption: HomozygousDisruption): ClinicalEvidence {
        return clinicalEvidenceFactory.create(homozygousDisruptionEvidence.findMatches(homozygousDisruption))
    }

    fun matchForDisruption(disruption: Disruption): ClinicalEvidence {
        return clinicalEvidenceFactory.create(disruptionEvidence.findMatches(disruption))
    }

    fun matchForFusion(fusion: Fusion): ClinicalEvidence {
        return clinicalEvidenceFactory.create(fusionEvidence.findMatches(fusion))
    }

    fun matchForVirus(virus: Virus): ClinicalEvidence {
        return clinicalEvidenceFactory.create(virusEvidence.findMatches(virus))
    }
}
