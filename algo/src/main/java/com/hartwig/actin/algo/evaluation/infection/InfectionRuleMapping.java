package com.hartwig.actin.algo.evaluation.infection;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public final class InfectionRuleMapping {

    private InfectionRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_ACTIVE_INFECTION, hasActiveInfectionCreator());
        map.put(EligibilityRule.HAS_KNOWN_HEPATITIS_B_INFECTION, hasKnownHepatitisBInfectionCreator());
        map.put(EligibilityRule.HAS_KNOWN_HEPATITIS_C_INFECTION, hasKnownHepatitisCInfectionCreator());
        map.put(EligibilityRule.HAS_KNOWN_HIV_INFECTION, hasKnownHIVInfectionCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator hasActiveInfectionCreator() {
        return function -> new HasActiveInfection();
    }

    @NotNull
    private static FunctionCreator hasKnownHepatitisBInfectionCreator() {
        return function -> new HasKnownHepatitisBInfection();
    }

    @NotNull
    private static FunctionCreator hasKnownHepatitisCInfectionCreator() {
        return function -> new HasKnownHepatitisCInfection();
    }

    @NotNull
    private static FunctionCreator hasKnownHIVInfectionCreator() {
        return function -> new HasKnownHIVInfection();
    }
}
