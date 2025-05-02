package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.molecular.evidence.curation.ApplicabilityFiltering
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.trial.ActionableTrial
import com.hartwig.serve.datamodel.trial.ImmutableActionableTrial

class ClinicalEvidenceMatcherFactory(
    private val doidModel: DoidModel,
    private val tumorDoids: Set<String>
) {

    fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): ClinicalEvidenceMatcher {
        val filteredEvidences = evidences
            .filter { it.source() == ActionabilityConstants.EVIDENCE_SOURCE }
            .filter { isMolecularCriteriumApplicable(it.molecularCriterium()) }

        val curatedTrials = trials
            .filter { it.source() == ActionabilityConstants.EXTERNAL_TRIAL_SOURCE }
            .mapNotNull { trial -> removeNonApplicableMolecularCriteria(trial) }
        return create(
            ClinicalEvidenceFactory(EvidenceCancerTypeResolver.create(doidModel, tumorDoids)),
            filteredEvidences,
            curatedTrials
        )
    }

    private fun isMolecularCriteriumApplicable(molecularCriterium: MolecularCriterium): Boolean {
        with(molecularCriterium) {
            return when {
                hotspots().isNotEmpty() -> ApplicabilityFiltering.isApplicable(hotspots().first())
                codons().isNotEmpty() -> ApplicabilityFiltering.isApplicable(codons().first())
                exons().isNotEmpty() -> ApplicabilityFiltering.isApplicable(exons().first())
                genes().isNotEmpty() -> ApplicabilityFiltering.isApplicable(genes().first())
                else -> true
            }
        }
    }

    private fun removeNonApplicableMolecularCriteria(trial: ActionableTrial): ActionableTrial? {
        val applicableMolecularCriteria = trial.anyMolecularCriteria().filter { isMolecularCriteriumApplicable(it) }

        return if (applicableMolecularCriteria.isNotEmpty()) {
            ImmutableActionableTrial.builder().from(trial).anyMolecularCriteria(applicableMolecularCriteria).build()
        } else {
            null
        }
    }

    private fun create(
        clinicalEvidenceFactory: ClinicalEvidenceFactory,
        evidences: List<EfficacyEvidence>,
        trials: List<ActionableTrial>
    ): ClinicalEvidenceMatcher {
        val variantEvidence = VariantEvidence.create(evidences, trials)
        val copyNumberEvidence = CopyNumberEvidence.create(evidences, trials)
        val disruptionEvidence = DisruptionEvidence.create(evidences, trials)
        val homozygousDisruptionEvidence = HomozygousDisruptionEvidence.create(evidences, trials)
        val fusionEvidence = FusionEvidence.create(evidences, trials)
        val virusEvidence = VirusEvidence.create(evidences, trials)
        val signatureEvidence = SignatureEvidence.create(evidences, trials)

        return ClinicalEvidenceMatcher(
            clinicalEvidenceFactory = clinicalEvidenceFactory,
            variantEvidence = variantEvidence,
            copyNumberEvidence = copyNumberEvidence,
            disruptionEvidence = disruptionEvidence,
            homozygousDisruptionEvidence = homozygousDisruptionEvidence,
            fusionEvidence = fusionEvidence,
            virusEvidence = virusEvidence,
            signatureEvidence = signatureEvidence
        )
    }
}
