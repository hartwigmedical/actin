package com.hartwig.actin.algo.evaluation.laboratory;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.Evaluation;
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

        map.put(EligibilityRule.HAS_LEUKOCYTES_ABS_OF_AT_LEAST_X, hasSufficientAbsLeukocytesCreator());
        map.put(EligibilityRule.HAS_LEUKOCYTES_ABS_LLN_OF_AT_LEAST_X, hasSufficientAbsLeukocytesLLNCreator());
        map.put(EligibilityRule.HAS_NEUTROPHILS_ABS_OF_AT_LEAST_X, hasSufficientAbsNeutrophilsCreator());
        map.put(EligibilityRule.HAS_THROMBOCYTES_ABS_OF_AT_LEAST_X, hasSufficientThrombocytesCreator());
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
        map.put(EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.INR));
        map.put(EligibilityRule.HAS_PT_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.PT));
        map.put(EligibilityRule.HAS_APTT_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.APTT));
        map.put(EligibilityRule.HAS_ASAT_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.ASAT));
        map.put(EligibilityRule.HAS_ALAT_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.ALAT));
        map.put(EligibilityRule.HAS_ALP_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.ALP));
        map.put(EligibilityRule.HAS_POTASSIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS, notImplementedCreator());
        map.put(EligibilityRule.HAS_MAGNESIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS, notImplementedCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator hasSufficientAbsLeukocytesCreator() {
        return function -> {
            double minLeukocytes = EligibilityParameterResolver.createOneDoubleInput(function);
            return new HasSufficientAbsLeukocytes(minLeukocytes);
        };
    }

    @NotNull
    private static FunctionCreator hasSufficientAbsLeukocytesLLNCreator() {
        return function -> {
            double minLeukocytesLLN = EligibilityParameterResolver.createOneDoubleInput(function);
            return new HasSufficientAbsLeukocytesLLN(minLeukocytesLLN);
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
    private static FunctionCreator hasSufficientThrombocytesCreator() {
        return function -> {
            double minThrombocytes = EligibilityParameterResolver.createOneDoubleInput(function);
            return new HasSufficientThrombocytes(minThrombocytes);
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
    private static FunctionCreator notImplementedCreator() {
        return function -> evaluation -> Evaluation.NOT_IMPLEMENTED;
    }
}
