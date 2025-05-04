package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.molecular.evidence.curation.ApplicabilityFiltering
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.trial.ActionableTrial
import com.hartwig.serve.datamodel.trial.ImmutableActionableTrial


// TODO didn't need an interface for this before, can we do without it?
interface ClinicalEvidenceMatcherFactory {
    fun create(molecularTest: MolecularTest): ClinicalEvidenceMatcher
}

// TODO better name than default? standard? will there be more than one?
class DefaultClinicalEvidenceMatcherFactory(
    private val doidModel: DoidModel,
    private val tumorDoids: Set<String>,
    private val evidences: List<EfficacyEvidence>,
    private val trials: List<ActionableTrial>,
) : ClinicalEvidenceMatcherFactory {

    override fun create(molecularTest: MolecularTest): ClinicalEvidenceMatcher {
        val filteredEvidences = evidences
            .filter { it.source() == ActionabilityConstants.EVIDENCE_SOURCE }
            .filter { isMolecularCriteriumApplicable(it.molecularCriterium()) }

        val curatedTrials = trials
            .filter { it.source() == ActionabilityConstants.EXTERNAL_TRIAL_SOURCE }
            .mapNotNull { trial -> removeNonApplicableMolecularCriteria(trial) }

        val personalizedActionabilityFactory = PersonalizedActionabilityFactory.create(doidModel, tumorDoids)
        val actionableToEvidences = actionableToEvidences(filteredEvidences, molecularTest)

        return create(personalizedActionabilityFactory, filteredEvidences, curatedTrials, actionableToEvidences)
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

    private fun actionableToEvidences(evidences: List<EfficacyEvidence>, molecularTest: MolecularTest): ActionableToEvidences {
        val combinedEvidenceMatcher = CombinedEvidenceMatcher(evidences)  // TODO build a factory, pay some tariffs
        return combinedEvidenceMatcher.match(molecularTest)
    }

    private fun create(
        personalizedActionabilityFactory: PersonalizedActionabilityFactory,
        evidences: List<EfficacyEvidence>,
        trials: List<ActionableTrial>,
        actionableToEvidences: ActionableToEvidences
    ): ClinicalEvidenceMatcher {
        val variantEvidence = VariantEvidence.create(actionableToEvidences, trials)
        val copyNumberEvidence = CopyNumberEvidence.create(actionableToEvidences, trials)
        val disruptionEvidence = DisruptionEvidence.create(actionableToEvidences, trials)
        val homozygousDisruptionEvidence = HomozygousDisruptionEvidence.create(actionableToEvidences, trials)
        val fusionEvidence = FusionEvidence.create(actionableToEvidences, trials)
        val virusEvidence = VirusEvidence.create(actionableToEvidences, trials)

        // TODO: signature evidence is not matched on an event type, how do we handle a signature in combination with molecular events
        val signatureEvidence = SignatureEvidence.create(evidences, trials)

        return ClinicalEvidenceMatcher(
            personalizedActionabilityFactory = personalizedActionabilityFactory,
            variantEvidence = variantEvidence,
            copyNumberEvidence = copyNumberEvidence,
            disruptionEvidence = disruptionEvidence,
            homozygousDisruptionEvidence = homozygousDisruptionEvidence,
            fusionEvidence = fusionEvidence,
            virusEvidence = virusEvidence,
            signatureEvidence = signatureEvidence,
        )
    }
}
