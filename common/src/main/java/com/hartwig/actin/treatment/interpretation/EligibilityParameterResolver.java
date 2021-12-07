package com.hartwig.actin.treatment.interpretation;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EligibilityParameterResolver {

    private static final Logger LOGGER = LogManager.getLogger(EligibilityParameterResolver.class);
    private static final String STRING_SEPARATOR = ";";

    static final Map<EligibilityRule, Inputs> PARAMETER_MAP = Maps.newHashMap();

    static {
        PARAMETER_MAP.put(EligibilityRule.IS_AT_LEAST_18_YEARS_OLD, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_WHO_STATUS_OF_AT_MOST_X, Inputs.ONE_INTEGER);
        PARAMETER_MAP.put(EligibilityRule.IS_ABLE_AND_WILLING_TO_GIVE_ADEQUATE_INFORMED_CONSENT, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.IS_INVOLVED_IN_STUDY_PROCEDURES, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_WEEKS, Inputs.ONE_INTEGER);
        PARAMETER_MAP.put(EligibilityRule.HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_MONTHS, Inputs.ONE_INTEGER);

        PARAMETER_MAP.put(EligibilityRule.PRIMARY_TUMOR_LOCATION_BELONGS_TO_DOID_X, Inputs.ONE_STRING);
        PARAMETER_MAP.put(EligibilityRule.HAS_ADVANCED_CANCER, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_METASTATIC_CANCER, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_LIVER_METASTASES, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_KNOWN_CNS_METASTASES, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_KNOWN_ACTIVE_CNS_METASTASES, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_KNOWN_SYMPTOMATIC_CNS_METASTASES, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_KNOWN_BRAIN_METASTASES, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_KNOWN_ACTIVE_BRAIN_METASTASES, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_KNOWN_SYMPTOMATIC_BRAIN_METASTASES, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_BONE_METASTASES, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_MEASURABLE_DISEASE_RECIST, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_BIOPSY_AMENABLE_LESION, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_COLLECTED_TUMOR_BIOPSY_WITHIN_X_MONTHS_BEFORE_IC, Inputs.ONE_INTEGER);

        PARAMETER_MAP.put(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_DECLINED_SOC_TREATMENTS, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY_BELONGING_TO_DOID_X, Inputs.ONE_STRING);
        PARAMETER_MAP.put(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY_BELONGING_TO_DOID_X_CURRENTLY_INACTIVE, Inputs.ONE_STRING);
        PARAMETER_MAP.put(EligibilityRule.EVERY_SECOND_MALIGNANCY_HAS_BEEN_CURED_SINCE_X_YEARS, Inputs.ONE_INTEGER);
        PARAMETER_MAP.put(EligibilityRule.HAS_HAD_AT_MOST_X_SYSTEMIC_TREATMENT_LINES, Inputs.ONE_INTEGER);
        PARAMETER_MAP.put(EligibilityRule.HAS_HAD_DRUG_NAME_X_TREATMENT, Inputs.ONE_STRING);
        PARAMETER_MAP.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT, Inputs.ONE_STRING);
        PARAMETER_MAP.put(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPE_Y, Inputs.TWO_STRINGS);
        PARAMETER_MAP.put(EligibilityRule.HAS_HAD_FLUOROPYRIMIDINE_TREATMENT, Inputs.TWO_STRINGS);
        PARAMETER_MAP.put(EligibilityRule.HAS_HAD_MAX_X_NR_ANTI_PD_L1_OR_PD_1_IMMUNOTHERAPIES, Inputs.ONE_INTEGER);
        PARAMETER_MAP.put(EligibilityRule.HAS_HAD_STEM_CELL_TRANSPLANTATION, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.IS_ELIGIBLE_FOR_ON_LABEL_DRUG_X, Inputs.ONE_STRING);

        PARAMETER_MAP.put(EligibilityRule.MOLECULAR_RESULTS_MUST_BE_AVAILABLE, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.ACTIVATION_OF_GENE_X, Inputs.ONE_STRING);
        PARAMETER_MAP.put(EligibilityRule.INACTIVATION_OF_GENE_X, Inputs.ONE_STRING);
        PARAMETER_MAP.put(EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X, Inputs.ONE_STRING);
        PARAMETER_MAP.put(EligibilityRule.MUTATION_IN_GENE_X_OF_TYPE_Y, Inputs.TWO_STRINGS);
        PARAMETER_MAP.put(EligibilityRule.INACTIVATING_MUTATION_IN_GENE_X, Inputs.ONE_STRING);
        PARAMETER_MAP.put(EligibilityRule.AMPLIFICATION_OF_GENE_X, Inputs.ONE_STRING);
        PARAMETER_MAP.put(EligibilityRule.DELETION_OF_GENE_X, Inputs.ONE_STRING);
        PARAMETER_MAP.put(EligibilityRule.ACTIVATING_FUSION_IN_GENE_X, Inputs.ONE_STRING);
        PARAMETER_MAP.put(EligibilityRule.SPECIFIC_FUSION_X, Inputs.ONE_STRING);
        PARAMETER_MAP.put(EligibilityRule.OVEREXPRESSION_OF_GENE_X, Inputs.ONE_STRING);
        PARAMETER_MAP.put(EligibilityRule.WILDTYPE_OF_GENE_X, Inputs.ONE_STRING);
        PARAMETER_MAP.put(EligibilityRule.MSI_SIGNATURE, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HRD_SIGNATURE, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.TMB_OF_AT_LEAST_X, Inputs.ONE_INTEGER);
        PARAMETER_MAP.put(EligibilityRule.TML_OF_AT_LEAST_X, Inputs.ONE_INTEGER);
        PARAMETER_MAP.put(EligibilityRule.TML_OF_AT_MOST_X, Inputs.ONE_INTEGER);

        PARAMETER_MAP.put(EligibilityRule.HAS_LEUKOCYTES_ABS_OF_AT_LEAST_X, Inputs.ONE_DOUBLE);
        PARAMETER_MAP.put(EligibilityRule.HAS_LEUKOCYTES_ABS_LLN_OF_AT_LEAST_X, Inputs.ONE_DOUBLE);
        PARAMETER_MAP.put(EligibilityRule.HAS_NEUTROPHILS_ABS_OF_AT_LEAST_X, Inputs.ONE_DOUBLE);
        PARAMETER_MAP.put(EligibilityRule.HAS_THROMBOCYTES_ABS_OF_AT_LEAST_X, Inputs.ONE_DOUBLE);
        PARAMETER_MAP.put(EligibilityRule.HAS_HEMOGLOBIN_G_PER_DL_OF_AT_LEAST_X, Inputs.ONE_DOUBLE);
        PARAMETER_MAP.put(EligibilityRule.HAS_HEMOGLOBIN_MMOL_PER_L_OF_AT_LEAST_X, Inputs.ONE_DOUBLE);
        PARAMETER_MAP.put(EligibilityRule.HAS_ALBUMIN_G_PER_DL_OF_AT_LEAST_X, Inputs.ONE_DOUBLE);
        PARAMETER_MAP.put(EligibilityRule.HAS_CREATININE_ULN_OF_AT_MOST_X, Inputs.ONE_DOUBLE);
        PARAMETER_MAP.put(EligibilityRule.HAS_EGFR_CKD_EPI_OF_AT_LEAST_X, Inputs.ONE_DOUBLE);
        PARAMETER_MAP.put(EligibilityRule.HAS_EGFR_MDRD_OF_AT_LEAST_X, Inputs.ONE_DOUBLE);
        PARAMETER_MAP.put(EligibilityRule.HAS_CREATININE_CLEARANCE_CG_OF_AT_LEAST_X, Inputs.ONE_DOUBLE);
        PARAMETER_MAP.put(EligibilityRule.HAS_TOTAL_BILIRUBIN_ULN_OF_AT_MOST_X, Inputs.ONE_DOUBLE);
        PARAMETER_MAP.put(EligibilityRule.HAS_DIRECT_BILIRUBIN_ULN_OF_AT_MOST_X, Inputs.ONE_DOUBLE);
        PARAMETER_MAP.put(EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X, Inputs.ONE_DOUBLE);
        PARAMETER_MAP.put(EligibilityRule.HAS_PT_ULN_OF_AT_MOST_X, Inputs.ONE_DOUBLE);
        PARAMETER_MAP.put(EligibilityRule.HAS_APTT_ULN_OF_AT_MOST_X, Inputs.ONE_DOUBLE);
        PARAMETER_MAP.put(EligibilityRule.HAS_ASAT_ULN_OF_AT_MOST_X, Inputs.ONE_DOUBLE);
        PARAMETER_MAP.put(EligibilityRule.HAS_ALAT_ULN_OF_AT_MOST_X, Inputs.ONE_DOUBLE);
        PARAMETER_MAP.put(EligibilityRule.HAS_ALP_ULN_OF_AT_MOST_X, Inputs.ONE_DOUBLE);
        PARAMETER_MAP.put(EligibilityRule.HAS_POTASSIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_MAGNESIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS, Inputs.NONE);

        PARAMETER_MAP.put(EligibilityRule.HAS_SIGNIFICANT_CONCOMITANT_ILLNESS, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_HISTORY_OF_AUTOIMMUNE_DISEASE, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_HISTORY_OF_CARDIAC_DISEASE, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_HISTORY_OF_CARDIOVASCULAR_DISEASE, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_HISTORY_OF_VASCULAR_DISEASE, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_HISTORY_OF_LUNG_DISEASE, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_HISTORY_OF_STROKE, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_HISTORY_OF_TIA, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_GILBERT_DISEASE, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_HAD_ORGAN_TRANSPLANT, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_CARDIAC_ARRHYTHMIA, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_HYPERTENSION, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_DIABETES, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_LVEF_OF_AT_LEAST_X, Inputs.ONE_DOUBLE);
        PARAMETER_MAP.put(EligibilityRule.HAS_LVEF_OF_AT_LEAST_X_IF_KNOWN, Inputs.ONE_DOUBLE);
        PARAMETER_MAP.put(EligibilityRule.HAS_KNOWN_MALABSORPTION_SYNDROME, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.IS_IN_DIALYSIS, Inputs.NONE);

        PARAMETER_MAP.put(EligibilityRule.HAS_ACTIVE_INFECTION, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_KNOWN_HEPATITIS_B_INFECTION, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_KNOWN_HEPATITIS_C_INFECTION, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_KNOWN_HIV_INFECTION, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.HAS_KNOWN_CYTOMEGALOVIRUS_INFECTION, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.ADHERENCE_TO_PROTOCOL_REGARDING_ATTENUATED_VACCINE_USE, Inputs.NONE);

        PARAMETER_MAP.put(EligibilityRule.HAS_ALLERGY_RELATED_TO_STUDY_MEDICATION, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.IS_ABLE_TO_SWALLOW_ORAL_MEDICATION, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.CURRENTLY_GETS_OTHER_ANTI_CANCER_THERAPY, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.CURRENTLY_GETS_ANTIBIOTICS_MEDICATION, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.CURRENTLY_GETS_ANTICOAGULANT_MEDICATION, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.CURRENTLY_GETS_CORTICOSTEROID_MEDICATION, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.CURRENTLY_GETS_COUMADIN_DERIVATIVE_MEDICATION, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.CURRENTLY_GETS_IMMUNOSUPPRESSANT_MEDICATION, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_ISOENZYME_X, Inputs.ONE_STRING);
        PARAMETER_MAP.put(EligibilityRule.HAS_STABLE_ANTICOAGULANT_DOSING, Inputs.NONE);

        PARAMETER_MAP.put(EligibilityRule.IS_BREASTFEEDING, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.IS_PREGNANT, Inputs.NONE);
        PARAMETER_MAP.put(EligibilityRule.IS_ABLE_AND_WILLING_TO_USE_ADEQUATE_ANTICONCEPTION_IF_REQUIRED, Inputs.NONE);

        PARAMETER_MAP.put(EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X, Inputs.ONE_INTEGER);
        PARAMETER_MAP.put(EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IN_Y, Inputs.ONE_INTEGER_ONE_STRING);
        PARAMETER_MAP.put(EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IGNORING_Y, Inputs.ONE_INTEGER_MANY_STRINGS);

        PARAMETER_MAP.put(EligibilityRule.HAS_SBP_MMHG_OF_AT_LEAST_X, Inputs.ONE_DOUBLE);
        PARAMETER_MAP.put(EligibilityRule.HAS_DBP_MMHG_OF_AT_LEAST_X, Inputs.ONE_DOUBLE);

        PARAMETER_MAP.put(EligibilityRule.HAS_HAD_ERYTHROCYTE_TRANSFUSION_WITHIN_LAST_X_WEEKS, Inputs.ONE_INTEGER);
        PARAMETER_MAP.put(EligibilityRule.HAS_HAD_THROMBOCYTE_TRANSFUSION_WITHIN_LAST_X_WEEKS, Inputs.ONE_INTEGER);

        PARAMETER_MAP.put(EligibilityRule.HAS_HAD_SURGERY_WITHIN_LAST_X_WEEKS, Inputs.ONE_INTEGER);

        PARAMETER_MAP.put(EligibilityRule.PATIENT_IS_TREATED_IN_HOSPITAL_X, Inputs.ONE_STRING);
    }

    private EligibilityParameterResolver() {
    }

    @Nullable
    public static Boolean hasValidParameters(@NotNull EligibilityFunction function) {
        try {
            if (CompositeRules.isComposite(function.rule())) {
                CompositeInput requiredInputs = CompositeRules.inputsForCompositeRule(function.rule());
                if (requiredInputs == CompositeInput.AT_LEAST_2) {
                    createAtLeastTwoCompositeParameters(function);
                } else if (requiredInputs == CompositeInput.MAXIMUM_1) {
                    createOneCompositeParameter(function);
                } else {
                    throw new IllegalStateException(
                            "Could not interpret composite inputs for rule '" + function.rule() + "': " + requiredInputs);
                }
                return true;
            }

            switch (PARAMETER_MAP.get(function.rule())) {
                case NONE: {
                    return function.parameters().isEmpty();
                }
                case ONE_INTEGER: {
                    createOneIntegerInput(function);
                    return true;
                }
                case ONE_DOUBLE: {
                    createOneDoubleInput(function);
                    return true;
                }
                case ONE_STRING: {
                    createOneStringInput(function);
                    return true;
                }
                case TWO_STRINGS: {
                    createTwoStringInput(function);
                    return true;
                }
                case ONE_INTEGER_ONE_STRING: {
                    createOneIntegerOneStringInput(function);
                    return true;
                }
                case ONE_INTEGER_MANY_STRINGS: {
                    createOneIntegerManyStringsInput(function);
                    return true;
                }
                default: {
                    LOGGER.warn("Rule '{}' not defined in parameter type map!", function.rule());
                    return null;
                }
            }

        } catch (Exception exception) {
            return false;
        }
    }

    public static int createOneIntegerInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, Inputs.ONE_INTEGER, 1);

        return Integer.parseInt((String) function.parameters().get(0));
    }

    public static double createOneDoubleInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, Inputs.ONE_DOUBLE, 1);

        return Double.parseDouble((String) function.parameters().get(0));
    }

    @NotNull
    public static String createOneStringInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, Inputs.ONE_STRING, 1);

        return (String) function.parameters().get(0);
    }

    @NotNull
    public static TwoStringInput createTwoStringInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, Inputs.TWO_STRINGS, 2);

        return ImmutableTwoStringInput.builder()
                .string1((String) function.parameters().get(0))
                .string2((String) function.parameters().get(1))
                .build();
    }

    @NotNull
    public static OneIntegerOneStringInput createOneIntegerOneStringInput(@NotNull EligibilityFunction function) {
        assertParamConfig(function, Inputs.ONE_INTEGER_ONE_STRING, 2);

        return ImmutableOneIntegerOneStringInput.builder()
                .integer(Integer.parseInt((String) function.parameters().get(0)))
                .string((String) function.parameters().get(1))
                .build();
    }

    @NotNull
    public static OneIntegerManyStringsInput createOneIntegerManyStringsInput(@NotNull EligibilityFunction function) {
        assertParamType(function, Inputs.ONE_INTEGER_MANY_STRINGS);

        List<String> strings = Lists.newArrayList();
        for (String input : ((String) function.parameters().get(1)).split(STRING_SEPARATOR)) {
            strings.add(input.trim());
        }
        return ImmutableOneIntegerManyStringsInput.builder()
                .integer(Integer.parseInt((String) function.parameters().get(0)))
                .strings(strings)
                .build();
    }

    @NotNull
    public static EligibilityFunction createOneCompositeParameter(@NotNull EligibilityFunction function) {
        assertParamCount(function, 1);

        return (EligibilityFunction) function.parameters().get(0);
    }

    @NotNull
    public static List<EligibilityFunction> createAtLeastTwoCompositeParameters(@NotNull EligibilityFunction function) {
        if (function.parameters().size() < 2) {
            throw new IllegalArgumentException(
                    "Not enough parameters passed into '" + function.rule() + "': " + function.parameters().size());
        }

        List<EligibilityFunction> functions = Lists.newArrayList();
        for (Object input : function.parameters()) {
            functions.add((EligibilityFunction) input);
        }
        return functions;
    }

    private static void assertParamConfig(@NotNull EligibilityFunction function, @NotNull Inputs requestedInputs, int expectedCount) {
        assertParamType(function, requestedInputs);
        assertParamCount(function, expectedCount);
    }

    private static void assertParamType(@NotNull EligibilityFunction function, @NotNull Inputs requestedInputs) {
        if (requestedInputs != PARAMETER_MAP.get(function.rule())) {
            throw new IllegalStateException("Incorrect type of inputs requested for '" + function.rule() + "': " + requestedInputs);
        }
    }

    private static void assertParamCount(@NotNull EligibilityFunction function, int expectedCount) {
        if (function.parameters().size() != expectedCount) {
            throw new IllegalArgumentException(
                    "Invalid number of inputs passed to '" + function.rule() + "': " + function.parameters().size());
        }
    }

    enum Inputs {
        NONE,
        ONE_INTEGER,
        ONE_DOUBLE,
        ONE_STRING,
        TWO_STRINGS,
        ONE_INTEGER_ONE_STRING,
        ONE_INTEGER_MANY_STRINGS
    }
}
