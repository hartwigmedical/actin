package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial
import java.util.function.Predicate

object ActionableTrialMatcherFactory {

    fun createHotspotTrialMatcher(trials: List<ActionableTrial>): ActionableTrialMatcher {
        return createTrialMatcher(trials, ActionableEventExtraction.hotspotFilter())
    }

    fun createCodonTrialMatcher(trials: List<ActionableTrial>): ActionableTrialMatcher {
        return createTrialMatcher(trials, ActionableEventExtraction.codonFilter())
    }

    fun createExonTrialMatcher(trials: List<ActionableTrial>): ActionableTrialMatcher {
        return createTrialMatcher(trials, ActionableEventExtraction.exonFilter())
    }

    fun createGeneTrialMatcher(trials: List<ActionableTrial>, validGeneEvents: Set<GeneEvent>): ActionableTrialMatcher {
        return createTrialMatcher(trials, ActionableEventExtraction.geneFilter(validGeneEvents))
    }

    fun createFusionTrialMatcher(trials: List<ActionableTrial>): ActionableTrialMatcher {
        return createTrialMatcher(trials, ActionableEventExtraction.fusionFilter())
    }

    fun createCharacteristicsTrialMatcher(
        trials: List<ActionableTrial>,
        validCharacteristicTypes: Set<TumorCharacteristicType>
    ): ActionableTrialMatcher {
        return createTrialMatcher(trials, ActionableEventExtraction.characteristicsFilter(validCharacteristicTypes))
    }

    private fun createTrialMatcher(trials: List<ActionableTrial>, predicate: Predicate<MolecularCriterium>): ActionableTrialMatcher {
        return ActionableTrialMatcher(extractTrials(trials, predicate), predicate)
    }

    private fun extractTrials(trials: List<ActionableTrial>, predicate: Predicate<MolecularCriterium>): List<ActionableTrial> {
        return trials.filter { trial ->
            trial.anyMolecularCriteria().any { criterium -> predicate.test(criterium) }
        }
    }
}