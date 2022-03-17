package com.hartwig.actin.algo.evaluation.bloodtransfusion;

import java.time.LocalDate;
import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.calendar.ReferenceDateProvider;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.FunctionInputResolver;

import org.jetbrains.annotations.NotNull;

public final class BloodTransfusionRuleMapping {

    private BloodTransfusionRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create(@NotNull ReferenceDateProvider referenceDateProvider) {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_HAD_ERYTHROCYTE_TRANSFUSION_WITHIN_LAST_X_WEEKS,
                hasHadRecentBloodTransfusion(TransfusionProduct.ERYTHROCYTE, referenceDateProvider));
        map.put(EligibilityRule.HAS_HAD_THROMBOCYTE_TRANSFUSION_WITHIN_LAST_X_WEEKS,
                hasHadRecentBloodTransfusion(TransfusionProduct.THROMBOCYTE, referenceDateProvider));

        return map;
    }

    @NotNull
    private static FunctionCreator hasHadRecentBloodTransfusion(@NotNull TransfusionProduct product,
            @NotNull ReferenceDateProvider referenceDateProvider) {
        return function -> {
            int maxAgeWeeks = FunctionInputResolver.createOneIntegerInput(function);
            LocalDate minDate = referenceDateProvider.date().minusWeeks(maxAgeWeeks);

            return new HasHadRecentBloodTransfusion(product, minDate);
        };
    }
}
