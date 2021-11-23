package com.hartwig.actin.algo.evaluation.laboratory;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public final class LaboratoryRuleMapping {

    private LaboratoryRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_LEUKOCYTES_ABS_OF_AT_LEAST_X, hasSufficientAbsLeukocytesCreator());
        map.put(EligibilityRule.HAS_NEUTROPHILS_ABS_OF_AT_LEAST_X, notImplementedCreator());
        map.put(EligibilityRule.HAS_THROMBOCYTES_ABS_AT_LEAST_X, hasSufficientThrombocytesCreator());
        map.put(EligibilityRule.HAS_HEMOGLOBIN_G_PER_DL_OF_AT_LEAST_X, hasSufficientHemoglobinCreator());
        map.put(EligibilityRule.HAS_HEMOGLOBIN_MMOL_PER_L_OF_AT_LEAST_X, notImplementedCreator());
        map.put(EligibilityRule.HAS_CREATININE_ULN_OF_AT_MOST_X, hasSufficientCreatinineCreator());
        map.put(EligibilityRule.HAS_EGFR_CKD_EPI_OF_AT_LEAST_X, hasSufficientCreatinineClearanceCKDEPICreator());
        map.put(EligibilityRule.HAS_EGFR_MDRD_OF_AT_LEAST_X, notImplementedCreator());
        map.put(EligibilityRule.HAS_CREATININE_CLEARANCE_CG_OF_AT_LEAST_X, notImplementedCreator());
        map.put(EligibilityRule.HAS_TOTAL_BILIRUBIN_ULN_AT_MOST_X, hasLimitedTotalBilirubinCreator());
        map.put(EligibilityRule.HAS_DIRECT_BILIRUBIN_ULN_AT_MOST_X, hasLimitedDirectBilirubinCreator());
        map.put(EligibilityRule.HAS_INR_ULN_AT_MOST_X, hasLimitedINRCreator());
        map.put(EligibilityRule.HAS_PT_ULN_AT_MOST_X, hasLimitedPTCreator());
        map.put(EligibilityRule.HAS_APTT_ULN_AT_MOST_X, hasLimitedAPPTCreator());
        map.put(EligibilityRule.HAS_ASAT_ULN_OF_AT_MOST_X, hasLimitedASATCreator());
        map.put(EligibilityRule.HAS_ALAT_ULN_OF_AT_MOST_X, notImplementedCreator());
        map.put(EligibilityRule.HAS_ALP_ULN_OF_AT_MOST_X, notImplementedCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator hasSufficientAbsLeukocytesCreator() {
        return function -> new HasSufficientAbsLeukocytes();
    }

    @NotNull
    private static FunctionCreator hasSufficientThrombocytesCreator() {
        return function -> new HasSufficientThrombocytes();
    }

    @NotNull
    private static FunctionCreator hasSufficientHemoglobinCreator() {
        return function -> new HasSufficientHemoglobin();
    }

    @NotNull
    private static FunctionCreator hasSufficientCreatinineCreator() {
        return function -> new HasSufficientCreatinine();
    }

    @NotNull
    private static FunctionCreator hasSufficientCreatinineClearanceCKDEPICreator() {
        return function -> new HasSufficientCreatinineClearanceCKDEPI();
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
