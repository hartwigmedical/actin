package com.hartwig.actin.algo.evaluation.bloodtransfusion;

import java.time.LocalDate;
import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.RuleMapper;
import com.hartwig.actin.algo.evaluation.RuleMappingResources;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public class BloodTransfusionRuleMapper extends RuleMapper {

    public BloodTransfusionRuleMapper(@NotNull final RuleMappingResources resources) {
        super(resources);
    }

    @NotNull
    @Override
    public Map<EligibilityRule, FunctionCreator> createMappings() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.REQUIRES_REGULAR_HEMATOPOIETIC_SUPPORT, requiresRegularHematopoieticSupportCreator());
        map.put(EligibilityRule.HAS_HAD_ERYTHROCYTE_TRANSFUSION_WITHIN_LAST_X_WEEKS,
                hasHadRecentBloodTransfusion(TransfusionProduct.ERYTHROCYTE));
        map.put(EligibilityRule.HAS_HAD_THROMBOCYTE_TRANSFUSION_WITHIN_LAST_X_WEEKS,
                hasHadRecentBloodTransfusion(TransfusionProduct.THROMBOCYTE));

        return map;
    }

    @NotNull
    private FunctionCreator requiresRegularHematopoieticSupportCreator() {
        return function -> new RequiresRegularHematopoieticSupport();
    }

    @NotNull
    private FunctionCreator hasHadRecentBloodTransfusion(@NotNull TransfusionProduct product) {
        return function -> {
            int maxAgeWeeks = functionInputResolver().createOneIntegerInput(function);
            LocalDate minDate = referenceDateProvider().date().minusWeeks(maxAgeWeeks);

            return new HasHadRecentBloodTransfusion(product, minDate);
        };
    }
}
