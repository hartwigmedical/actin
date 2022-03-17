package com.hartwig.actin.algo.evaluation.laboratory;

import java.time.LocalDate;
import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.calendar.ReferenceDateProvider;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.composite.Fallback;
import com.hartwig.actin.clinical.datamodel.LabUnit;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.FunctionInputResolver;

import org.jetbrains.annotations.NotNull;

public final class LaboratoryRuleMapping {

    private static final int MAX_LAB_VALUE_AGE_DAYS = 30;

    private LaboratoryRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create(@NotNull ReferenceDateProvider referenceDateProvider) {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_LEUKOCYTES_ABS_OF_AT_LEAST_X,
                hasSufficientLabValueCreator(LabMeasurement.LEUKOCYTES_ABS, referenceDateProvider));
        map.put(EligibilityRule.HAS_LEUKOCYTES_ABS_LLN_OF_AT_LEAST_X,
                hasSufficientLabValueLLNCreator(LabMeasurement.LEUKOCYTES_ABS, referenceDateProvider));
        map.put(EligibilityRule.HAS_LYMPHOCYTES_ABS_OF_AT_LEAST_X,
                hasSufficientLabValueCreator(LabMeasurement.LYMPHOCYTES_ABS_EDA, referenceDateProvider));
        map.put(EligibilityRule.HAS_LYMPHOCYTES_CELLS_PER_MM3_OF_AT_LEAST_X,
                hasSufficientLabValueCreator(LabMeasurement.LYMPHOCYTES_ABS_EDA,
                        LabUnit.CELLS_PER_CUBIC_MILLIMETER,
                        referenceDateProvider));
        map.put(EligibilityRule.HAS_NEUTROPHILS_ABS_OF_AT_LEAST_X,
                hasSufficientLabValueCreator(LabMeasurement.NEUTROPHILS_ABS, referenceDateProvider));
        map.put(EligibilityRule.HAS_THROMBOCYTES_ABS_OF_AT_LEAST_X,
                hasSufficientLabValueCreator(LabMeasurement.THROMBOCYTES_ABS, referenceDateProvider));
        map.put(EligibilityRule.HAS_HEMOGLOBIN_G_PER_DL_OF_AT_LEAST_X,
                hasSufficientLabValueCreator(LabMeasurement.HEMOGLOBIN, LabUnit.GRAMS_PER_DECILITER, referenceDateProvider));
        map.put(EligibilityRule.HAS_HEMOGLOBIN_MMOL_PER_L_OF_AT_LEAST_X,
                hasSufficientLabValueCreator(LabMeasurement.HEMOGLOBIN, LabUnit.MILLIMOLES_PER_LITER, referenceDateProvider));

        map.put(EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X,
                hasLimitedLabValueULNCreator(LabMeasurement.INTERNATIONAL_NORMALIZED_RATIO, referenceDateProvider));
        map.put(EligibilityRule.HAS_PT_ULN_OF_AT_MOST_X,
                hasLimitedLabValueULNCreator(LabMeasurement.PROTHROMBIN_TIME, referenceDateProvider));
        map.put(EligibilityRule.HAS_APTT_ULN_OF_AT_MOST_X,
                hasLimitedLabValueULNCreator(LabMeasurement.ACTIVATED_PARTIAL_THROMBOPLASTIN_TIME, referenceDateProvider));
        map.put(EligibilityRule.HAS_PTT_ULN_OF_AT_MOST_X, hasLimitedPTTCreator());

        map.put(EligibilityRule.HAS_ALBUMIN_G_PER_DL_OF_AT_LEAST_X,
                hasSufficientLabValueCreator(LabMeasurement.ALBUMIN, LabUnit.GRAMS_PER_DECILITER, referenceDateProvider));
        map.put(EligibilityRule.HAS_ALBUMIN_LLN_OF_AT_LEAST_X,
                hasSufficientLabValueLLNCreator(LabMeasurement.ALBUMIN, referenceDateProvider));
        map.put(EligibilityRule.HAS_ASAT_ULN_OF_AT_MOST_X,
                hasLimitedLabValueULNCreator(LabMeasurement.ASPARTATE_AMINOTRANSFERASE, referenceDateProvider));
        map.put(EligibilityRule.HAS_ALAT_ULN_OF_AT_MOST_X,
                hasLimitedLabValueULNCreator(LabMeasurement.ALANINE_AMINOTRANSFERASE, referenceDateProvider));
        map.put(EligibilityRule.HAS_ALP_ULN_OF_AT_MOST_X,
                hasLimitedLabValueULNCreator(LabMeasurement.ALKALINE_PHOSPHATASE, referenceDateProvider));
        map.put(EligibilityRule.HAS_TOTAL_BILIRUBIN_ULN_OF_AT_MOST_X,
                hasLimitedLabValueULNCreator(LabMeasurement.TOTAL_BILIRUBIN, referenceDateProvider));
        map.put(EligibilityRule.HAS_TOTAL_BILIRUBIN_UMOL_PER_L_OF_AT_MOST_X,
                hasLimitedLabValueCreator(LabMeasurement.TOTAL_BILIRUBIN, referenceDateProvider));
        map.put(EligibilityRule.HAS_DIRECT_BILIRUBIN_ULN_OF_AT_MOST_X,
                hasLimitedLabValueULNCreator(LabMeasurement.DIRECT_BILIRUBIN, referenceDateProvider));
        map.put(EligibilityRule.HAS_DIRECT_BILIRUBIN_PERCENTAGE_OF_TOTAL_OF_AT_MOST_X,
                hasLimitedBilirubinPercentageCreator(referenceDateProvider));

        map.put(EligibilityRule.HAS_CREATININE_MG_PER_DL_OF_AT_MOST_X,
                hasLimitedLabValueCreator(LabMeasurement.CREATININE, LabUnit.MILLIGRAMS_PER_DECILITER, referenceDateProvider));
        map.put(EligibilityRule.HAS_CREATININE_ULN_OF_AT_MOST_X,
                hasLimitedLabValueULNCreator(LabMeasurement.CREATININE, referenceDateProvider));
        map.put(EligibilityRule.HAS_EGFR_CKD_EPI_OF_AT_LEAST_X,
                hasSufficientCreatinineClearanceCreator(CreatinineClearanceMethod.EGFR_CKD_EPI, referenceDateProvider));
        map.put(EligibilityRule.HAS_EGFR_MDRD_OF_AT_LEAST_X,
                hasSufficientCreatinineClearanceCreator(CreatinineClearanceMethod.EGFR_MDRD, referenceDateProvider));
        map.put(EligibilityRule.HAS_CREATININE_CLEARANCE_CG_OF_AT_LEAST_X,
                hasSufficientCreatinineClearanceCreator(CreatinineClearanceMethod.COCKCROFT_GAULT, referenceDateProvider));

        map.put(EligibilityRule.HAS_BNP_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.NT_PRO_BNP, referenceDateProvider));
        map.put(EligibilityRule.HAS_TROPONIN_IT_ULN_OF_AT_MOST_X,
                hasLimitedLabValueULNCreator(LabMeasurement.TROPONIN_IT, referenceDateProvider));
        map.put(EligibilityRule.HAS_TRIGLYCERIDE_MMOL_PER_L_OF_AT_MOST_X,
                hasLimitedLabValueCreator(LabMeasurement.TRIGLYCERIDE, referenceDateProvider));

        map.put(EligibilityRule.HAS_CALCIUM_MG_PER_DL_OF_AT_MOST_X,
                hasLimitedLabValueCreator(LabMeasurement.CALCIUM, LabUnit.MILLIGRAMS_PER_DECILITER, referenceDateProvider));
        map.put(EligibilityRule.HAS_CALCIUM_MMOL_PER_L_OF_AT_MOST_X,
                hasLimitedLabValueCreator(LabMeasurement.CALCIUM, LabUnit.MILLIMOLES_PER_LITER, referenceDateProvider));
        map.put(EligibilityRule.HAS_IONIZED_CALCIUM_MMOL_PER_L_OF_AT_MOST_X,
                hasLimitedLabValueCreator(LabMeasurement.IONIZED_CALCIUM, referenceDateProvider));
        map.put(EligibilityRule.HAS_CORRECTED_CALCIUM_ULN_OF_AT_MOST_X,
                hasLimitedLabValueULNCreator(LabMeasurement.CORRECTED_CALCIUM, referenceDateProvider));
        map.put(EligibilityRule.HAS_CALCIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS,
                hasLabValueWithinRefCreator(LabMeasurement.CALCIUM, referenceDateProvider));
        map.put(EligibilityRule.HAS_CORRECTED_CALCIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS,
                hasLabValueWithinRefCreator(LabMeasurement.CORRECTED_CALCIUM, referenceDateProvider));
        map.put(EligibilityRule.HAS_MAGNESIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS,
                hasLabValueWithinRefCreator(LabMeasurement.MAGNESIUM, referenceDateProvider));
        map.put(EligibilityRule.HAS_CORRECTED_MAGNESIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS,
                undeterminedLabValueCreator("corrected magnesium"));
        map.put(EligibilityRule.HAS_PHOSPHORUS_ULN_OF_AT_MOST_X,
                hasLimitedLabValueULNCreator(LabMeasurement.PHOSPHORUS, referenceDateProvider));
        map.put(EligibilityRule.HAS_PHOSPHORUS_WITHIN_INSTITUTIONAL_NORMAL_LIMITS,
                hasLabValueWithinRefCreator(LabMeasurement.PHOSPHORUS, referenceDateProvider));
        map.put(EligibilityRule.HAS_CORRECTED_PHOSPHORUS_WITHIN_INSTITUTIONAL_NORMAL_LIMITS,
                undeterminedLabValueCreator("corrected phosphorus"));
        map.put(EligibilityRule.HAS_POTASSIUM_MMOL_PER_L_OF_AT_LEAST_X,
                hasSufficientLabValueCreator(LabMeasurement.POTASSIUM, referenceDateProvider));
        map.put(EligibilityRule.HAS_POTASSIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS,
                hasLabValueWithinRefCreator(LabMeasurement.POTASSIUM, referenceDateProvider));
        map.put(EligibilityRule.HAS_CORRECTED_POTASSIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS,
                undeterminedLabValueCreator("corrected potassium"));

        map.put(EligibilityRule.HAS_SERUM_TESTOSTERONE_NG_PER_DL_OF_AT_MOST_X, undeterminedLabValueCreator("serum testosterone"));

        map.put(EligibilityRule.HAS_AFP_ULN_OF_AT_LEAST_X,
                hasSufficientLabValueCreator(LabMeasurement.ALPHA_FETOPROTEIN, referenceDateProvider));
        map.put(EligibilityRule.HAS_CA125_ULN_OF_AT_LEAST_X, hasSufficientLabValueCreator(LabMeasurement.CA_125, referenceDateProvider));
        map.put(EligibilityRule.HAS_HCG_ULN_OF_AT_LEAST_X,
                hasSufficientLabValueCreator(LabMeasurement.HCG_AND_BETA_HCG, referenceDateProvider));
        map.put(EligibilityRule.HAS_LDH_ULN_OF_AT_MOST_X,
                hasLimitedLabValueULNCreator(LabMeasurement.LACTATE_DEHYDROGENASE, referenceDateProvider));

        map.put(EligibilityRule.HAS_TOTAL_PROTEIN_IN_URINE_OF_AT_LEAST_X,
                hasSufficientLabValueCreator(LabMeasurement.TOTAL_PROTEIN_URINE, referenceDateProvider));
        map.put(EligibilityRule.HAS_TOTAL_PROTEIN_IN_24H_URINE_OF_AT_LEAST_X, undeterminedLabValueCreator("protein in 24h urine"));

        map.put(EligibilityRule.HAS_GLUCOSE_PL_MMOL_PER_L_OF_AT_MOST_X, undeterminedLabValueCreator("Glucose"));

        return map;
    }

    @NotNull
    private static FunctionCreator hasSufficientLabValueCreator(@NotNull LabMeasurement measurement,
            @NotNull ReferenceDateProvider referenceDateProvider) {
        return hasSufficientLabValueCreator(measurement, measurement.defaultUnit(), referenceDateProvider);
    }

    @NotNull
    private static FunctionCreator hasSufficientLabValueCreator(@NotNull LabMeasurement measurement, @NotNull LabUnit targetUnit,
            @NotNull ReferenceDateProvider referenceDateProvider) {
        return function -> {
            double minValue = FunctionInputResolver.createOneDoubleInput(function);
            return createLabEvaluator(measurement, new HasSufficientLabValue(minValue, measurement, targetUnit), referenceDateProvider);
        };
    }

    @NotNull
    private static FunctionCreator hasSufficientLabValueLLNCreator(@NotNull LabMeasurement measurement,
            @NotNull ReferenceDateProvider referenceDateProvider) {
        return function -> {
            double minLLN = FunctionInputResolver.createOneDoubleInput(function);
            return createLabEvaluator(measurement, new HasSufficientLabValueLLN(minLLN), referenceDateProvider);
        };
    }

    @NotNull
    private static FunctionCreator hasLimitedLabValueCreator(@NotNull LabMeasurement measurement,
            @NotNull ReferenceDateProvider referenceDateProvider) {
        return hasLimitedLabValueCreator(measurement, measurement.defaultUnit(), referenceDateProvider);
    }

    @NotNull
    private static FunctionCreator hasLimitedLabValueCreator(@NotNull LabMeasurement measurement, @NotNull LabUnit targetUnit,
            @NotNull ReferenceDateProvider referenceDateProvider) {
        return function -> {
            double maxValue = FunctionInputResolver.createOneDoubleInput(function);
            return createLabEvaluator(measurement, new HasLimitedLabValue(maxValue, measurement, targetUnit), referenceDateProvider);
        };
    }

    @NotNull
    private static FunctionCreator hasLimitedLabValueULNCreator(@NotNull LabMeasurement measurement,
            @NotNull ReferenceDateProvider referenceDateProvider) {
        return function -> {
            double maxULN = FunctionInputResolver.createOneDoubleInput(function);
            return createLabEvaluator(measurement, new HasLimitedLabValueULN(maxULN), referenceDateProvider);
        };
    }

    @NotNull
    private static FunctionCreator hasLabValueWithinRefCreator(@NotNull LabMeasurement measurement,
            @NotNull ReferenceDateProvider referenceDateProvider) {
        return function -> createLabEvaluator(measurement, new HasLabValueWithinRef(), referenceDateProvider);
    }

    @NotNull
    private static FunctionCreator hasLimitedPTTCreator() {
        return function -> new HasLimitedPTT();
    }

    @NotNull
    private static FunctionCreator hasLimitedBilirubinPercentageCreator(@NotNull ReferenceDateProvider referenceDateProvider) {
        return function -> {
            double maxPercentage = FunctionInputResolver.createOneDoubleInput(function);
            return createLabEvaluator(LabMeasurement.DIRECT_BILIRUBIN,
                    new HasLimitedBilirubinPercentageOfTotal(maxPercentage, minValidLabDate(referenceDateProvider)),
                    referenceDateProvider);
        };
    }

    @NotNull
    private static FunctionCreator hasSufficientCreatinineClearanceCreator(@NotNull CreatinineClearanceMethod method,
            @NotNull ReferenceDateProvider referenceDateProvider) {
        return function -> {
            double minCreatinineClearance = FunctionInputResolver.createOneDoubleInput(function);
            LabMeasurement measurement = retrieveForMethod(method);
            EvaluationFunction main = createLabEvaluator(measurement,
                    new HasSufficientLabValue(minCreatinineClearance, measurement, measurement.defaultUnit()),
                    referenceDateProvider);

            EvaluationFunction fallback = createLabEvaluator(LabMeasurement.CREATININE,
                    new HasSufficientDerivedCreatinineClearance(referenceDateProvider.year(), method, minCreatinineClearance),
                    referenceDateProvider);

            return new Fallback(main, fallback);
        };
    }

    @NotNull
    private static LabMeasurement retrieveForMethod(@NotNull CreatinineClearanceMethod method) {
        switch (method) {
            case EGFR_MDRD:
                return LabMeasurement.EGFR_MDRD;
            case EGFR_CKD_EPI:
                return LabMeasurement.EGFR_CKD_EPI;
            case COCKCROFT_GAULT:
                return LabMeasurement.CREATININE_CLEARANCE_CG;
            default: {
                throw new IllegalStateException("No lab measurement defined for " + method);
            }
        }
    }

    @NotNull
    private static FunctionCreator undeterminedLabValueCreator(@NotNull String measure) {
        return function -> record -> ImmutableEvaluation.builder()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("It is not clear yet under what code '" + measure + "' is measured")
                .build();
    }

    @NotNull
    private static EvaluationFunction createLabEvaluator(@NotNull LabMeasurement measurement, @NotNull LabEvaluationFunction function,
            @NotNull ReferenceDateProvider referenceDateProvider) {
        return new LabMeasurementEvaluator(measurement, function, minValidLabDate(referenceDateProvider));
    }

    private static LocalDate minValidLabDate(@NotNull ReferenceDateProvider referenceDateProvider) {
        return referenceDateProvider.date().minusDays(MAX_LAB_VALUE_AGE_DAYS);
    }
}
