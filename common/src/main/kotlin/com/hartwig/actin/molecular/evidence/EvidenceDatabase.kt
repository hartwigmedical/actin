package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.orange.driver.Disruption
import com.hartwig.actin.datamodel.molecular.orange.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.orange.driver.Virus
import com.hartwig.actin.molecular.evidence.actionability.ClinicalEvidenceMatcher
import com.hartwig.actin.molecular.evidence.known.KnownEventResolver
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.serve.datamodel.molecular.common.GeneAlteration
import com.hartwig.serve.datamodel.molecular.fusion.KnownFusion

class EvidenceDatabase(
    private val knownEventResolver: KnownEventResolver,
    private val clinicalEvidenceMatcher: ClinicalEvidenceMatcher
) {

    fun evidenceForMicrosatelliteStatus(isMicrosatelliteUnstable: Boolean): ClinicalEvidence {
        return clinicalEvidenceMatcher.matchForMicrosatelliteStatus(isMicrosatelliteUnstable)
    }

    fun evidenceForHomologousRepairStatus(isHomologousRepairDeficient: Boolean): ClinicalEvidence {
        return clinicalEvidenceMatcher.matchForHomologousRepairStatus(isHomologousRepairDeficient)
    }

    fun evidenceForTumorMutationalBurdenStatus(hasHighTumorMutationalBurden: Boolean): ClinicalEvidence {
        return clinicalEvidenceMatcher.matchForHighTumorMutationalBurden(hasHighTumorMutationalBurden)
    }

    fun evidenceForTumorMutationalLoadStatus(hasHighTumorMutationalLoad: Boolean): ClinicalEvidence {
        return clinicalEvidenceMatcher.matchForHighTumorMutationalLoad(hasHighTumorMutationalLoad)
    }

    fun geneAlterationForVariant(variant: VariantMatchCriteria): GeneAlteration? {
        return knownEventResolver.resolveForVariant(variant)
    }

    fun evidenceForVariant(variant: VariantMatchCriteria): ClinicalEvidence {
        return clinicalEvidenceMatcher.matchForVariant(variant)
    }

    fun geneAlterationForCopyNumber(copyNumber: CopyNumber): GeneAlteration? {
        return knownEventResolver.resolveForCopyNumber(copyNumber)
    }

    fun evidenceForCopyNumber(copyNumber: CopyNumber): ClinicalEvidence {
        return clinicalEvidenceMatcher.matchForCopyNumber(copyNumber)
    }

    fun geneAlterationForHomozygousDisruption(homozygousDisruption: HomozygousDisruption): GeneAlteration? {
        return knownEventResolver.resolveForHomozygousDisruption(homozygousDisruption)
    }

    fun evidenceForHomozygousDisruption(homozygousDisruption: HomozygousDisruption): ClinicalEvidence {
        return clinicalEvidenceMatcher.matchForHomozygousDisruption(homozygousDisruption)
    }

    fun geneAlterationForBreakend(disruption: Disruption): GeneAlteration? {
        return knownEventResolver.resolveForBreakend(disruption)
    }

    fun evidenceForBreakend(disruption: Disruption): ClinicalEvidence {
        return clinicalEvidenceMatcher.matchForBreakend(disruption)
    }

    fun lookupKnownFusion(fusion: FusionMatchCriteria): KnownFusion? {
        return knownEventResolver.resolveForFusion(fusion)
    }

    fun evidenceForFusion(fusion: FusionMatchCriteria): ClinicalEvidence {
        return clinicalEvidenceMatcher.matchForFusion(fusion)
    }

    fun evidenceForVirus(virus: Virus): ClinicalEvidence {
        return clinicalEvidenceMatcher.matchForVirus(virus)
    }
}
