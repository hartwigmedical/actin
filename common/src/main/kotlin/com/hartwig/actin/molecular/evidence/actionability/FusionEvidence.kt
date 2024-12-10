package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.orange.driver.FusionDriverType
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.evidence.matching.FusionMatching
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.fusion.ActionableFusion
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial
import java.util.function.Predicate

class FusionEvidence(
    private val applicableFusionEvidences: List<EfficacyEvidence>,
    private val fusionTrialMatcher: ActionableTrialMatcher,
    private val applicablePromiscuousEvidences: List<EfficacyEvidence>,
    private val promiscuousTrialMatcher: ActionableTrialMatcher
) : ActionabilityMatcher<FusionMatchCriteria> {

    override fun findMatches(event: FusionMatchCriteria): ActionabilityMatch {
        val fusionMatchPredicate: Predicate<MolecularCriterium> =
            Predicate { isFusionMatch(ActionableEventsExtraction.extractFusion(it), event) }
        val promiscuousMatchPredicate: Predicate<MolecularCriterium> =
            Predicate { isPromiscuousMatch(ActionableEventsExtraction.extractGene(it), event) }

        val matchedFusionEvidence = applicableFusionEvidences.filter { fusionMatchPredicate.test(it.molecularCriterium()) }
        val matchedPromiscuousEvidence = applicablePromiscuousEvidences.filter { promiscuousMatchPredicate.test(it.molecularCriterium()) }

        val matchedFusionTrials = fusionTrialMatcher.matchTrials(fusionMatchPredicate)
        val matchedPromiscuousTrials = promiscuousTrialMatcher.matchTrials(promiscuousMatchPredicate)

        return ActionabilityMatch(matchedFusionEvidence + matchedPromiscuousEvidence, matchedFusionTrials + matchedPromiscuousTrials)
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

        fun create(evidences: List<EfficacyEvidence>, trials: List<ActionableTrial>): FusionEvidence {
            val applicableFusionEvidences = EfficacyEvidenceExtractor.extractFusionEvidence(evidences)
            val fusionTrialMatcher = ActionableTrialMatcherFactory.createFusionTrialMatcher(trials)

            val applicablePromiscuousEvidences = EfficacyEvidenceExtractor.extractGeneEvidence(evidences, PROMISCUOUS_FUSION_EVENTS)
            val promiscuousTrialMatcher = ActionableTrialMatcherFactory.createGeneTrialMatcher(trials, PROMISCUOUS_FUSION_EVENTS)

            return FusionEvidence(
                applicableFusionEvidences,
                fusionTrialMatcher,
                applicablePromiscuousEvidences,
                promiscuousTrialMatcher,
            )
        }
    }
}
