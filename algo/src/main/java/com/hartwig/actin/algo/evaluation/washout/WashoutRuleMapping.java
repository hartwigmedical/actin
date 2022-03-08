package com.hartwig.actin.algo.evaluation.washout;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.util.EvaluationConstants;
import com.hartwig.actin.algo.evaluation.util.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluationFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.FunctionInputResolver;
import com.hartwig.actin.treatment.interpretation.single.OneIntegerOneString;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class WashoutRuleMapping {

    private static final Map<String, Set<String>> NAMES_MAP = Maps.newHashMap();
    private static final Map<String, Set<String>> CATEGORIES_MAP = Maps.newHashMap();

    static {
        NAMES_MAP.put("Immunotherapy", Sets.newHashSet("Pembrolizumab", "Nivolumab", "Ipilimumab", "Cemiplimab"));
        NAMES_MAP.put("PARP inhibitors", Sets.newHashSet("Olaparib", "Rucaparib"));

        CATEGORIES_MAP.put("Chemotherapy", Sets.newHashSet("Platinum compound", "Pyrimidine antagonist", "Taxane", "Alkylating agent"));
        CATEGORIES_MAP.put("Endocrine therapy", Sets.newHashSet("Anti-androgen", "Anti-estrogen"));
        CATEGORIES_MAP.put("Gonadorelin", Sets.newHashSet("Gonadorelin agonist", "Gonadorelin antagonist"));
    }

    private WashoutRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_RECEIVED_DRUG_X_CANCER_THERAPY_WITHIN_Y_WEEKS, hasRecentlyReceivedCancerTherapyOfNameCreator());
        map.put(EligibilityRule.HAS_RECEIVED_CATEGORY_X_CANCER_THERAPY_WITHIN_Y_WEEKS, hasRecentlyReceivedCancerTherapyOfCategoryCreator());
        map.put(EligibilityRule.HAS_RECEIVED_RADIOTHERAPY_WITHIN_X_WEEKS, hasRecentlyReceivedRadiotherapyCreator());
        map.put(EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_WITHIN_X_WEEKS,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_WITHIN_X_WEEKS_Y_HALF_LIVES,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_EXCL_CATEGORY_X_WITHIN_Y_WEEKS,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_EXCL_CATEGORY_X_WITHIN_Y_WEEKS_Z_HALF_LIVES,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.WILL_REQUIRE_ANY_ANTICANCER_THERAPY_DURING_TRIAL, willRequireAnticancerTherapyCreator());
        map.put(EligibilityRule.HAS_RECEIVED_HERBAL_MEDICATION_OR_DIETARY_SUPPLEMENTS_WITHIN_X_WEEKS,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));

        return map;
    }

    @NotNull
    private static FunctionCreator hasRecentlyReceivedCancerTherapyOfNameCreator() {
        return function -> {
            OneIntegerOneString input = FunctionInputResolver.createOneStringOneIntegerInput(function);
            LocalDate minDate = determineMinDateForWashout(input.integer());

            return new PassOrFailEvaluationFunction(new HasRecentlyReceivedCancerTherapyOfName(Sets.newHashSet(input.string()), minDate));
        };
    }

    @NotNull
    private static FunctionCreator hasRecentlyReceivedCancerTherapyOfCategoryCreator() {
        return function -> {
            OneIntegerOneString input = FunctionInputResolver.createOneStringOneIntegerInput(function);
            LocalDate minDate = determineMinDateForWashout(input.integer());

            Set<String> names = determineNames(input.string());
            if (names != null) {
                return new PassOrFailEvaluationFunction(new HasRecentlyReceivedCancerTherapyOfName(names, minDate));
            } else {
                Set<String> categories = determineCategories(input.string());
                return new PassOrFailEvaluationFunction(new HasRecentlyReceivedCancerTherapyOfCategory(categories, minDate));
            }
        };
    }

    @NotNull
    private static FunctionCreator hasRecentlyReceivedRadiotherapyCreator() {
        return function -> new PassOrFailEvaluationFunction(new HasRecentlyReceivedRadiotherapy(EvaluationConstants.REFERENCE_YEAR,
                EvaluationConstants.REFERENCE_MONTH));
    }

    @NotNull
    private static FunctionCreator willRequireAnticancerTherapyCreator() {
        return function -> new WillRequireAnticancerTherapy();
    }

    @NotNull
    private static LocalDate determineMinDateForWashout(int inputWeeks) {
        return EvaluationConstants.REFERENCE_DATE.minusWeeks(inputWeeks).plusWeeks(2);
    }

    @NotNull
    private static Set<String> determineCategories(@NotNull String input) {
        return CATEGORIES_MAP.getOrDefault(input, Sets.newHashSet(input));
    }

    @Nullable
    private static Set<String> determineNames(@NotNull String input) {
        return NAMES_MAP.get(input);
    }
}
