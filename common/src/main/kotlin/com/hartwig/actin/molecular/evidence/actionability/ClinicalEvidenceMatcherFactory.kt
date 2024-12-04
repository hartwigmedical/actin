package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.molecular.evidence.curation.ApplicabilityFiltering
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.trial.ActionableTrial

class ClinicalEvidenceMatcherFactory(
    private val doidModel: DoidModel,
    private val tumorDoids: Set<String>
) {

    val actionableEventSources = setOf(ActionabilityConstants.EVIDENCE_SOURCE, ActionabilityConstants.EXTERNAL_TRIAL_SOURCE)

    fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): ClinicalEvidenceMatcher {
        val filteredEvidences = evidences
            .filter { actionableEventSources.contains(it.source()) }
            .filter { isMolecularCriteriumApplicable(it.molecularCriterium()) }

        val filteredTrials = trials
            .filter { actionableEventSources.contains(it.source()) }
            .filter { isMolecularCriteriumApplicable(it.anyMolecularCriteria().iterator().next()) }

        val personalizedActionabilityFactory = PersonalizedActionabilityFactory.create(doidModel, tumorDoids)

        return create(personalizedActionabilityFactory, filteredEvidences, filteredTrials)
    }

    private fun isMolecularCriteriumApplicable(molecularCriterium: MolecularCriterium): Boolean {
        with(molecularCriterium) {
            return when {
                hotspots().isNotEmpty() -> ApplicabilityFiltering.isApplicable(hotspots().first())
                genes().isNotEmpty() -> ApplicabilityFiltering.isApplicable(genes().first())
                exons().isNotEmpty() -> ApplicabilityFiltering.isApplicable(exons().first())
                codons().isNotEmpty() -> ApplicabilityFiltering.isApplicable(codons().first())
                else -> true
            }
        }
    }

    private fun create(
        personalizedActionabilityFactory: PersonalizedActionabilityFactory,
        evidences: List<EfficacyEvidence>,
        trials: List<ActionableTrial>
    ): ClinicalEvidenceMatcher {
        val variantEvidence = VariantEvidence.create(evidences, trials)
        val copyNumberEvidence = CopyNumberEvidence.create(evidences, trials)
        val breakendEvidence = BreakendEvidence.create(evidences, trials)
        val homozygousDisruptionEvidence = HomozygousDisruptionEvidence.create(evidences, trials)
        val fusionEvidence = FusionEvidence.create(evidences, trials)
        val virusEvidence = VirusEvidence.create(evidences, trials)
        val signatureEvidence = SignatureEvidence.create(evidences, trials)

        return ClinicalEvidenceMatcher(
            personalizedActionabilityFactory = personalizedActionabilityFactory,
            variantEvidence = variantEvidence,
            copyNumberEvidence = copyNumberEvidence,
            breakendEvidence = breakendEvidence,
            homozygousDisruptionEvidence = homozygousDisruptionEvidence,
            fusionEvidence = fusionEvidence,
            virusEvidence = virusEvidence,
            signatureEvidence = signatureEvidence
        )
    }
}
