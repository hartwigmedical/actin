package com.hartwig.actin.treatment.interpretation;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EligibilityParameterResolver {

    private static final Logger LOGGER = LogManager.getLogger(EligibilityParameterResolver.class);

    public static final Set<EligibilityRule> COMPOSITE_RULES = Sets.newHashSet();

    static final Set<EligibilityRule> RULES_WITH_SINGLE_DOUBLE_PARAMETER = Sets.newHashSet();
    static final Set<EligibilityRule> RULES_WITH_SINGLE_INTEGER_PARAMETER = Sets.newHashSet();
    static final Set<EligibilityRule> RULES_WITH_SINGLE_STRING_PARAMETER = Sets.newHashSet();
    static final Set<EligibilityRule> RULES_WITHOUT_PARAMETERS = Sets.newHashSet();

    static {
        COMPOSITE_RULES.add(EligibilityRule.AND);
        COMPOSITE_RULES.add(EligibilityRule.OR);
        COMPOSITE_RULES.add(EligibilityRule.NOT);
        COMPOSITE_RULES.add(EligibilityRule.WARN_IF);

        RULES_WITH_SINGLE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_WEEKS);
        RULES_WITH_SINGLE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_LEUKOCYTES_ABS_OF_AT_LEAST_X);
        RULES_WITH_SINGLE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_HEMOGLOBIN_G_PER_DL_OF_AT_LEAST_X);
        RULES_WITH_SINGLE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_THROMBOCYTES_ABS_AT_LEAST_X);
        RULES_WITH_SINGLE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_CREATININE_ULN_OF_AT_MOST_X);
        RULES_WITH_SINGLE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_CREATININE_CLEARANCE_CKD_EPI_OF_AT_LEAST_X);
        RULES_WITH_SINGLE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_TOTAL_BILIRUBIN_ULN_AT_MOST_X);
        RULES_WITH_SINGLE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_DIRECT_BILIRUBIN_ULN_AT_MOST_X);
        RULES_WITH_SINGLE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_ASAT_ULN_OF_AT_MOST_X);
        RULES_WITH_SINGLE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_INR_ULN_AT_MOST_X);
        RULES_WITH_SINGLE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_PT_ULN_AT_MOST_X);
        RULES_WITH_SINGLE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_APTT_ULN_AT_MOST_X);
        RULES_WITH_SINGLE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_SBP_MMHG_OF_AT_LEAST_X);
        RULES_WITH_SINGLE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_DBP_MMHG_OF_AT_LEAST_X);

        RULES_WITH_SINGLE_INTEGER_PARAMETER.add(EligibilityRule.HAS_WHO_STATUS_OF_AT_MOST_X);
        RULES_WITH_SINGLE_INTEGER_PARAMETER.add(EligibilityRule.HAS_HAD_SURGERY_WITHIN_LAST_X_WEEKS);
        RULES_WITH_SINGLE_INTEGER_PARAMETER.add(EligibilityRule.HAS_HAD_MAX_X_NR_ANTI_PD_L1_OR_PD_1_IMMUNOTHERAPIES);
        RULES_WITH_SINGLE_INTEGER_PARAMETER.add(EligibilityRule.SECOND_MALIGNANCY_HAS_BEEN_CURED_SINCE_X_YEARS);
        RULES_WITH_SINGLE_INTEGER_PARAMETER.add(EligibilityRule.HAS_TOXICITY_GRADE_OF_AT_LEAST_X);
        RULES_WITH_SINGLE_INTEGER_PARAMETER.add(EligibilityRule.HAS_TOXICITY_GRADE_OF_AT_LEAST_X_IN_NEUROPATHY);
        RULES_WITH_SINGLE_INTEGER_PARAMETER.add(EligibilityRule.HAS_HAD_ERYTHROCYTE_TRANSFUSION_WITHIN_LAST_X_WEEKS);
        RULES_WITH_SINGLE_INTEGER_PARAMETER.add(EligibilityRule.HAS_HAD_THROMBOCYTE_TRANSFUSION_WITHIN_LAST_X_WEEKS);

        RULES_WITH_SINGLE_STRING_PARAMETER.add(EligibilityRule.PRIMARY_TUMOR_LOCATION_BELONGS_TO_DOID_X);

        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.IS_AT_LEAST_18_YEARS_OLD);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.IS_BREASTFEEDING);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.IS_PREGNANT);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_ALLERGY_RELATED_TO_STUDY_MEDICATION);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_ADVANCED_CANCER);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_METASTATIC_CANCER);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_LIVER_METASTASES);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_ACTIVE_CNS_METASTASES);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_MEASURABLE_DISEASE_RECIST);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_BIOPSY_AMENABLE_LESION);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_DECLINED_SOC_TREATMENTS);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_ACTIVE_INFECTION);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_SIGNIFICANT_CONCOMITANT_ILLNESS);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_HISTORY_OF_AUTOIMMUNE_DISEASE);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_HISTORY_OF_CARDIAC_DISEASE);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_KNOWN_HEPATITIS_B_INFECTION);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_KNOWN_HEPATITIS_C_INFECTION);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_KNOWN_HIV_INFECTION);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_GILBERT_DISEASE);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_STABLE_ANTICOAGULANT_DOSING);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.CURRENTLY_GETS_IMMUNOSUPPRESSANT_MEDICATION);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.CURRENTLY_GETS_CORTICOSTEROID_MEDICATION);
    }

    private EligibilityParameterResolver() {
    }

    @Nullable
    public static Boolean hasValidParameters(@NotNull EligibilityFunction function) {
        try {
            if (COMPOSITE_RULES.contains(function.rule())) {
                createCompositeParameters(function);
                return true;
            } else if (RULES_WITH_SINGLE_DOUBLE_PARAMETER.contains(function.rule())) {
                createSingleDoubleParameter(function);
                return true;
            } else if (RULES_WITH_SINGLE_INTEGER_PARAMETER.contains(function.rule())) {
                createSingleIntegerParameter(function);
                return true;
            } else if (RULES_WITH_SINGLE_STRING_PARAMETER.contains(function.rule())) {
                createSingleStringParameter(function);
                return true;
            } else if (RULES_WITHOUT_PARAMETERS.contains(function.rule())) {
                return function.parameters().isEmpty();
            } else {
                LOGGER.warn("Could not determine validity of parameters for function with rule '{}'", function.rule());
                return null;
            }
        } catch (Exception exception) {
            return false;
        }
    }

    public static double createSingleDoubleParameter(@NotNull EligibilityFunction function) {
        assertExpectedParamCount(function, 1);

        return Double.parseDouble((String) function.parameters().get(0));
    }

    public static int createSingleIntegerParameter(@NotNull EligibilityFunction function) {
        assertExpectedParamCount(function, 1);

        return Integer.parseInt((String) function.parameters().get(0));
    }

    @NotNull
    public static String createSingleStringParameter(@NotNull EligibilityFunction function) {
        assertExpectedParamCount(function, 1);

        return (String) function.parameters().get(0);
    }

    @NotNull
    public static List<EligibilityFunction> createCompositeParameters(@NotNull EligibilityFunction function) {
        if (function.rule() == EligibilityRule.WARN_IF) {
            assertExpectedParamCount(function, 1);
        } else if (function.rule() == EligibilityRule.NOT && function.parameters().isEmpty()) {
            throw new IllegalArgumentException("No parameters passed into NOT function");
        } else if ((function.rule() == EligibilityRule.OR || function.rule() == EligibilityRule.AND) && function.parameters().size() < 2) {
            throw new IllegalArgumentException(
                    "Not enough parameters passed into " + function.rule() + " function: " + function.parameters().size());
        }

        List<EligibilityFunction> functions = Lists.newArrayList();
        for (Object input : function.parameters()) {
            functions.add((EligibilityFunction) input);
        }
        return functions;
    }

    private static void assertExpectedParamCount(@NotNull EligibilityFunction function, int expectedCount) {
        if (function.parameters().size() != expectedCount) {
            throw new IllegalArgumentException(
                    "Invalid number of inputs passed to function " + function.rule() + ": " + function.parameters().size());
        }
    }
}
