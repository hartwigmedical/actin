package com.hartwig.actin.algo.evaluation.washout;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.util.EvaluationConstants;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluationFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.FunctionInputResolver;
import com.hartwig.actin.treatment.interpretation.single.OneIntegerOneString;
import com.hartwig.actin.treatment.interpretation.single.OneStringTwoIntegers;
import com.hartwig.actin.treatment.interpretation.single.TwoIntegers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class WashoutRuleMapping {

    private static final Set<String> ALL_ANTI_CANCER_CATEGORIES = Sets.newHashSet();

    private static final Map<String, Set<String>> MEDICATIONS_FOR_MAIN_CATEGORY_MAP = Maps.newHashMap();
    private static final Map<String, Set<String>> CATEGORIES_PER_MAIN_CATEGORY = Maps.newHashMap();

    static {
        MEDICATIONS_FOR_MAIN_CATEGORY_MAP.put("Immunotherapy", Sets.newHashSet("Pembrolizumab", "Nivolumab", "Ipilimumab", "Cemiplimab"));
        MEDICATIONS_FOR_MAIN_CATEGORY_MAP.put("PARP inhibitors", Sets.newHashSet("Olaparib", "Rucaparib"));

        CATEGORIES_PER_MAIN_CATEGORY.put("Chemotherapy",
                Sets.newHashSet("Platinum compound", "Pyrimidine antagonist", "Taxane", "Alkylating agent"));
        CATEGORIES_PER_MAIN_CATEGORY.put("Endocrine therapy", Sets.newHashSet("Anti-androgen", "Anti-estrogen"));
        CATEGORIES_PER_MAIN_CATEGORY.put("Gonadorelin", Sets.newHashSet("Gonadorelin agonist", "Gonadorelin antagonist"));

        for (Set<String> categories : CATEGORIES_PER_MAIN_CATEGORY.values()) {
            ALL_ANTI_CANCER_CATEGORIES.addAll(categories);
        }

        ALL_ANTI_CANCER_CATEGORIES.add("Cytotoxic antibiotics");
        ALL_ANTI_CANCER_CATEGORIES.add("Monoclonal antibody");
        ALL_ANTI_CANCER_CATEGORIES.add("Protein kinase inhibitor");
        ALL_ANTI_CANCER_CATEGORIES.add("Oncolytics, other");
    }

    private WashoutRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_RECEIVED_DRUG_X_CANCER_THERAPY_WITHIN_Y_WEEKS, hasRecentlyReceivedCancerTherapyOfNameCreator());
        map.put(EligibilityRule.HAS_RECEIVED_CATEGORY_X_CANCER_THERAPY_WITHIN_Y_WEEKS, hasRecentlyReceivedCancerTherapyOfCategoryCreator());
        map.put(EligibilityRule.HAS_RECEIVED_RADIOTHERAPY_WITHIN_X_WEEKS, hasRecentlyReceivedRadiotherapyCreator());
        map.put(EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_WITHIN_X_WEEKS, hasRecentlyReceivedAnyCancerTherapyCreator());
        map.put(EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_EXCL_CATEGORY_X_WITHIN_Y_WEEKS,
                hasRecentlyReceivedAnyCancerTherapyButSomeCreator());
        map.put(EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_WITHIN_X_WEEKS_Y_HALF_LIVES,
                hasRecentlyReceivedAnyCancerTherapyWithHalfLifeCreator());
        map.put(EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_EXCL_CATEGORY_X_WITHIN_Y_WEEKS_Z_HALF_LIVES,
                hasRecentlyReceivedAnyCancerTherapyButSomeWithHalfLifeCreator());
        map.put(EligibilityRule.WILL_REQUIRE_ANY_ANTICANCER_THERAPY_DURING_TRIAL, willRequireAnticancerTherapyCreator());
        map.put(EligibilityRule.HAS_RECEIVED_HERBAL_MEDICATION_OR_DIETARY_SUPPLEMENTS_WITHIN_X_WEEKS,
                hasRecentlyReceivedHerbalMedicationOrSupplementsCreator());

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
    private static FunctionCreator hasRecentlyReceivedAnyCancerTherapyCreator() {
        return function -> {
            LocalDate minDate = determineMinDateForWashout(FunctionInputResolver.createOneIntegerInput(function));

            return new PassOrFailEvaluationFunction(new HasRecentlyReceivedCancerTherapyOfCategory(ALL_ANTI_CANCER_CATEGORIES, minDate));
        };
    }

    @NotNull
    private static FunctionCreator hasRecentlyReceivedAnyCancerTherapyButSomeCreator() {
        return function -> {
            OneIntegerOneString input = FunctionInputResolver.createOneStringOneIntegerInput(function);
            LocalDate minDate = determineMinDateForWashout(input.integer());

            Set<String> categoriesToConsider = Sets.newHashSet();
            categoriesToConsider.addAll(ALL_ANTI_CANCER_CATEGORIES);

            categoriesToConsider.removeAll(determineCategories(input.string()));
            return new PassOrFailEvaluationFunction(new HasRecentlyReceivedCancerTherapyOfCategory(categoriesToConsider, minDate));
        };
    }

    @NotNull
    private static FunctionCreator hasRecentlyReceivedAnyCancerTherapyWithHalfLifeCreator() {
        return function -> {
            TwoIntegers input = FunctionInputResolver.createTwoIntegerInput(function);
            LocalDate minDate = determineMinDateForWashout(input.integer1());

            return new PassOrFailEvaluationFunction(new HasRecentlyReceivedCancerTherapyOfCategory(ALL_ANTI_CANCER_CATEGORIES, minDate));
        };
    }

    @NotNull
    private static FunctionCreator hasRecentlyReceivedAnyCancerTherapyButSomeWithHalfLifeCreator() {
        return function -> {
            OneStringTwoIntegers input = FunctionInputResolver.createOneStringTwoIntegerInput(function);
            LocalDate minDate = determineMinDateForWashout(input.integer1());

            Set<String> categoriesToConsider = Sets.newHashSet();
            categoriesToConsider.addAll(ALL_ANTI_CANCER_CATEGORIES);

            categoriesToConsider.removeAll(determineCategories(input.string()));
            return new PassOrFailEvaluationFunction(new HasRecentlyReceivedCancerTherapyOfCategory(categoriesToConsider, minDate));
        };
    }

    @NotNull
    private static FunctionCreator willRequireAnticancerTherapyCreator() {
        return function -> new WillRequireAnticancerTherapy();
    }

    @NotNull
    private static FunctionCreator hasRecentlyReceivedHerbalMedicationOrSupplementsCreator() {
        return function -> {
            LocalDate minDate = determineMinDateForWashout(FunctionInputResolver.createOneIntegerInput(function));

            Set<String> categories = Sets.newHashSet("Supplement", "Herbal remedy");
            return new PassOrFailEvaluationFunction(new HasRecentlyReceivedCancerTherapyOfCategory(categories, minDate));
        };
    }

    @NotNull
    private static LocalDate determineMinDateForWashout(int inputWeeks) {
        return EvaluationConstants.REFERENCE_DATE.minusWeeks(inputWeeks).plusWeeks(2);
    }

    @NotNull
    private static Set<String> determineCategories(@NotNull String input) {
        return CATEGORIES_PER_MAIN_CATEGORY.getOrDefault(input, Sets.newHashSet(input));
    }

    @Nullable
    private static Set<String> determineNames(@NotNull String input) {
        return MEDICATIONS_FOR_MAIN_CATEGORY_MAP.get(input);
    }
}
