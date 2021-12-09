package com.hartwig.actin.algo.evaluation.laboratory;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.EvaluationConstants;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
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
        map.put(EligibilityRule.HAS_NEUTROPHILS_ABS_OF_AT_LEAST_X, hasSufficientAbsNeutrophilsCreator());
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
        map.put(EligibilityRule.HAS_POTASSIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS, hasValueWithinRefCreator(LabMeasurement.POTASSIUM));
        map.put(EligibilityRule.HAS_MAGNESIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS, hasValueWithinRefCreator(LabMeasurement.MAGNESIUM));

        return map;
    }

    @NotNull
    private static FunctionCreator hasSufficientLabValueLLNCreator(@NotNull LabMeasurement measurement) {
        return function -> {
            double minLLN = EligibilityParameterResolver.createOneDoubleInput(function);
            return new HasSufficientLabValueLLN(measurement, minLLN);
        };
    }

    @NotNull
    private static FunctionCreator hasSufficientAbsNeutrophilsCreator() {
        return function -> {
            double minNeutrophils = EligibilityParameterResolver.createOneDoubleInput(function);
            return new HasSufficientAbsNeutrophils(minNeutrophils);
        };
    }

    @NotNull
    private static FunctionCreator hasSufficientLabValueCreator(@NotNull LabMeasurement measurement) {
        return function -> {
            double minValue = EligibilityParameterResolver.createOneDoubleInput(function);
            return new HasSufficientLabValue(measurement, minValue);
        };
    }

    @NotNull
    private static FunctionCreator hasSufficientAlbuminCreator() {
        return function -> {
            double minAlbuminGPerDL = EligibilityParameterResolver.createOneDoubleInput(function);
            return new HasSufficientAlbumin(minAlbuminGPerDL);
        };
    }

    @NotNull
    private static FunctionCreator hasSufficientHemoglobinCreator(@NotNull LabUnit targetUnit) {
        return function -> {
            double minHemoglobin = EligibilityParameterResolver.createOneDoubleInput(function);
            return new HasSufficientHemoglobin(minHemoglobin, targetUnit);
        };
    }

    @NotNull
    private static FunctionCreator hasLimitedLabValueULNCreator(@NotNull LabMeasurement measurement) {
        return function -> {
            double maxULN = EligibilityParameterResolver.createOneDoubleInput(function);
            return new HasLimitedLabValueULN(measurement, maxULN);
        };
    }

    @NotNull
    private static FunctionCreator hasSufficientCreatinineClearanceCreator(@NotNull CreatinineClearanceMethod method) {
        return function -> {
            double minCreatinineClearance = EligibilityParameterResolver.createOneDoubleInput(function);
            return new HasSufficientCreatinineClearance(EvaluationConstants.REFERENCE_YEAR, method, minCreatinineClearance);
        };
    }

    @NotNull
    private static FunctionCreator hasValueWithinRefCreator(@NotNull LabMeasurement measurement) {
        return function -> new HasLabValueWithinRef(measurement);
    }
}
