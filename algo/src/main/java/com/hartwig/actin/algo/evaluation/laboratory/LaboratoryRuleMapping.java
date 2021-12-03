package com.hartwig.actin.algo.evaluation.laboratory;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationConstants;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.Eligibility;
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
        map.put(EligibilityRule.HAS_LEUKOCYTES_ABS_LLN_OF_AT_LEAST_X, notImplementedCreator());
        map.put(EligibilityRule.HAS_NEUTROPHILS_ABS_OF_AT_LEAST_X, hasSufficientAbsNeutrophilsCreator());
        map.put(EligibilityRule.HAS_THROMBOCYTES_ABS_OF_AT_LEAST_X, hasSufficientThrombocytesCreator());
        map.put(EligibilityRule.HAS_ALBUMIN_G_PER_DL_OF_AT_LEAST_X, notImplementedCreator());
        map.put(EligibilityRule.HAS_HEMOGLOBIN_G_PER_DL_OF_AT_LEAST_X, hasSufficientHemoglobinCreator(LabUnit.G_PER_DL));
        map.put(EligibilityRule.HAS_HEMOGLOBIN_MMOL_PER_L_OF_AT_LEAST_X, hasSufficientHemoglobinCreator(LabUnit.MMOL_PER_L));
        map.put(EligibilityRule.HAS_CREATININE_ULN_OF_AT_MOST_X, hasLimitedCreatinineULNCreator());
        map.put(EligibilityRule.HAS_EGFR_CKD_EPI_OF_AT_LEAST_X,
                hasSufficientCreatinineClearanceCreator(CreatinineClearanceMethod.EGFR_CDK_EPI));
        map.put(EligibilityRule.HAS_EGFR_MDRD_OF_AT_LEAST_X, hasSufficientCreatinineClearanceCreator(CreatinineClearanceMethod.EGFR_MDRD));
        map.put(EligibilityRule.HAS_CREATININE_CLEARANCE_CG_OF_AT_LEAST_X,
                hasSufficientCreatinineClearanceCreator(CreatinineClearanceMethod.COCKCROFT_GAULT));
        map.put(EligibilityRule.HAS_TOTAL_BILIRUBIN_ULN_OF_AT_MOST_X, hasLimitedTotalBilirubinCreator());
        map.put(EligibilityRule.HAS_DIRECT_BILIRUBIN_ULN_OF_AT_MOST_X, hasLimitedDirectBilirubinCreator());
        map.put(EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X, hasLimitedINRCreator());
        map.put(EligibilityRule.HAS_PT_ULN_OF_AT_MOST_X, hasLimitedPTCreator());
        map.put(EligibilityRule.HAS_APTT_ULN_OF_AT_MOST_X, hasLimitedAPPTCreator());
        map.put(EligibilityRule.HAS_ASAT_ULN_OF_AT_MOST_X, hasLimitedASATCreator());
        map.put(EligibilityRule.HAS_ALAT_ULN_OF_AT_MOST_X, notImplementedCreator());
        map.put(EligibilityRule.HAS_ALP_ULN_OF_AT_MOST_X, notImplementedCreator());
        map.put(EligibilityRule.HAS_POTASSIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS, notImplementedCreator());
        map.put(EligibilityRule.HAS_MAGNESIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS, notImplementedCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator hasSufficientAbsLeukocytesCreator() {
        return function -> {
            double minLeukocytes = EligibilityParameterResolver.createOneDoubleParameter(function);
            return new HasSufficientAbsLeukocytes(minLeukocytes);
        };
    }

    @NotNull
    private static FunctionCreator hasSufficientAbsNeutrophilsCreator() {
        return function -> {
            double minNeutrophils = EligibilityParameterResolver.createOneDoubleParameter(function);
            return new HasSufficientAbsNeutrophils(minNeutrophils);
        };
    }

    @NotNull
    private static FunctionCreator hasSufficientThrombocytesCreator() {
        return function -> {
            double minThrombocytes = EligibilityParameterResolver.createOneDoubleParameter(function);
            return new HasSufficientThrombocytes(minThrombocytes);
        };
    }

    @NotNull
    private static FunctionCreator hasSufficientHemoglobinCreator(@NotNull LabUnit targetUnit) {
        return function -> {
            double minHemoglobin = EligibilityParameterResolver.createOneDoubleParameter(function);
            return new HasSufficientHemoglobin(minHemoglobin, targetUnit);
        };
    }

    @NotNull
    private static FunctionCreator hasLimitedCreatinineULNCreator() {
        return function -> {
            double maxCreatinineULN = EligibilityParameterResolver.createOneDoubleParameter(function);
            return new HasLimitedCreatinineULN(maxCreatinineULN);
        };
    }

    @NotNull
    private static FunctionCreator hasSufficientCreatinineClearanceCreator(@NotNull CreatinineClearanceMethod method) {
        return function -> {
            double minCreatinineClearance = EligibilityParameterResolver.createOneDoubleParameter(function);
            return new HasSufficientCreatinineClearance(EvaluationConstants.REFERENCE_YEAR, method, minCreatinineClearance);
        };
    }

    @NotNull
    private static FunctionCreator hasLimitedTotalBilirubinCreator() {
        return function -> new HasLimitedTotalBilirubin();
    }

    @NotNull
    private static FunctionCreator hasLimitedDirectBilirubinCreator() {
        return function -> new HasLimitedDirectBilirubin();
    }

    @NotNull
    private static FunctionCreator hasLimitedINRCreator() {
        return function -> new HasLimitedINR();
    }

    @NotNull
    private static FunctionCreator hasLimitedPTCreator() {
        return function -> new HasLimitedPT();
    }

    @NotNull
    private static FunctionCreator hasLimitedAPPTCreator() {
        return function -> new HasLimitedAPTT();
    }

    @NotNull
    private static FunctionCreator hasLimitedASATCreator() {
        return function -> new HasLimitedASAT();
    }

    @NotNull
    private static FunctionCreator notImplementedCreator() {
        return function -> evaluation -> Evaluation.NOT_IMPLEMENTED;
    }
}
