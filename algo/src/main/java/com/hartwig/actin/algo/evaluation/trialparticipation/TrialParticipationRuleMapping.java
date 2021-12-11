package com.hartwig.actin.algo.evaluation.trialparticipation;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public final class TrialParticipationRuleMapping {

    private TrialParticipationRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.IS_PARTICIPATING_IN_ANOTHER_TRIAL, participatesInAnotherTrialCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator participatesInAnotherTrialCreator() {
        return function -> new ParticipatesInAnotherTrial();
    }
}
