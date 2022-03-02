package com.hartwig.actin.algo.evaluation.laboratory;

import java.time.LocalDate;
import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.composite.Fallback;
import com.hartwig.actin.algo.evaluation.util.EvaluationConstants;
import com.hartwig.actin.algo.evaluation.util.EvaluationFactory;
import com.hartwig.actin.clinical.datamodel.LabUnit;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.FunctionInputResolver;

import org.jetbrains.annotations.NotNull;

public final class LaboratoryRuleMapping {

    private static final LocalDate MIN_VALID_LAB_DATE =
            EvaluationConstants.REFERENCE_DATE.minusDays(EvaluationConstants.MAX_LAB_VALUE_AGE_DAYS);

    private LaboratoryRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_LEUKOCYTES_ABS_OF_AT_LEAST_X, hasSufficientLabValueCreator(LabMeasurement.LEUKOCYTES_ABS));
        map.put(EligibilityRule.HAS_LEUKOCYTES_ABS_LLN_OF_AT_LEAST_X, hasSufficientLabValueLLNCreator(LabMeasurement.LEUKOCYTES_ABS));
        map.put(EligibilityRule.HAS_LYMPHOCYTES_ABS_OF_AT_LEAST_X, hasSufficientLabValueCreator(LabMeasurement.LYMPHOCYTES_ABS_EDA));
        map.put(EligibilityRule.HAS_LYMPHOCYTES_CELLS_PER_MM3_OF_AT_LEAST_X,
                hasSufficientLabValueCreator(LabMeasurement.LYMPHOCYTES_ABS_EDA, LabUnit.CELLS_PER_CUBIC_MILLIMETER));
        map.put(EligibilityRule.HAS_NEUTROPHILS_ABS_OF_AT_LEAST_X, hasSufficientLabValueCreator(LabMeasurement.NEUTROPHILS_ABS));
        map.put(EligibilityRule.HAS_THROMBOCYTES_ABS_OF_AT_LEAST_X, hasSufficientLabValueCreator(LabMeasurement.THROMBOCYTES_ABS));
        map.put(EligibilityRule.HAS_HEMOGLOBIN_G_PER_DL_OF_AT_LEAST_X,
                hasSufficientLabValueCreator(LabMeasurement.HEMOGLOBIN, LabUnit.GRAMS_PER_DECILITER));
        map.put(EligibilityRule.HAS_HEMOGLOBIN_MMOL_PER_L_OF_AT_LEAST_X,
                hasSufficientLabValueCreator(LabMeasurement.HEMOGLOBIN, LabUnit.MILLIMOLES_PER_LITER));

        map.put(EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.INTERNATIONAL_NORMALIZED_RATIO));
        map.put(EligibilityRule.HAS_PT_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.PROTHROMBIN_TIME));
        map.put(EligibilityRule.HAS_APTT_ULN_OF_AT_MOST_X,
                hasLimitedLabValueULNCreator(LabMeasurement.ACTIVATED_PARTIAL_THROMBOPLASTIN_TIME));
        map.put(EligibilityRule.HAS_PTT_ULN_OF_AT_MOST_X, hasLimitedPTTCreator());

        map.put(EligibilityRule.HAS_ALBUMIN_G_PER_DL_OF_AT_LEAST_X,
                hasSufficientLabValueCreator(LabMeasurement.ALBUMIN, LabUnit.GRAMS_PER_DECILITER));
        map.put(EligibilityRule.HAS_ALBUMIN_LLN_OF_AT_LEAST_X, hasSufficientLabValueLLNCreator(LabMeasurement.ALBUMIN));
        map.put(EligibilityRule.HAS_ASAT_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.ASPARTATE_AMINOTRANSFERASE));
        map.put(EligibilityRule.HAS_ALAT_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.ALANINE_AMINOTRANSFERASE));
        map.put(EligibilityRule.HAS_ALP_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.ALKALINE_PHOSPHATASE));
        map.put(EligibilityRule.HAS_TOTAL_BILIRUBIN_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.TOTAL_BILIRUBIN));
        map.put(EligibilityRule.HAS_TOTAL_BILIRUBIN_UMOL_PER_L_OF_AT_MOST_X, hasLimitedLabValueCreator(LabMeasurement.TOTAL_BILIRUBIN));
        map.put(EligibilityRule.HAS_DIRECT_BILIRUBIN_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.DIRECT_BILIRUBIN));
        map.put(EligibilityRule.HAS_DIRECT_BILIRUBIN_PERCENTAGE_OF_TOTAL_OF_AT_MOST_X,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));

        map.put(EligibilityRule.HAS_CREATININE_MG_PER_DL_OF_AT_MOST_X,
                hasLimitedLabValueCreator(LabMeasurement.CREATININE, LabUnit.MILLIGRAMS_PER_DECILITER));
        map.put(EligibilityRule.HAS_CREATININE_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.CREATININE));
        map.put(EligibilityRule.HAS_EGFR_CKD_EPI_OF_AT_LEAST_X,
                hasSufficientCreatinineClearanceCreator(CreatinineClearanceMethod.EGFR_CKD_EPI));
        map.put(EligibilityRule.HAS_EGFR_MDRD_OF_AT_LEAST_X, hasSufficientCreatinineClearanceCreator(CreatinineClearanceMethod.EGFR_MDRD));
        map.put(EligibilityRule.HAS_CREATININE_CLEARANCE_CG_OF_AT_LEAST_X,
                hasSufficientCreatinineClearanceCreator(CreatinineClearanceMethod.COCKCROFT_GAULT));

        map.put(EligibilityRule.HAS_BNP_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.NT_PRO_BNP));
        map.put(EligibilityRule.HAS_TROPONIN_IT_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.TROPONIN_IT));
        map.put(EligibilityRule.HAS_TRIGLYCERIDE_MMOL_PER_L_OF_AT_MOST_X, hasLimitedLabValueCreator(LabMeasurement.TRIGLYCERIDE));
        map.put(EligibilityRule.HAS_CALCIUM_MG_PER_DL_OF_AT_MOST_X, hasLimitedCalciumCreator(LabUnit.MILLIGRAMS_PER_DECILITER));
        map.put(EligibilityRule.HAS_CALCIUM_MMOL_PER_L_OF_AT_MOST_X, hasLimitedCalciumCreator(LabUnit.MILLIMOLES_PER_LITER));
        map.put(EligibilityRule.HAS_IONIZED_CALCIUM_MMOL_PER_L_OF_AT_MOST_X, hasLimitedLabValueCreator(LabMeasurement.IONIZED_CALCIUM));
        map.put(EligibilityRule.HAS_CORRECTED_CALCIUM_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.CORRECTED_CALCIUM));
        map.put(EligibilityRule.HAS_CALCIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS, hasLabValueWithinRefCreator(LabMeasurement.CALCIUM));
        map.put(EligibilityRule.HAS_CORRECTED_CALCIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS,
                hasLabValueWithinRefCreator(LabMeasurement.CORRECTED_CALCIUM));
        map.put(EligibilityRule.HAS_MAGNESIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS, hasLabValueWithinRefCreator(LabMeasurement.MAGNESIUM));
        map.put(EligibilityRule.HAS_CORRECTED_MAGNESIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.HAS_PHOSPHORUS_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.PHOSPHORUS));
        map.put(EligibilityRule.HAS_PHOSPHORUS_WITHIN_INSTITUTIONAL_NORMAL_LIMITS, hasLabValueWithinRefCreator(LabMeasurement.PHOSPHORUS));
        map.put(EligibilityRule.HAS_CORRECTED_PHOSPHORUS_WITHIN_INSTITUTIONAL_NORMAL_LIMITS,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.HAS_POTASSIUM_MMOL_PER_L_OF_AT_LEAST_X, hasSufficientLabValueCreator(LabMeasurement.POTASSIUM));
        map.put(EligibilityRule.HAS_POTASSIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS, hasLabValueWithinRefCreator(LabMeasurement.POTASSIUM));
        map.put(EligibilityRule.HAS_CORRECTED_POTASSIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.HAS_SERUM_TESTOSTERONE_NG_PER_DL_OF_AT_MOST_X,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.HAS_AFP_ULN_OF_AT_LEAST_X,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.HAS_CA125_ULN_OF_AT_LEAST_X,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.HAS_HCG_ULN_OF_AT_LEAST_X,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.HAS_LDH_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.LACTATE_DEHYDROGENASE));
        map.put(EligibilityRule.HAS_TOTAL_PROTEIN_IN_URINE_OF_AT_LEAST_X, hasSufficientLabValueCreator(LabMeasurement.TOTAL_PROTEIN_URINE));
        map.put(EligibilityRule.HAS_TOTAL_PROTEIN_IN_24H_URINE_OF_AT_LEAST_X,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.HAS_GLUCOSE_PL_MMOL_PER_L_OF_AT_MOST_X,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));

        return map;
    }

    @NotNull
    private static FunctionCreator hasSufficientLabValueCreator(@NotNull LabMeasurement measurement) {
        return hasSufficientLabValueCreator(measurement, measurement.defaultUnit());
    }

    @NotNull
    private static FunctionCreator hasSufficientLabValueCreator(@NotNull LabMeasurement measurement, @NotNull LabUnit targetUnit) {
        return function -> {
            double minValue = FunctionInputResolver.createOneDoubleInput(function);
            return createLabEvaluator(measurement, new HasSufficientLabValue(minValue, measurement, targetUnit));
        };
    }

    @NotNull
    private static FunctionCreator hasSufficientLabValueLLNCreator(@NotNull LabMeasurement measurement) {
        return function -> {
            double minLLN = FunctionInputResolver.createOneDoubleInput(function);
            return createLabEvaluator(measurement, new HasSufficientLabValueLLN(minLLN));
        };
    }

    @NotNull
    private static FunctionCreator hasLimitedLabValueULNCreator(@NotNull LabMeasurement measurement) {
        return function -> {
            double maxULN = FunctionInputResolver.createOneDoubleInput(function);
            return createLabEvaluator(measurement, new HasLimitedLabValueULN(maxULN));
        };
    }

    @NotNull
    private static FunctionCreator hasLimitedLabValueCreator(@NotNull LabMeasurement measurement) {
        return hasLimitedLabValueCreator(measurement, measurement.defaultUnit());
    }

    @NotNull
    private static FunctionCreator hasLimitedLabValueCreator(@NotNull LabMeasurement measurement, @NotNull LabUnit targetUnit) {
        return function -> {
            double maxValue = FunctionInputResolver.createOneDoubleInput(function);
            return createLabEvaluator(measurement, new HasLimitedLabValue(maxValue, measurement, targetUnit));
        };
    }

    @NotNull
    private static FunctionCreator hasLabValueWithinRefCreator(@NotNull LabMeasurement measurement) {
        return function -> createLabEvaluator(measurement, new HasLabValueWithinRef());
    }

    @NotNull
    private static FunctionCreator hasLimitedPTTCreator() {
        return function -> new HasLimitedPTT();
    }


    @NotNull
    private static FunctionCreator hasSufficientCreatinineClearanceCreator(@NotNull CreatinineClearanceMethod method) {
        return function -> {
            double minCreatinineClearance = FunctionInputResolver.createOneDoubleInput(function);
            EvaluationFunction main = createLabEvaluator(retrieveForMethod(method),
                    new HasSufficientLabValue(minCreatinineClearance, LabMeasurement.CREATININE, LabMeasurement.CREATININE.defaultUnit()));

            EvaluationFunction fallback = createLabEvaluator(LabMeasurement.CREATININE,
                    new HasSufficientDerivedCreatinineClearance(EvaluationConstants.REFERENCE_YEAR, method, minCreatinineClearance));

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
    private static FunctionCreator hasLimitedCalciumCreator(@NotNull LabUnit unit) {
        // TODO
        return function -> evaluation -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED);
    }

    @NotNull
    private static EvaluationFunction createLabEvaluator(@NotNull LabMeasurement measurement, @NotNull LabEvaluationFunction function) {
        return new LabMeasurementEvaluator(measurement, function, MIN_VALID_LAB_DATE);
    }
}
