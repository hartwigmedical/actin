package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.FusionDriverType
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.evidence.matching.FusionMatching
import com.hartwig.actin.molecular.evidence.matching.MatchingCriteriaFunctions
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.fusion.ActionableFusion
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial
import java.util.function.Predicate

class FusionEvidence(
    private val actionableToEvidences: ActionableToEvidences,
//    private val fusionEvidences: List<EfficacyEvidence>,
    private val fusionTrialMatcher: ActionableTrialMatcher,
//    private val promiscuousEvidences: List<EfficacyEvidence>,
    private val promiscuousTrialMatcher: ActionableTrialMatcher
) : ActionabilityMatcher<Fusion> {

    override fun findMatches(event: Fusion): ActionabilityMatch {
        val criteria = MatchingCriteriaFunctions.createFusionCriteria(event)
        val fusionMatchPredicate: Predicate<MolecularCriterium> =
            Predicate { isFusionMatch(ActionableEventExtraction.extractFusion(it), criteria) }

//        val matchedFusionEvidence = fusionEvidences.filter { fusionMatchPredicate.test(it.molecularCriterium()) }
        // TODO check we don't need an additional predicate for fusion evidences here?
        val matchedFusionEvidence = (actionableToEvidences[event] ?: emptySet())
            .filter { fusionMatchPredicate.test(it.molecularCriterium()) }
            .toList()
        val matchedFusionTrials = fusionTrialMatcher.apply(fusionMatchPredicate)

        val promiscuousMatchPredicate: Predicate<MolecularCriterium> =
            Predicate { isPromiscuousMatch(ActionableEventExtraction.extractGene(it), criteria) }

//        val matchedPromiscuousEvidence = promiscuousEvidences.filter { promiscuousMatchPredicate.test(it.molecularCriterium()) }
        val matchedPromiscuousEvidence = (actionableToEvidences[event] ?: emptySet())
            .filter { promiscuousMatchPredicate.test(it.molecularCriterium()) }
            .filter { evidence ->
                ActionableEventExtraction.geneFilter(PROMISCUOUS_FUSION_EVENTS).test(evidence.molecularCriterium())
            }
            .toList()
        val matchedPromiscuousTrials = promiscuousTrialMatcher.apply(promiscuousMatchPredicate)

        return ActionabilityMatchFactory.create(
            evidenceMatchLists = listOf(matchedFusionEvidence, matchedPromiscuousEvidence),
            matchingCriteriaPerTrialMatchLists = listOf(matchedFusionTrials + matchedPromiscuousTrials)
        )
    }

    private fun isFusionMatch(actionable: ActionableFusion, fusion: FusionMatchCriteria): Boolean {
        return fusion.isReportable && FusionMatching.isGeneMatch(actionable, fusion) && FusionMatching.isExonMatch(actionable, fusion)
    }

    private fun isPromiscuousMatch(actionable: ActionableGene, fusion: FusionMatchCriteria): Boolean {
        if (!fusion.isReportable) {
            return false
        }

        return when (fusion.driverType) {
            FusionDriverType.PROMISCUOUS_3 -> {
                actionable.gene() == fusion.geneEnd
            }

            FusionDriverType.PROMISCUOUS_5 -> {
                actionable.gene() == fusion.geneStart
            }

            else -> {
                actionable.gene() == fusion.geneStart || actionable.gene() == fusion.geneEnd
            }
        }
    }

    companion object {
        private val PROMISCUOUS_FUSION_EVENTS = setOf(GeneEvent.FUSION, GeneEvent.ACTIVATION, GeneEvent.ANY_MUTATION)

        fun create(actionableToEvidences: ActionableToEvidences, trials: List<ActionableTrial>): FusionEvidence {
//            val fusionEvidences = EfficacyEvidenceExtractor.extractFusionEvidence(evidences)
            val fusionTrialMatcher = ActionableTrialMatcherFactory.createFusionTrialMatcher(trials)

//            val promiscuousEvidences = EfficacyEvidenceExtractor.extractGeneEvidence(evidences, PROMISCUOUS_FUSION_EVENTS)
            val promiscuousTrialMatcher = ActionableTrialMatcherFactory.createGeneTrialMatcher(trials, PROMISCUOUS_FUSION_EVENTS)

            return FusionEvidence(
                actionableToEvidences,
//                fusionEvidences,
                fusionTrialMatcher,
//                promiscuousEvidences,
                promiscuousTrialMatcher,
            )
        }
    }
}
