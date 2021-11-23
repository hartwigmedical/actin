package com.hartwig.actin.algo.evaluation.bloodtransfusion;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public final class BloodTransfusionRuleMapping {

    private BloodTransfusionRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_HAD_ERYTHROCYTE_TRANSFUSION_WITHIN_LAST_X_WEEKS, hasHadRecentErythrocyteTransfusion());
        map.put(EligibilityRule.HAS_HAD_THROMBOCYTE_TRANSFUSION_WITHIN_LAST_X_WEEKS, hasHadRecentThrombocyteTransfusion());

        return map;
    }

    @NotNull
    private static FunctionCreator hasHadRecentErythrocyteTransfusion() {
        return function -> new HasHadRecentErythrocyteTransfusion();
    }

    @NotNull
    private static FunctionCreator hasHadRecentThrombocyteTransfusion() {
        return function -> new HasHadRecentThrombocyteTransfusion();
    }
}
