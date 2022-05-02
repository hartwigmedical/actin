package com.hartwig.actin.algo.evaluation.washout;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.calendar.ReferenceDateProvider;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.medication.MedicationStatusInterpreter;
import com.hartwig.actin.algo.medication.MedicationStatusInterpreterOnEvaluationDate;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.FunctionInputResolver;
import com.hartwig.actin.treatment.input.single.OneIntegerManyStrings;
import com.hartwig.actin.treatment.input.single.TwoIntegers;
import com.hartwig.actin.treatment.input.single.TwoIntegersManyStrings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class WashoutRuleMapping {

    private static final Logger LOGGER = LogManager.getLogger(WashoutRuleMapping.class);

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
        CATEGORIES_PER_MAIN_CATEGORY.put("Immunosuppressants",
                Sets.newHashSet("Immunosuppressants, selective", "Immunosuppressants, other"));

        ALL_ANTI_CANCER_CATEGORIES.addAll(CATEGORIES_PER_MAIN_CATEGORY.get("Chemotherapy"));
        ALL_ANTI_CANCER_CATEGORIES.addAll(CATEGORIES_PER_MAIN_CATEGORY.get("Gonadorelin"));
        ALL_ANTI_CANCER_CATEGORIES.addAll(CATEGORIES_PER_MAIN_CATEGORY.get("Endocrine therapy"));
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

        map.put(EligibilityRule.HAS_RECEIVED_DRUGS_X_CANCER_THERAPY_WITHIN_Y_WEEKS,
                hasRecentlyReceivedCancerTherapyOfNamesCreator(referenceDateProvider));
        map.put(EligibilityRule.HAS_RECEIVED_DRUGS_X_CANCER_THERAPY_WITHIN_Y_WEEKS_Z_HALF_LIVES,
                hasRecentlyReceivedCancerTherapyOfNamesHalfLifeCreator(referenceDateProvider));
        map.put(EligibilityRule.HAS_RECEIVED_CATEGORIES_X_CANCER_THERAPY_WITHIN_Y_WEEKS,
                hasRecentlyReceivedCancerTherapyOfCategoriesCreator(referenceDateProvider));
        map.put(EligibilityRule.HAS_RECEIVED_CATEGORIES_X_CANCER_THERAPY_WITHIN_Y_WEEKS_Z_HALF_LIVES,
                hasRecentlyReceivedCancerTherapyOfCategoriesHalfLifeCreator(referenceDateProvider));
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
    private static FunctionCreator hasRecentlyReceivedCancerTherapyOfNamesCreator(@NotNull ReferenceDateProvider referenceDateProvider) {
        return function -> {
            OneIntegerManyStrings input = FunctionInputResolver.createManyStringsOneIntegerInput(function);
            MedicationStatusInterpreter interpreter = createInterpreterForWashout(referenceDateProvider, input.integer());

            return new HasRecentlyReceivedCancerTherapyOfName(Sets.newHashSet(input.strings()), interpreter);
        };
    }

    @NotNull
    private static FunctionCreator hasRecentlyReceivedCancerTherapyOfNamesHalfLifeCreator(
            @NotNull ReferenceDateProvider referenceDateProvider) {
        return function -> {
            TwoIntegersManyStrings input = FunctionInputResolver.createManyStringsTwoIntegersInput(function);
            MedicationStatusInterpreter interpreter = createInterpreterForWashout(referenceDateProvider, input.integer1());

            return new HasRecentlyReceivedCancerTherapyOfName(Sets.newHashSet(input.strings()), interpreter);
        };
    }

    @NotNull
    private static FunctionCreator hasRecentlyReceivedCancerTherapyOfCategoriesCreator(
            @NotNull ReferenceDateProvider referenceDateProvider) {
        return function -> {
            OneIntegerManyStrings input = FunctionInputResolver.createManyStringsOneIntegerInput(function);
            MedicationStatusInterpreter interpreter = createInterpreterForWashout(referenceDateProvider, input.integer());

            Set<String> names = determineNames(input.strings());
            if (names != null) {
                return new HasRecentlyReceivedCancerTherapyOfName(names, interpreter);
            } else {
                Set<String> categories = determineCategories(input.strings());
                return new HasRecentlyReceivedCancerTherapyOfCategory(categories, interpreter);
            }
        };
    }

    @NotNull
    private static FunctionCreator hasRecentlyReceivedCancerTherapyOfCategoriesHalfLifeCreator(
            @NotNull ReferenceDateProvider referenceDateProvider) {
        return function -> {
            TwoIntegersManyStrings input = FunctionInputResolver.createManyStringsTwoIntegersInput(function);
            MedicationStatusInterpreter interpreter = createInterpreterForWashout(referenceDateProvider, input.integer1());

            Set<String> names = determineNames(input.strings());
            if (names != null) {
                return new HasRecentlyReceivedCancerTherapyOfName(names, interpreter);
            } else {
                Set<String> categories = determineCategories(input.strings());
                return new HasRecentlyReceivedCancerTherapyOfCategory(categories, interpreter);
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
            int minWeeks = FunctionInputResolver.createOneIntegerInput(function);
            return createReceivedAnyCancerTherapyFunction(referenceDateProvider, minWeeks);
        };
    }

    @NotNull
    private static FunctionCreator hasRecentlyReceivedAnyCancerTherapyWithHalfLifeCreator(
            @NotNull ReferenceDateProvider referenceDateProvider) {
        return function -> {
            TwoIntegers input = FunctionInputResolver.createTwoIntegersInput(function);
            return createReceivedAnyCancerTherapyFunction(referenceDateProvider, input.integer1());
        };
    }

    @NotNull
    private static EvaluationFunction createReceivedAnyCancerTherapyFunction(@NotNull ReferenceDateProvider referenceDateProvider,
            int minWeeks) {
        MedicationStatusInterpreter interpreter = createInterpreterForWashout(referenceDateProvider, minWeeks);

        return new HasRecentlyReceivedCancerTherapyOfCategory(ALL_ANTI_CANCER_CATEGORIES, interpreter);
    }

    @NotNull
    private static FunctionCreator hasRecentlyReceivedAnyCancerTherapyButSomeCreator(@NotNull ReferenceDateProvider referenceDateProvider) {
        return function -> {
            OneIntegerManyStrings input = FunctionInputResolver.createManyStringsOneIntegerInput(function);
            MedicationStatusInterpreter interpreter = createInterpreterForWashout(referenceDateProvider, input.integer());

            Set<String> categoriesToConsider = Sets.newHashSet();
            categoriesToConsider.addAll(ALL_ANTI_CANCER_CATEGORIES);
            categoriesToConsider.removeAll(determineCategories(input.strings()));

            return new HasRecentlyReceivedCancerTherapyOfCategory(categoriesToConsider, interpreter);
        };
    }

    @NotNull
    private static FunctionCreator hasRecentlyReceivedAnyCancerTherapyButSomeWithHalfLifeCreator(
            @NotNull ReferenceDateProvider referenceDateProvider) {
        return function -> {
            TwoIntegersManyStrings input = FunctionInputResolver.createManyStringsTwoIntegersInput(function);
            MedicationStatusInterpreter interpreter = createInterpreterForWashout(referenceDateProvider, input.integer1());

            Set<String> categoriesToConsider = Sets.newHashSet();
            categoriesToConsider.addAll(ALL_ANTI_CANCER_CATEGORIES);
            categoriesToConsider.removeAll(determineCategories(input.strings()));

            return new HasRecentlyReceivedCancerTherapyOfCategory(categoriesToConsider, interpreter);
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
            MedicationStatusInterpreter interpreter =
                    createInterpreterForWashout(referenceDateProvider, FunctionInputResolver.createOneIntegerInput(function));

            Set<String> categories = Sets.newHashSet("Supplement", "Herbal remedy");
            return new HasRecentlyReceivedCancerTherapyOfCategory(categories, interpreter);
        };
    }

    @NotNull
    private static MedicationStatusInterpreter createInterpreterForWashout(@NotNull ReferenceDateProvider referenceDateProvider,
            int inputWeeks) {
        LocalDate minDate = referenceDateProvider.date().minusWeeks(inputWeeks).plusWeeks(2);
        return new MedicationStatusInterpreterOnEvaluationDate(minDate);
    }

    @NotNull
    private static Set<String> determineCategories(@NotNull List<String> inputs) {
        Set<String> categories = Sets.newHashSet();
        for (String input : inputs) {
            categories.addAll(CATEGORIES_PER_MAIN_CATEGORY.getOrDefault(input, Sets.newHashSet(input)));
        }
        return categories;
    }

    @Nullable
    private static Set<String> determineNames(@NotNull List<String> inputs) {
        String first = inputs.get(0);

        Set<String> result = MEDICATIONS_FOR_MAIN_CATEGORY.get(first);
        if (result != null && inputs.size() > 1) {
            LOGGER.warn("Multiple inputs configured in washout while first input resolves to explicit set of medication names: {}", inputs);
        }
        return result;
    }
}
