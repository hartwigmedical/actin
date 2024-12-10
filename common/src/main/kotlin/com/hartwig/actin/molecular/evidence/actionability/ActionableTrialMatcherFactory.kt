package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial
import java.util.function.Predicate

object ActionableTrialMatcherFactory {

    fun createHotspotTrialMatcher(trials: List<ActionableTrial>): ActionableTrialMatcher {
        return createTrialMatcher(trials, ActionableEventsExtraction.hotspotFilter())
    }

    fun createCodonTrialMatcher(trials: List<ActionableTrial>): ActionableTrialMatcher {
        return createTrialMatcher(trials, ActionableEventsExtraction.codonFilter())
    }

    fun createExonTrialMatcher(trials: List<ActionableTrial>): ActionableTrialMatcher {
        return createTrialMatcher(trials, ActionableEventsExtraction.codonFilter())
    }

    fun createGeneTrialMatcher(trials: List<ActionableTrial>, validGeneEvents: Set<GeneEvent>): ActionableTrialMatcher {
        return createTrialMatcher(trials, ActionableEventsExtraction.geneFilter(validGeneEvents))
    }

    fun createFusionTrialMatcher(trials: List<ActionableTrial>): ActionableTrialMatcher {
        return createTrialMatcher(trials, ActionableEventsExtraction.fusionFilter())
    }

    fun createCharacteristicsTrialMatcher(
        trials: List<ActionableTrial>,
        validCharacteristicTypes: Set<TumorCharacteristicType>
    ): ActionableTrialMatcher {
        return createTrialMatcher(trials, ActionableEventsExtraction.characteristicsFilter(validCharacteristicTypes))
    }

    private fun createTrialMatcher(trials: List<ActionableTrial>, predicate: Predicate<MolecularCriterium>): ActionableTrialMatcher {
        return ActionableTrialMatcher(ActionableEventsExtraction.extractTrials(trials, predicate), predicate)
    }
}