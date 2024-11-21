package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.molecular.evidence.curation.ApplicabilityFiltering.isApplicable
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.molecular.MolecularCriterium

class ActionableEventMatcherFactory(
    private val doidModel: DoidModel,
    private val tumorDoids: Set<String>
) {

    val actionableEventSources = setOf(ActionabilityConstants.EVIDENCE_SOURCE, ActionabilityConstants.EXTERNAL_TRIAL_SOURCE)

    fun create(actionableEvents: ActionableEvents): ActionableEventMatcher {
        val filtered = filterForApplicability(
            filterForSources(
                actionableEvents,
                actionableEventSources
            )
        )
        val personalizedActionabilityFactory: PersonalizedActionabilityFactory =
            PersonalizedActionabilityFactory.create(doidModel, tumorDoids)
        return fromActionableEvents(
            personalizedActionabilityFactory,
            filtered
        )
    }

    internal fun filterForSources(actionableEvents: ActionableEvents, sourcesToInclude: Set<Knowledgebase?>): ActionableEvents {
        val filteredEvidences = actionableEvents.evidences.filter { sourcesToInclude.contains(it.source()) }
        val filteredTrials = actionableEvents.trials.filter { sourcesToInclude.contains(it.source()) }
        return ActionableEvents(filteredEvidences, filteredTrials)
    }

    internal fun filterForApplicability(actionableEvents: ActionableEvents): ActionableEvents {
        val evidences = actionableEvents.evidences.filter { isMolecularCriteriumApplicable(it.molecularCriterium()) }
        val trials = actionableEvents.trials.filter { isMolecularCriteriumApplicable(it.anyMolecularCriteria().iterator().next()) }
        return ActionableEvents(evidences, trials)
    }

    private fun isMolecularCriteriumApplicable(molecularCriterium: MolecularCriterium): Boolean {
        with(molecularCriterium) {
            return when {
                hotspots().isNotEmpty() -> isApplicable(hotspots().first())
                genes().isNotEmpty() -> isApplicable(genes().first())
                exons().isNotEmpty() -> isApplicable(exons().first())
                codons().isNotEmpty() -> isApplicable(codons().first())
                else -> true
            }
        }
    }

    private fun fromActionableEvents(
        personalizedActionabilityFactory: PersonalizedActionabilityFactory,
        actionableEvents: ActionableEvents
    ): ActionableEventMatcher {
        val signatureEvidence: SignatureEvidence = SignatureEvidence.create(actionableEvents)
        val variantEvidence: VariantEvidence = VariantEvidence.create(actionableEvents)
        val copyNumberEvidence: CopyNumberEvidence = CopyNumberEvidence.create(actionableEvents)
        val homozygousDisruptionEvidence: HomozygousDisruptionEvidence = HomozygousDisruptionEvidence.create(actionableEvents)
        val breakendEvidence: BreakendEvidence = BreakendEvidence.create(actionableEvents)
        val fusionEvidence: FusionEvidence = FusionEvidence.create(actionableEvents)
        val virusEvidence: VirusEvidence = VirusEvidence.create(actionableEvents)
        return ActionableEventMatcher(
            personalizedActionabilityFactory,
            signatureEvidence,
            variantEvidence,
            copyNumberEvidence,
            homozygousDisruptionEvidence,
            breakendEvidence,
            fusionEvidence,
            virusEvidence
        )
    }
}
