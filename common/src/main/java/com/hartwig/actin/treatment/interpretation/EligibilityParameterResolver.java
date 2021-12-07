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

    static final Set<EligibilityRule> RULES_WITH_ONE_DOUBLE_PARAMETER = Sets.newHashSet();
    static final Set<EligibilityRule> RULES_WITH_ONE_INTEGER_PARAMETER = Sets.newHashSet();
    static final Set<EligibilityRule> RULES_WITH_ONE_INTEGER_ONE_STRING_PARAMETER = Sets.newHashSet();
    static final Set<EligibilityRule> RULES_WITH_ONE_STRING_PARAMETER = Sets.newHashSet();
    static final Set<EligibilityRule> RULES_WITH_ONE_STRING_ONE_INTEGER_PARAMETER = Sets.newHashSet();
    static final Set<EligibilityRule> RULES_WITH_TWO_STRING_PARAMETERS = Sets.newHashSet();
    static final Set<EligibilityRule> RULES_WITHOUT_PARAMETERS = Sets.newHashSet();

    static {
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.IS_AT_LEAST_18_YEARS_OLD);
        RULES_WITH_ONE_INTEGER_PARAMETER.add(EligibilityRule.HAS_WHO_STATUS_OF_AT_MOST_X);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.IS_ABLE_AND_WILLING_TO_GIVE_ADEQUATE_INFORMED_CONSENT);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.IS_INVOLVED_IN_STUDY_PROCEDURES);
        RULES_WITH_ONE_INTEGER_PARAMETER.add(EligibilityRule.HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_WEEKS);
        RULES_WITH_ONE_INTEGER_PARAMETER.add(EligibilityRule.HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_MONTHS);

        RULES_WITH_ONE_STRING_PARAMETER.add(EligibilityRule.PRIMARY_TUMOR_LOCATION_BELONGS_TO_DOID_X);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_ADVANCED_CANCER);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_METASTATIC_CANCER);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_LIVER_METASTASES);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_KNOWN_CNS_METASTASES);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_KNOWN_ACTIVE_CNS_METASTASES);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_KNOWN_SYMPTOMATIC_CNS_METASTASES);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_KNOWN_BRAIN_METASTASES);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_KNOWN_ACTIVE_BRAIN_METASTASES);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_KNOWN_SYMPTOMATIC_BRAIN_METASTASES);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_BONE_METASTASES);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_MEASURABLE_DISEASE_RECIST);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_BIOPSY_AMENABLE_LESION);
        RULES_WITH_ONE_INTEGER_PARAMETER.add(EligibilityRule.HAS_COLLECTED_TUMOR_BIOPSY_WITHIN_X_MONTHS_BEFORE_IC);

        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_DECLINED_SOC_TREATMENTS);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY);
        RULES_WITH_ONE_STRING_PARAMETER.add(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY_BELONGING_TO_DOID_X);
        RULES_WITH_ONE_STRING_PARAMETER.add(EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY_BELONGING_TO_DOID_X_CURRENTLY_INACTIVE);
        RULES_WITH_ONE_INTEGER_PARAMETER.add(EligibilityRule.EVERY_SECOND_MALIGNANCY_HAS_BEEN_CURED_SINCE_X_YEARS);
        RULES_WITH_ONE_INTEGER_PARAMETER.add(EligibilityRule.HAS_HAD_AT_MOST_X_SYSTEMIC_TREATMENT_LINES);
        RULES_WITH_ONE_STRING_PARAMETER.add(EligibilityRule.HAS_HAD_DRUG_NAME_X_TREATMENT);
        RULES_WITH_ONE_STRING_PARAMETER.add(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT);
        RULES_WITH_TWO_STRING_PARAMETERS.add(EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPE_Y);
        RULES_WITH_ONE_STRING_ONE_INTEGER_PARAMETER.add(EligibilityRule.HAD_HAD_CATEGORY_X_TREATMENT_WITHIN_Y_WEEKS);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_HAD_FLUOROPYRIMIDINE_TREATMENT);
        RULES_WITH_ONE_INTEGER_PARAMETER.add(EligibilityRule.HAS_HAD_MAX_X_NR_ANTI_PD_L1_OR_PD_1_IMMUNOTHERAPIES);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_HAD_STEM_CELL_TRANSPLANTATION);
        RULES_WITH_ONE_STRING_PARAMETER.add(EligibilityRule.IS_ELIGIBLE_FOR_ON_LABEL_DRUG_X);

        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.MOLECULAR_RESULTS_MUST_BE_AVAILABLE);
        RULES_WITH_ONE_STRING_PARAMETER.add(EligibilityRule.ACTIVATION_OF_GENE_X);
        RULES_WITH_ONE_STRING_PARAMETER.add(EligibilityRule.INACTIVATION_OF_GENE_X);
        RULES_WITH_ONE_STRING_PARAMETER.add(EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X);
        RULES_WITH_TWO_STRING_PARAMETERS.add(EligibilityRule.MUTATION_IN_GENE_X_OF_TYPE_Y);
        RULES_WITH_ONE_STRING_PARAMETER.add(EligibilityRule.INACTIVATING_MUTATION_IN_GENE_X);
        RULES_WITH_ONE_STRING_PARAMETER.add(EligibilityRule.AMPLIFICATION_OF_GENE_X);
        RULES_WITH_ONE_STRING_PARAMETER.add(EligibilityRule.DELETION_OF_GENE_X);
        RULES_WITH_ONE_STRING_PARAMETER.add(EligibilityRule.ACTIVATING_FUSION_IN_GENE_X);
        RULES_WITH_ONE_STRING_PARAMETER.add(EligibilityRule.SPECIFIC_FUSION_X);
        RULES_WITH_ONE_STRING_PARAMETER.add(EligibilityRule.OVEREXPRESSION_OF_GENE_X);
        RULES_WITH_ONE_STRING_PARAMETER.add(EligibilityRule.WILDTYPE_OF_GENE_X);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.MSI_SIGNATURE);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HRD_SIGNATURE);
        RULES_WITH_ONE_DOUBLE_PARAMETER.add(EligibilityRule.TMB_OF_AT_LEAST_X);
        RULES_WITH_ONE_INTEGER_PARAMETER.add(EligibilityRule.TML_OF_AT_LEAST_X);
        RULES_WITH_ONE_INTEGER_PARAMETER.add(EligibilityRule.TML_OF_AT_MOST_X);

        RULES_WITH_ONE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_LEUKOCYTES_ABS_OF_AT_LEAST_X);
        RULES_WITH_ONE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_LEUKOCYTES_ABS_LLN_OF_AT_LEAST_X);
        RULES_WITH_ONE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_NEUTROPHILS_ABS_OF_AT_LEAST_X);
        RULES_WITH_ONE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_THROMBOCYTES_ABS_OF_AT_LEAST_X);
        RULES_WITH_ONE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_HEMOGLOBIN_G_PER_DL_OF_AT_LEAST_X);
        RULES_WITH_ONE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_HEMOGLOBIN_MMOL_PER_L_OF_AT_LEAST_X);
        RULES_WITH_ONE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_ALBUMIN_G_PER_DL_OF_AT_LEAST_X);
        RULES_WITH_ONE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_CREATININE_ULN_OF_AT_MOST_X);
        RULES_WITH_ONE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_EGFR_CKD_EPI_OF_AT_LEAST_X);
        RULES_WITH_ONE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_EGFR_MDRD_OF_AT_LEAST_X);
        RULES_WITH_ONE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_CREATININE_CLEARANCE_CG_OF_AT_LEAST_X);
        RULES_WITH_ONE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_TOTAL_BILIRUBIN_ULN_OF_AT_MOST_X);
        RULES_WITH_ONE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_DIRECT_BILIRUBIN_ULN_OF_AT_MOST_X);
        RULES_WITH_ONE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X);
        RULES_WITH_ONE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_PT_ULN_OF_AT_MOST_X);
        RULES_WITH_ONE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_APTT_ULN_OF_AT_MOST_X);
        RULES_WITH_ONE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_ASAT_ULN_OF_AT_MOST_X);
        RULES_WITH_ONE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_ALAT_ULN_OF_AT_MOST_X);
        RULES_WITH_ONE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_ALP_ULN_OF_AT_MOST_X);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_POTASSIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_MAGNESIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS);

        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_SIGNIFICANT_CONCOMITANT_ILLNESS);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_HISTORY_OF_AUTOIMMUNE_DISEASE);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_HISTORY_OF_CARDIAC_DISEASE);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_HISTORY_OF_CARDIOVASCULAR_DISEASE);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_HISTORY_OF_VASCULAR_DISEASE);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_HISTORY_OF_LUNG_DISEASE);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_HISTORY_OF_STROKE);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_HISTORY_OF_TIA);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_GILBERT_DISEASE);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_HAD_ORGAN_TRANSPLANT);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_CARDIAC_ARRHYTHMIA);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_HYPERTENSION);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_DIABETES);
        RULES_WITH_ONE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_LVEF_OF_AT_LEAST_X);
        RULES_WITH_ONE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_LVEF_OF_AT_LEAST_X_IF_KNOWN);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_KNOWN_MALABSORPTION_SYNDROME);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.IS_IN_DIALYSIS);

        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_ACTIVE_INFECTION);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_KNOWN_HEPATITIS_B_INFECTION);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_KNOWN_HEPATITIS_C_INFECTION);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_KNOWN_HIV_INFECTION);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_KNOWN_CYTOMEGALOVIRUS_INFECTION);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.ADHERENCE_TO_PROTOCOL_REGARDING_ATTENUATED_VACCINE_USE);

        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_ALLERGY_RELATED_TO_STUDY_MEDICATION);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.IS_ABLE_TO_SWALLOW_ORAL_MEDICATION);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.CURRENTLY_GETS_OTHER_ANTI_CANCER_THERAPY);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.CURRENTLY_GETS_ANTIBIOTICS_MEDICATION);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.CURRENTLY_GETS_ANTICOAGULANT_MEDICATION);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.CURRENTLY_GETS_CORTICOSTEROID_MEDICATION);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.CURRENTLY_GETS_COUMADIN_DERIVATIVE_MEDICATION);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.CURRENTLY_GETS_IMMUNOSUPPRESSANT_MEDICATION);
        RULES_WITH_ONE_STRING_PARAMETER.add(EligibilityRule.CURRENTLY_GETS_MEDICATION_INHIBITING_OR_INDUCING_ISOENZYME_X);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.HAS_STABLE_ANTICOAGULANT_DOSING);

        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.IS_BREASTFEEDING);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.IS_PREGNANT);
        RULES_WITHOUT_PARAMETERS.add(EligibilityRule.IS_ABLE_AND_WILLING_TO_USE_ADEQUATE_ANTICONCEPTION_IF_REQUIRED);

        RULES_WITH_ONE_INTEGER_PARAMETER.add(EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X);
        RULES_WITH_ONE_INTEGER_ONE_STRING_PARAMETER.add(EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IN_Y);
        RULES_WITH_ONE_INTEGER_ONE_STRING_PARAMETER.add(EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IGNORING_Y);

        RULES_WITH_ONE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_SBP_MMHG_OF_AT_LEAST_X);
        RULES_WITH_ONE_DOUBLE_PARAMETER.add(EligibilityRule.HAS_DBP_MMHG_OF_AT_LEAST_X);

        RULES_WITH_ONE_INTEGER_PARAMETER.add(EligibilityRule.HAS_HAD_ERYTHROCYTE_TRANSFUSION_WITHIN_LAST_X_WEEKS);
        RULES_WITH_ONE_INTEGER_PARAMETER.add(EligibilityRule.HAS_HAD_THROMBOCYTE_TRANSFUSION_WITHIN_LAST_X_WEEKS);

        RULES_WITH_ONE_INTEGER_PARAMETER.add(EligibilityRule.HAS_HAD_SURGERY_WITHIN_LAST_X_WEEKS);

        RULES_WITH_ONE_STRING_PARAMETER.add(EligibilityRule.PATIENT_IS_TREATED_IN_HOSPITAL_X);
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
                    throw new IllegalStateException("Could not interpret composite inputs for rule '" + function.rule() + "': " + requiredInputs);
                }
                return true;
            }
            if (RULES_WITH_ONE_DOUBLE_PARAMETER.contains(function.rule())) {
                createOneDoubleParameter(function);
                return true;
            } else if (RULES_WITH_ONE_INTEGER_PARAMETER.contains(function.rule())) {
                createOneIntegerParameter(function);
                return true;
            } else if (RULES_WITH_ONE_INTEGER_ONE_STRING_PARAMETER.contains(function.rule())) {
                createOneIntegerOneStringParameter(function);
                return true;
            } else if (RULES_WITH_ONE_STRING_PARAMETER.contains(function.rule())) {
                createOneStringParameter(function);
                return true;
            } else if (RULES_WITH_TWO_STRING_PARAMETERS.contains(function.rule())) {
                createTwoStringParameters(function);
                return true;
            } else if (RULES_WITHOUT_PARAMETERS.contains(function.rule())) {
                return function.parameters().isEmpty();
            } else {
                LOGGER.warn("Rule '{}' not defined in eligibility parameter resolver!", function.rule());
                return null;
            }
        } catch (Exception exception) {
            return false;
        }

    }

    @NotNull
    public static List<Object> createOneIntegerOneStringParameter(@NotNull EligibilityFunction function) {
        assertExpectedParamCount(function, 2);

        return Lists.newArrayList(Integer.parseInt((String) function.parameters().get(0)), function.parameters().get(1));
    }

    public static double createOneDoubleParameter(@NotNull EligibilityFunction function) {
        assertExpectedParamCount(function, 1);

        return Double.parseDouble((String) function.parameters().get(0));
    }

    public static int createOneIntegerParameter(@NotNull EligibilityFunction function) {
        assertExpectedParamCount(function, 1);

        return Integer.parseInt((String) function.parameters().get(0));
    }

    @NotNull
    public static String createOneStringParameter(@NotNull EligibilityFunction function) {
        assertExpectedParamCount(function, 1);

        return (String) function.parameters().get(0);
    }

    @NotNull
    public static List<String> createTwoStringParameters(@NotNull EligibilityFunction function) {
        assertExpectedParamCount(function, 2);

        return Lists.newArrayList((String) function.parameters().get(0), (String) function.parameters().get(1));
    }

    @NotNull
    public static EligibilityFunction createOneCompositeParameter(@NotNull EligibilityFunction function) {
        assertExpectedParamCount(function, 1);

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

    private static void assertExpectedParamCount(@NotNull EligibilityFunction function, int expectedCount) {
        if (function.parameters().size() != expectedCount) {
            throw new IllegalArgumentException(
                    "Invalid number of inputs passed to '" + function.rule() + "': " + function.parameters().size());
        }
    }
}
