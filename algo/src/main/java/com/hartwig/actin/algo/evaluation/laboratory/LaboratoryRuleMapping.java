package com.hartwig.actin.algo.evaluation.laboratory;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.EvaluationConstants;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.composite.Fallback;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.EligibilityParameterResolver;

import org.jetbrains.annotations.NotNull;

public final class LaboratoryRuleMapping {

    private LaboratoryRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_LEUKOCYTES_ABS_OF_AT_LEAST_X, hasSufficientLabValueCreator(LabMeasurement.LEUKOCYTES_ABS));
        map.put(EligibilityRule.HAS_LEUKOCYTES_ABS_LLN_OF_AT_LEAST_X, hasSufficientLabValueLLNCreator(LabMeasurement.LEUKOCYTES_ABS));
        map.put(EligibilityRule.HAS_NEUTROPHILS_ABS_OF_AT_LEAST_X, hasSufficientLabValueCreator(LabMeasurement.NEUTROPHILS_ABS));
        map.put(EligibilityRule.HAS_THROMBOCYTES_ABS_OF_AT_LEAST_X, hasSufficientLabValueCreator(LabMeasurement.THROMBOCYTES_ABS));
        map.put(EligibilityRule.HAS_ALBUMIN_G_PER_DL_OF_AT_LEAST_X, hasSufficientAlbuminCreator());
        map.put(EligibilityRule.HAS_HEMOGLOBIN_G_PER_DL_OF_AT_LEAST_X, hasSufficientHemoglobinCreator(LabUnit.G_PER_DL));
        map.put(EligibilityRule.HAS_HEMOGLOBIN_MMOL_PER_L_OF_AT_LEAST_X, hasSufficientHemoglobinCreator(LabUnit.MMOL_PER_L));
        map.put(EligibilityRule.HAS_CREATININE_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.CREATININE));
        map.put(EligibilityRule.HAS_EGFR_CKD_EPI_OF_AT_LEAST_X,
                hasSufficientCreatinineClearanceCreator(CreatinineClearanceMethod.EGFR_CKD_EPI));
        map.put(EligibilityRule.HAS_EGFR_MDRD_OF_AT_LEAST_X, hasSufficientCreatinineClearanceCreator(CreatinineClearanceMethod.EGFR_MDRD));
        map.put(EligibilityRule.HAS_CREATININE_CLEARANCE_CG_OF_AT_LEAST_X,
                hasSufficientCreatinineClearanceCreator(CreatinineClearanceMethod.COCKCROFT_GAULT));
        map.put(EligibilityRule.HAS_TOTAL_BILIRUBIN_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.TOTAL_BILIRUBIN));
        map.put(EligibilityRule.HAS_DIRECT_BILIRUBIN_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.DIRECT_BILIRUBIN));
        map.put(EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.INTERNATIONAL_NORMALIZED_RATIO));
        map.put(EligibilityRule.HAS_PT_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.PROTHROMBIN_TIME));
        map.put(EligibilityRule.HAS_APTT_ULN_OF_AT_MOST_X,
                hasLimitedLabValueULNCreator(LabMeasurement.ACTIVATED_PARTIAL_THROMBOPLASTIN_TIME));
        map.put(EligibilityRule.HAS_ASAT_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.ASPARTATE_AMINOTRANSFERASE));
        map.put(EligibilityRule.HAS_ALAT_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.ALANINE_AMINOTRANSFERASE));
        map.put(EligibilityRule.HAS_ALP_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.ALKALINE_PHOSPHATASE));
        map.put(EligibilityRule.HAS_POTASSIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS, hasLabValueWithinRefCreator(LabMeasurement.POTASSIUM));
        map.put(EligibilityRule.HAS_MAGNESIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS, hasLabValueWithinRefCreator(LabMeasurement.MAGNESIUM));

        return map;
    }

    @NotNull
    private static FunctionCreator hasSufficientLabValueCreator(@NotNull LabMeasurement measurement) {
        return function -> {
            double minValue = EligibilityParameterResolver.createOneDoubleInput(function);
            return new LabMeasurementEvaluator(measurement, new HasSufficientLabValue(minValue));
        };
    }

    @NotNull
    private static FunctionCreator hasSufficientLabValueLLNCreator(@NotNull LabMeasurement measurement) {
        return function -> {
            double minLLN = EligibilityParameterResolver.createOneDoubleInput(function);
            return new LabMeasurementEvaluator(measurement, new HasSufficientLabValueLLN(minLLN));
        };
    }

    @NotNull
    private static FunctionCreator hasSufficientAlbuminCreator() {
        return function -> {
            double minAlbuminGPerDL = EligibilityParameterResolver.createOneDoubleInput(function);
            return new LabMeasurementEvaluator(LabMeasurement.ALBUMIN, new HasSufficientAlbumin(minAlbuminGPerDL));
        };
    }

    @NotNull
    private static FunctionCreator hasSufficientHemoglobinCreator(@NotNull LabUnit targetUnit) {
        return function -> {
            double minHemoglobin = EligibilityParameterResolver.createOneDoubleInput(function);
            return new LabMeasurementEvaluator(LabMeasurement.HEMOGLOBIN, new HasSufficientHemoglobin(minHemoglobin, targetUnit));
        };
    }

    @NotNull
    private static FunctionCreator hasLimitedLabValueULNCreator(@NotNull LabMeasurement measurement) {
        return function -> {
            double maxULN = EligibilityParameterResolver.createOneDoubleInput(function);
            return new LabMeasurementEvaluator(measurement, new HasLimitedLabValueULN(maxULN));
        };
    }

    @NotNull
    private static FunctionCreator hasSufficientCreatinineClearanceCreator(@NotNull CreatinineClearanceMethod method) {
        return function -> {
            double minCreatinineClearance = EligibilityParameterResolver.createOneDoubleInput(function);
            return new Fallback(new LabMeasurementEvaluator(retrieveForMethod(method), new HasSufficientLabValue(minCreatinineClearance)),
                    new LabMeasurementEvaluator(LabMeasurement.CREATININE,
                            new HasSufficientDerivedCreatinineClearance(EvaluationConstants.REFERENCE_YEAR,
                                    method,
                                    minCreatinineClearance)));
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
    private static FunctionCreator hasLabValueWithinRefCreator(@NotNull LabMeasurement measurement) {
        return function -> new LabMeasurementEvaluator(measurement, new HasLabValueWithinRef());
    }
}
