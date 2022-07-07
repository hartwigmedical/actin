package com.hartwig.actin.algo.evaluation.washout;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.RuleMapper;
import com.hartwig.actin.algo.evaluation.RuleMappingResources;
import com.hartwig.actin.algo.medication.MedicationStatusInterpreter;
import com.hartwig.actin.algo.medication.MedicationStatusInterpreterOnEvaluationDate;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.single.OneIntegerManyStrings;
import com.hartwig.actin.treatment.input.single.TwoIntegers;
import com.hartwig.actin.treatment.input.single.TwoIntegersManyStrings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WashoutRuleMapper extends RuleMapper {

    private static final Logger LOGGER = LogManager.getLogger(WashoutRuleMapper.class);

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

    public WashoutRuleMapper(@NotNull final RuleMappingResources resources) {
        super(resources);
    }

    @NotNull
    @Override
    public Map<EligibilityRule, FunctionCreator> createMappings() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_RECEIVED_DRUGS_X_CANCER_THERAPY_WITHIN_Y_WEEKS, hasRecentlyReceivedCancerTherapyOfNamesCreator());
        map.put(EligibilityRule.HAS_RECEIVED_DRUGS_X_CANCER_THERAPY_WITHIN_Y_WEEKS_Z_HALF_LIVES,
                hasRecentlyReceivedCancerTherapyOfNamesHalfLifeCreator());
        map.put(EligibilityRule.HAS_RECEIVED_CATEGORIES_X_CANCER_THERAPY_WITHIN_Y_WEEKS,
                hasRecentlyReceivedCancerTherapyOfCategoriesCreator());
        map.put(EligibilityRule.HAS_RECEIVED_CATEGORIES_X_CANCER_THERAPY_WITHIN_Y_WEEKS_Z_HALF_LIVES,
                hasRecentlyReceivedCancerTherapyOfCategoriesHalfLifeCreator());
        map.put(EligibilityRule.HAS_RECEIVED_RADIOTHERAPY_WITHIN_X_WEEKS, hasRecentlyReceivedRadiotherapyCreator());
        map.put(EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_WITHIN_X_WEEKS, hasRecentlyReceivedAnyCancerTherapyCreator());
        map.put(EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_EXCL_CATEGORIES_X_WITHIN_Y_WEEKS,
                hasRecentlyReceivedAnyCancerTherapyButSomeCreator());
        map.put(EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_WITHIN_X_WEEKS_Y_HALF_LIVES,
                hasRecentlyReceivedAnyCancerTherapyWithHalfLifeCreator());
        map.put(EligibilityRule.HAS_RECEIVED_ANY_ANTI_CANCER_THERAPY_EXCL_CATEGORIES_X_WITHIN_Y_WEEKS_Z_HALF_LIVES,
                hasRecentlyReceivedAnyCancerTherapyButSomeWithHalfLifeCreator());
        map.put(EligibilityRule.WILL_REQUIRE_ANY_ANTICANCER_THERAPY_DURING_TRIAL, willRequireAnticancerTherapyCreator());
        map.put(EligibilityRule.HAS_RECEIVED_HERBAL_MEDICATION_OR_DIETARY_SUPPLEMENTS_WITHIN_X_WEEKS,
                hasRecentlyReceivedHerbalMedicationOrSupplementsCreator());

        return map;
    }

    @NotNull
    private FunctionCreator hasRecentlyReceivedCancerTherapyOfNamesCreator() {
        return function -> {
            OneIntegerManyStrings input = functionInputResolver().createManyStringsOneIntegerInput(function);
            return createReceivedCancerTherapyOfNameFunction(input.strings(), input.integer());
        };
    }

    @NotNull
    private FunctionCreator hasRecentlyReceivedCancerTherapyOfNamesHalfLifeCreator() {
        return function -> {
            TwoIntegersManyStrings input = functionInputResolver().createManyStringsTwoIntegersInput(function);
            return createReceivedCancerTherapyOfNameFunction(input.strings(), input.integer1());
        };
    }

    @NotNull
    private EvaluationFunction createReceivedCancerTherapyOfNameFunction(@NotNull List<String> names, int minWeeks) {
        MedicationStatusInterpreter interpreter = createInterpreterForWashout(minWeeks);

        return new HasRecentlyReceivedCancerTherapyOfName(Sets.newHashSet(names), interpreter);
    }

    @NotNull
    private FunctionCreator hasRecentlyReceivedCancerTherapyOfCategoriesCreator() {
        return function -> {
            OneIntegerManyStrings input = functionInputResolver().createManyStringsOneIntegerInput(function);
            return createReceivedCancerTherapyOfCategoryFunction(input.strings(), input.integer());
        };
    }

    @NotNull
    private FunctionCreator hasRecentlyReceivedCancerTherapyOfCategoriesHalfLifeCreator() {
        return function -> {
            TwoIntegersManyStrings input = functionInputResolver().createManyStringsTwoIntegersInput(function);
            return createReceivedCancerTherapyOfCategoryFunction(input.strings(), input.integer1());
        };
    }

    @NotNull
    private EvaluationFunction createReceivedCancerTherapyOfCategoryFunction(@NotNull List<String> categoryInputs, int minWeeks) {
        MedicationStatusInterpreter interpreter = createInterpreterForWashout(minWeeks);

        Set<String> names = determineNames(categoryInputs);
        if (names != null) {
            return new HasRecentlyReceivedCancerTherapyOfName(names, interpreter);
        } else {
            Set<String> categories = determineCategories(categoryInputs);
            return new HasRecentlyReceivedCancerTherapyOfCategory(categories, interpreter);
        }
    }

    @NotNull
    private FunctionCreator hasRecentlyReceivedRadiotherapyCreator() {
        return function -> new HasRecentlyReceivedRadiotherapy(referenceDateProvider().year(), referenceDateProvider().month());
    }

    @NotNull
    private FunctionCreator hasRecentlyReceivedAnyCancerTherapyCreator() {
        return function -> {
            int minWeeks = functionInputResolver().createOneIntegerInput(function);
            return createReceivedAnyCancerTherapyFunction(minWeeks);
        };
    }

    @NotNull
    private FunctionCreator hasRecentlyReceivedAnyCancerTherapyWithHalfLifeCreator() {
        return function -> {
            TwoIntegers input = functionInputResolver().createTwoIntegersInput(function);
            return createReceivedAnyCancerTherapyFunction(input.integer1());
        };
    }

    @NotNull
    private EvaluationFunction createReceivedAnyCancerTherapyFunction(int minWeeks) {
        MedicationStatusInterpreter interpreter = createInterpreterForWashout(minWeeks);

        return new HasRecentlyReceivedCancerTherapyOfCategory(ALL_ANTI_CANCER_CATEGORIES, interpreter);
    }

    @NotNull
    private FunctionCreator hasRecentlyReceivedAnyCancerTherapyButSomeCreator() {
        return function -> {
            OneIntegerManyStrings input = functionInputResolver().createManyStringsOneIntegerInput(function);
            return createReceivedAnyCancerTherapyButSomeFunction(input.strings(), input.integer());
        };
    }

    @NotNull
    private FunctionCreator hasRecentlyReceivedAnyCancerTherapyButSomeWithHalfLifeCreator() {
        return function -> {
            TwoIntegersManyStrings input = functionInputResolver().createManyStringsTwoIntegersInput(function);
            return createReceivedAnyCancerTherapyButSomeFunction(input.strings(), input.integer1());
        };
    }

    @NotNull
    private EvaluationFunction createReceivedAnyCancerTherapyButSomeFunction(@NotNull List<String> categoriesToIgnore, int minWeeks) {
        MedicationStatusInterpreter interpreter = createInterpreterForWashout(minWeeks);

        Set<String> categoriesToConsider = Sets.newHashSet();
        categoriesToConsider.addAll(ALL_ANTI_CANCER_CATEGORIES);
        categoriesToConsider.removeAll(determineCategories(categoriesToIgnore));

        return new HasRecentlyReceivedCancerTherapyOfCategory(categoriesToConsider, interpreter);
    }

    @NotNull
    private FunctionCreator willRequireAnticancerTherapyCreator() {
        return function -> new WillRequireAnticancerTherapy();
    }

    @NotNull
    private FunctionCreator hasRecentlyReceivedHerbalMedicationOrSupplementsCreator() {
        return function -> {
            MedicationStatusInterpreter interpreter = createInterpreterForWashout(functionInputResolver().createOneIntegerInput(function));

            Set<String> categories = Sets.newHashSet("Supplement", "Herbal remedy");
            return new HasRecentlyReceivedCancerTherapyOfCategory(categories, interpreter);
        };
    }

    @NotNull
    private MedicationStatusInterpreter createInterpreterForWashout(int inputWeeks) {
        LocalDate minDate = referenceDateProvider().date().minusWeeks(inputWeeks).plusWeeks(2);
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
