package com.hartwig.actin.algo.evaluation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.composite.And;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.EligibilityParameterResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class EvaluationFunctionFactory {

    private static final Logger LOGGER = LogManager.getLogger(EvaluationFunctionFactory.class);

    static final Map<EligibilityRule, FunctionCreator> FUNCTION_CREATOR_MAP = Maps.newHashMap();

    static {
        FUNCTION_CREATOR_MAP.put(EligibilityRule.AND, andCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.OR, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.NOT, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.WARN_ON_FAIL, cannotBeDeterminedCreator());

        FUNCTION_CREATOR_MAP.put(EligibilityRule.IS_AT_LEAST_18_YEARS_OLD, isAtLeast18YearsOldCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.IS_BREASTFEEDING, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.IS_PREGNANT, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_WEEKS, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_WHO_STATUS_OF_AT_MOST_X, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_ALLERGY_RELATED_TO_STUDY_MEDICATION, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_HAD_SURGERY_WITHIN_LAST_X_WEEKS, cannotBeDeterminedCreator());

        FUNCTION_CREATOR_MAP.put(EligibilityRule.PRIMARY_TUMOR_LOCATION_BELONGS_TO_DOID_X, cannotBeDeterminedCreator());

        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_ADVANCED_CANCER, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_METASTATIC_CANCER, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_LIVER_METASTASES, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_ACTIVE_CNS_METASTASES, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_MEASURABLE_DISEASE_RECIST, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_BIOPSY_AMENABLE_LESION, cannotBeDeterminedCreator());

        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_DECLINED_SOC_TREATMENTS, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_HAD_MAX_X_NR_ANTI_PD_L1_OR_PD_1_IMMUNOTHERAPIES, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.SECOND_MALIGNANCY_HAS_BEEN_CURED_SINCE_X_YEARS, cannotBeDeterminedCreator());

        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_LEUKOCYTES_ABS_OF_AT_LEAST_X, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_HEMOGLOBIN_G_PER_DL_OF_AT_LEAST_X, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_THROMBOCYTES_ABS_AT_LEAST_X, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_CREATININE_ULN_OF_AT_MOST_X, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_CREATININE_CLEARANCE_CKD_EPI_OF_AT_LEAST_X, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_TOTAL_BILIRUBIN_ULN_AT_MOST_X, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_DIRECT_BILIRUBIN_ULN_AT_MOST_X, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_ASAT_ULN_OF_AT_MOST_X, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_INR_ULN_AT_MOST_X, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_PT_ULN_AT_MOST_X, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_APTT_ULN_AT_MOST_X, cannotBeDeterminedCreator());

        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_ACTIVE_INFECTION, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_SIGNIFICANT_CONCOMITANT_ILLNESS, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_HISTORY_OF_AUTOIMMUNE_DISEASE, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_HISTORY_OF_CARDIAC_DISEASE, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_KNOWN_HEPATITIS_B_INFECTION, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_KNOWN_HEPATITIS_C_INFECTION, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_KNOWN_HIV_INFECTION, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_GILBERT_DISEASE, cannotBeDeterminedCreator());

        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_STABLE_ANTICOAGULANT_DOSING, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_TOXICITY_GRADE_OF_AT_LEAST_X, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_TOXICITY_GRADE_OF_AT_LEAST_X_IN_NEUROPATHY, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.CURRENTLY_GETS_IMMUNOSUPPRESSANT_MEDICATION, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.CURRENTLY_GETS_CORTICOSTEROID_MEDICATION, cannotBeDeterminedCreator());

        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_SBP_MMHG_OF_AT_LEAST_X, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_DBP_MMHG_OF_AT_LEAST_X, cannotBeDeterminedCreator());

        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_HAD_ERYTHROCYTE_TRANSFUSION_WITHIN_LAST_X_WEEKS, cannotBeDeterminedCreator());
        FUNCTION_CREATOR_MAP.put(EligibilityRule.HAS_HAD_THROMBOCYTE_TRANSFUSION_WITHIN_LAST_X_WEEKS, cannotBeDeterminedCreator());
    }

    private EvaluationFunctionFactory() {
    }

    @NotNull
    public static EvaluationFunction create(@NotNull EligibilityFunction function) {
        if (!EligibilityParameterResolver.hasValidParameters(function)) {
            LOGGER.warn("Function with rule '{}' has invalid inputs {}. Evaluation for this rule will always fail",
                    function.rule(),
                    function.parameters());
            return cannotBeDeterminedCreator().create(function);
        }

        FunctionCreator creator = FUNCTION_CREATOR_MAP.get(function.rule());
        if (creator == null) {
            LOGGER.warn("No creator function defined for '{}'. Evaluation for this rule will always fail", function.rule());
            return cannotBeDeterminedCreator().create(function);
        }

        return creator.create(function);
    }

    @NotNull
    private static FunctionCreator andCreator() {
        return new FunctionCreator() {
            @NotNull
            @Override
            public EvaluationFunction create(@NotNull final EligibilityFunction function) {
                List<EvaluationFunction> functions = Lists.newArrayList();
                for (EligibilityFunction input : EligibilityParameterResolver.createCompositeParameters(function)) {
                    functions.add(create(input));
                }
                return new And(functions);
            }
        };
    }

    @NotNull
    private static FunctionCreator isAtLeast18YearsOldCreator() {
        return function -> new IsAtLeastEighteenYearsOld(LocalDate.now().getYear());
    }

    @NotNull
    private static FunctionCreator cannotBeDeterminedCreator() {
        return function -> canNeverBeDetermined();
    }

    @NotNull
    private static EvaluationFunction canNeverBeDetermined() {
        return evaluation -> Evaluation.COULD_NOT_BE_DETERMINED;
    }

    private interface FunctionCreator {

        @NotNull
        EvaluationFunction create(@NotNull EligibilityFunction function);
    }
}
