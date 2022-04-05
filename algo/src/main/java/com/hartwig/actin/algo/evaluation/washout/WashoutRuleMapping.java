package com.hartwig.actin.algo.evaluation.washout;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.calendar.ReferenceDateProvider;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.FunctionInputResolver;
import com.hartwig.actin.treatment.input.single.OneIntegerOneString;
import com.hartwig.actin.treatment.input.single.OneStringTwoIntegers;
import com.hartwig.actin.treatment.input.single.TwoIntegers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class WashoutRuleMapping {

    private static final Set<String> ALL_ANTI_CANCER_CATEGORIES = Sets.newHashSet();

    private static final Map<String, Set<String>> MEDICATIONS_FOR_MAIN_CATEGORY = Maps.newHashMap();
    private static final Map<String, Set<String>> CATEGORIES_PER_MAIN_CATEGORY = Maps.newHashMap();

    static {
        MEDICATIONS_FOR_MAIN_CATEGORY.put("Immunotherapy", Sets.newHashSet("Pembrolizumab", "Nivolumab", "Ipilimumab", "Cemiplimab"));
        MEDICATIONS_FOR_MAIN_CATEGORY.put("PARP inhibitors", Sets.newHashSet("Olaparib", "Rucaparib"));

        CATEGORIES_PER_MAIN_CATEGORY.put("Chemotherapy",
                Sets.newHashSet("Platinum compound", "Pyrimidine antagonist", "Taxane", "Alkylating agent"));
        CATEGORIES_PER_MAIN_CATEGORY.put("Endocrine therapy", Sets.newHashSet("Anti-androgen", "Anti-estrogen"));
        CATEGORIES_PER_MAIN_CATEGORY.put("Gonadorelin", Sets.newHashSet("Gonadorelin agonist", "Gonadorelin antagonist"));

        for (Set<String> categories : CATEGORIES_PER_MAIN_CATEGORY.values()) {
            ALL_ANTI_CANCER_CATEGORIES.addAll(categories);
        }

        ALL_ANTI_CANCER_CATEGORIES.add("Cytotoxic antibiotics");
        ALL_ANTI_CANCER_CATEGORIES.add("Monoclonal antibody for malignancies");
        ALL_ANTI_CANCER_CATEGORIES.add("Protein kinase inhibitor");
        ALL_ANTI_CANCER_CATEGORIES.add("Oncolytics, other");
    }

    private WashoutRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create(@NotNull ReferenceDateProvider referenceDateProvider) {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_RECEIVED_DRUG_X_CANCER_THERAPY_WITHIN_Y_WEEKS,
                hasRecentlyReceivedCancerTherapyOfNameCreator(referenceDateProvider));
        map.put(EligibilityRule.HAS_RECEIVED_CATEGORY_X_CANCER_THERAPY_WITHIN_Y_WEEKS,
                hasRecentlyReceivedCancerTherapyOfCategoryCreator(referenceDateProvider));
        map.put(EligibilityRule.HAS_RECEIVED_RADIOTHERAPY_WITHIN_X_WEEKS, hasRecentlyReceivedRadiotherapyCreator(referenceDateProvider));
        map.put(EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_WITHIN_X_WEEKS,
                hasRecentlyReceivedAnyCancerTherapyCreator(referenceDateProvider));
        map.put(EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_EXCL_CATEGORIES_X_WITHIN_Y_WEEKS,
                hasRecentlyReceivedAnyCancerTherapyButSomeCreator(referenceDateProvider));
        map.put(EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_WITHIN_X_WEEKS_Y_HALF_LIVES,
                hasRecentlyReceivedAnyCancerTherapyWithHalfLifeCreator(referenceDateProvider));
        map.put(EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_EXCL_CATEGORIES_X_WITHIN_Y_WEEKS_Z_HALF_LIVES,
                hasRecentlyReceivedAnyCancerTherapyButSomeWithHalfLifeCreator(referenceDateProvider));
        map.put(EligibilityRule.WILL_REQUIRE_ANY_ANTICANCER_THERAPY_DURING_TRIAL, willRequireAnticancerTherapyCreator());
        map.put(EligibilityRule.HAS_RECEIVED_HERBAL_MEDICATION_OR_DIETARY_SUPPLEMENTS_WITHIN_X_WEEKS,
                hasRecentlyReceivedHerbalMedicationOrSupplementsCreator(referenceDateProvider));

        return map;
    }

    @NotNull
    private static FunctionCreator hasRecentlyReceivedCancerTherapyOfNameCreator(@NotNull ReferenceDateProvider referenceDateProvider) {
        return function -> {
            OneIntegerOneString input = FunctionInputResolver.createOneStringOneIntegerInput(function);
            LocalDate minDate = determineMinDateForWashout(referenceDateProvider, input.integer());

            return new HasRecentlyReceivedCancerTherapyOfName(Sets.newHashSet(input.string()), minDate);
        };
    }

    @NotNull
    private static FunctionCreator hasRecentlyReceivedCancerTherapyOfCategoryCreator(@NotNull ReferenceDateProvider referenceDateProvider) {
        return function -> {
            OneIntegerOneString input = FunctionInputResolver.createOneStringOneIntegerInput(function);
            LocalDate minDate = determineMinDateForWashout(referenceDateProvider, input.integer());

            Set<String> names = determineNames(input.string());
            if (names != null) {
                return new HasRecentlyReceivedCancerTherapyOfName(names, minDate);
            } else {
                Set<String> categories = determineCategories(input.string());
                return new HasRecentlyReceivedCancerTherapyOfCategory(categories, minDate);
            }
        };
    }

    @NotNull
    private static FunctionCreator hasRecentlyReceivedRadiotherapyCreator(@NotNull ReferenceDateProvider referenceDateProvider) {
        return function -> new HasRecentlyReceivedRadiotherapy(referenceDateProvider.year(), referenceDateProvider.month());
    }

    @NotNull
    private static FunctionCreator hasRecentlyReceivedAnyCancerTherapyCreator(@NotNull ReferenceDateProvider referenceDateProvider) {
        return function -> {
            LocalDate minDate = determineMinDateForWashout(referenceDateProvider, FunctionInputResolver.createOneIntegerInput(function));

            return new HasRecentlyReceivedCancerTherapyOfCategory(ALL_ANTI_CANCER_CATEGORIES, minDate);
        };
    }

    @NotNull
    private static FunctionCreator hasRecentlyReceivedAnyCancerTherapyButSomeCreator(@NotNull ReferenceDateProvider referenceDateProvider) {
        return function -> {
            OneIntegerOneString input = FunctionInputResolver.createOneStringOneIntegerInput(function);
            LocalDate minDate = determineMinDateForWashout(referenceDateProvider, input.integer());

            Set<String> categoriesToConsider = Sets.newHashSet();
            categoriesToConsider.addAll(ALL_ANTI_CANCER_CATEGORIES);

            categoriesToConsider.removeAll(determineCategories(input.string()));
            return new HasRecentlyReceivedCancerTherapyOfCategory(categoriesToConsider, minDate);
        };
    }

    @NotNull
    private static FunctionCreator hasRecentlyReceivedAnyCancerTherapyWithHalfLifeCreator(
            @NotNull ReferenceDateProvider referenceDateProvider) {
        return function -> {
            TwoIntegers input = FunctionInputResolver.createTwoIntegerInput(function);
            LocalDate minDate = determineMinDateForWashout(referenceDateProvider, input.integer1());

            return new HasRecentlyReceivedCancerTherapyOfCategory(ALL_ANTI_CANCER_CATEGORIES, minDate);
        };
    }

    @NotNull
    private static FunctionCreator hasRecentlyReceivedAnyCancerTherapyButSomeWithHalfLifeCreator(
            @NotNull ReferenceDateProvider referenceDateProvider) {
        return function -> {
            OneStringTwoIntegers input = FunctionInputResolver.createOneStringTwoIntegerInput(function);
            LocalDate minDate = determineMinDateForWashout(referenceDateProvider, input.integer1());

            Set<String> categoriesToConsider = Sets.newHashSet();
            categoriesToConsider.addAll(ALL_ANTI_CANCER_CATEGORIES);

            categoriesToConsider.removeAll(determineCategories(input.string()));
            return new HasRecentlyReceivedCancerTherapyOfCategory(categoriesToConsider, minDate);
        };
    }

    @NotNull
    private static FunctionCreator willRequireAnticancerTherapyCreator() {
        return function -> new WillRequireAnticancerTherapy();
    }

    @NotNull
    private static FunctionCreator hasRecentlyReceivedHerbalMedicationOrSupplementsCreator(
            @NotNull ReferenceDateProvider referenceDateProvider) {
        return function -> {
            LocalDate minDate = determineMinDateForWashout(referenceDateProvider, FunctionInputResolver.createOneIntegerInput(function));

            Set<String> categories = Sets.newHashSet("Supplement", "Herbal remedy");
            return new HasRecentlyReceivedCancerTherapyOfCategory(categories, minDate);
        };
    }

    @NotNull
    private static LocalDate determineMinDateForWashout(@NotNull ReferenceDateProvider referenceDateProvider, int inputWeeks) {
        return referenceDateProvider.date().minusWeeks(inputWeeks).plusWeeks(2);
    }

    @NotNull
    private static Set<String> determineCategories(@NotNull String input) {
        return CATEGORIES_PER_MAIN_CATEGORY.getOrDefault(input, Sets.newHashSet(input));
    }

    @Nullable
    private static Set<String> determineNames(@NotNull String input) {
        return MEDICATIONS_FOR_MAIN_CATEGORY.get(input);
    }
}
