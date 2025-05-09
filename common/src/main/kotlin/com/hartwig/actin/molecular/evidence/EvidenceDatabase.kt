package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.GeneAlteration
import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.VariantAlteration
import com.hartwig.actin.datamodel.molecular.driver.Virus
import com.hartwig.actin.molecular.evidence.actionability.ClinicalEvidenceMatcher
import com.hartwig.actin.molecular.evidence.known.KnownEventResolver
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.serve.datamodel.molecular.fusion.KnownFusion

class EvidenceDatabase(
    private val knownEventResolver: KnownEventResolver,
    private val clinicalEvidenceMatcher: ClinicalEvidenceMatcher
) {

    fun evidenceForMicrosatelliteStatus(isMicrosatelliteUnstable: Boolean): ClinicalEvidence {
        return clinicalEvidenceMatcher.matchForMicrosatelliteStatus(isMicrosatelliteUnstable)
    }

    fun evidenceForHomologousRecombinationStatus(isHomologousRecombinationDeficient: Boolean): ClinicalEvidence {
        return clinicalEvidenceMatcher.matchForHomologousRecombinationStatus(isHomologousRecombinationDeficient)
    }

    fun evidenceForTumorMutationalBurdenStatus(hasHighTumorMutationalBurden: Boolean): ClinicalEvidence {
        return clinicalEvidenceMatcher.matchForHighTumorMutationalBurden(hasHighTumorMutationalBurden)
    }

    fun evidenceForTumorMutationalLoadStatus(hasHighTumorMutationalLoad: Boolean): ClinicalEvidence {
        return clinicalEvidenceMatcher.matchForHighTumorMutationalLoad(hasHighTumorMutationalLoad)
    }

    fun alterationForVariant(variant: VariantMatchCriteria): VariantAlteration {
        return knownEventResolver.resolveForVariant(variant)
    }

    fun evidenceForVariant(variant: VariantMatchCriteria): ClinicalEvidence {
        return clinicalEvidenceMatcher.matchForVariant(variant)
    }

    fun alterationForCopyNumber(copyNumber: CopyNumber): GeneAlteration {
        return knownEventResolver.resolveForCopyNumber(copyNumber)
    }

    fun evidenceForCopyNumber(copyNumber: CopyNumber): ClinicalEvidence {
        return clinicalEvidenceMatcher.matchForCopyNumber(copyNumber)
    }

    fun alterationForHomozygousDisruption(homozygousDisruption: HomozygousDisruption): GeneAlteration {
        return knownEventResolver.resolveForHomozygousDisruption(homozygousDisruption)
    }

    fun evidenceForHomozygousDisruption(homozygousDisruption: HomozygousDisruption): ClinicalEvidence {
        return clinicalEvidenceMatcher.matchForHomozygousDisruption(homozygousDisruption)
    }

    fun alterationForDisruption(disruption: Disruption): GeneAlteration {
        return knownEventResolver.resolveForDisruption(disruption)
    }

    fun evidenceForDisruption(disruption: Disruption): ClinicalEvidence {
        return clinicalEvidenceMatcher.matchForDisruption(disruption)
    }

    fun lookupKnownFusion(fusion: FusionMatchCriteria): KnownFusion {
        return knownEventResolver.resolveForFusion(fusion)
    }

    fun evidenceForFusion(fusion: FusionMatchCriteria): ClinicalEvidence {
        return clinicalEvidenceMatcher.matchForFusion(fusion)
    }

    fun evidenceForVirus(virus: Virus): ClinicalEvidence {
        return clinicalEvidenceMatcher.matchForVirus(virus)
    }
}
