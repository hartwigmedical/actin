package com.hartwig.actin.algo.evaluation.laboratory;

import java.time.LocalDate;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.RuleMapper;
import com.hartwig.actin.algo.evaluation.RuleMappingResources;
import com.hartwig.actin.algo.evaluation.composite.And;
import com.hartwig.actin.algo.evaluation.composite.Fallback;
import com.hartwig.actin.algo.evaluation.composite.Not;
import com.hartwig.actin.algo.evaluation.composite.Or;
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionFunctionFactory;
import com.hartwig.actin.clinical.datamodel.LabUnit;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.single.TwoDoubles;

import org.jetbrains.annotations.NotNull;

public class LaboratoryRuleMapper extends RuleMapper {

    private static final int MAX_LAB_VALUE_AGE_DAYS = 30;

    private static final String AUTOSOMAL_DOMINANT_HYPOCALCEMIA_DOID = "0090109";
    private static final String PRIMARY_HYPOMAGNESEMIA_DOID = "0060879";
    private static final String HYPOKALEMIA_DOID = "4500";

    public LaboratoryRuleMapper(@NotNull final RuleMappingResources resources) {
        super(resources);
    }

    @NotNull
    @Override
    public Map<EligibilityRule, FunctionCreator> createMappings() {
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
        map.put(EligibilityRule.HAS_D_DIMER_OUTSIDE_REF_UPPER_LIMIT, hasLabValueOutsideRefLimitUpCreator(LabMeasurement.DDIMER));

        map.put(EligibilityRule.HAS_ALBUMIN_G_PER_DL_OF_AT_LEAST_X,
                hasSufficientLabValueCreator(LabMeasurement.ALBUMIN, LabUnit.GRAMS_PER_DECILITER));
        map.put(EligibilityRule.HAS_ALBUMIN_LLN_OF_AT_LEAST_X, hasSufficientLabValueLLNCreator(LabMeasurement.ALBUMIN));
        map.put(EligibilityRule.HAS_ASAT_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.ASPARTATE_AMINOTRANSFERASE));
        map.put(EligibilityRule.HAS_ALAT_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.ALANINE_AMINOTRANSFERASE));
        map.put(EligibilityRule.HAS_ALP_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.ALKALINE_PHOSPHATASE));
        map.put(EligibilityRule.HAS_ALP_ULN_OF_AT_LEAST_X, hasSufficientLabValueULNCreator(LabMeasurement.ALKALINE_PHOSPHATASE));
        map.put(EligibilityRule.HAS_TOTAL_BILIRUBIN_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.TOTAL_BILIRUBIN));
        map.put(EligibilityRule.HAS_TOTAL_BILIRUBIN_UMOL_PER_L_OF_AT_MOST_X, hasLimitedLabValueCreator(LabMeasurement.TOTAL_BILIRUBIN));
        map.put(EligibilityRule.HAS_DIRECT_BILIRUBIN_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.DIRECT_BILIRUBIN));
        map.put(EligibilityRule.HAS_DIRECT_BILIRUBIN_PERCENTAGE_OF_TOTAL_OF_AT_MOST_X, hasLimitedBilirubinPercentageCreator());

        map.put(EligibilityRule.HAS_CREATININE_MG_PER_DL_OF_AT_MOST_X,
                hasLimitedLabValueCreator(LabMeasurement.CREATININE, LabUnit.MILLIGRAMS_PER_DECILITER));
        map.put(EligibilityRule.HAS_CREATININE_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.CREATININE));
        map.put(EligibilityRule.HAS_EGFR_CKD_EPI_OF_AT_LEAST_X,
                hasSufficientCreatinineClearanceCreator(CreatinineClearanceMethod.EGFR_CKD_EPI));
        map.put(EligibilityRule.HAS_EGFR_MDRD_OF_AT_LEAST_X, hasSufficientCreatinineClearanceCreator(CreatinineClearanceMethod.EGFR_MDRD));
        map.put(EligibilityRule.HAS_CREATININE_CLEARANCE_CG_OF_AT_LEAST_X,
                hasSufficientCreatinineClearanceCreator(CreatinineClearanceMethod.COCKCROFT_GAULT));
        map.put(EligibilityRule.HAS_CREATININE_CLEARANCE_BETWEEN_X_AND_Y,
                hasCreatinineClearanceBetweenValuesCreator(CreatinineClearanceMethod.COCKCROFT_GAULT));

        map.put(EligibilityRule.HAS_BNP_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.NT_PRO_BNP));
        map.put(EligibilityRule.HAS_TROPONIN_IT_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.TROPONIN_IT));
        map.put(EligibilityRule.HAS_TRIGLYCERIDE_MMOL_PER_L_OF_AT_MOST_X, hasLimitedLabValueCreator(LabMeasurement.TRIGLYCERIDE));

        map.put(EligibilityRule.HAS_AMYLASE_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.AMYLASE));
        map.put(EligibilityRule.HAS_LIPASE_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.LIPASE));

        map.put(EligibilityRule.HAS_CALCIUM_MG_PER_DL_OF_AT_MOST_X,
                hasLimitedLabValueCreator(LabMeasurement.CALCIUM, LabUnit.MILLIGRAMS_PER_DECILITER));
        map.put(EligibilityRule.HAS_IONIZED_CALCIUM_MMOL_PER_L_OF_AT_MOST_X, hasLimitedLabValueCreator(LabMeasurement.IONIZED_CALCIUM));
        map.put(EligibilityRule.HAS_CORRECTED_CALCIUM_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.CORRECTED_CALCIUM));
        map.put(EligibilityRule.HAS_CALCIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS, hasLabValueWithinRefCreator(LabMeasurement.CALCIUM));
        map.put(EligibilityRule.HAS_CORRECTED_CALCIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS,
                hasLabValueWithinRefCreator(LabMeasurement.CORRECTED_CALCIUM));
        map.put(EligibilityRule.HAS_MAGNESIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS, hasLabValueWithinRefCreator(LabMeasurement.MAGNESIUM));
        map.put(EligibilityRule.HAS_CORRECTED_MAGNESIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS,
                undeterminedLabValueCreator("corrected magnesium"));
        map.put(EligibilityRule.HAS_PHOSPHORUS_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.PHOSPHORUS));
        map.put(EligibilityRule.HAS_PHOSPHORUS_WITHIN_INSTITUTIONAL_NORMAL_LIMITS, hasLabValueWithinRefCreator(LabMeasurement.PHOSPHORUS));
        map.put(EligibilityRule.HAS_CORRECTED_PHOSPHORUS_WITHIN_INSTITUTIONAL_NORMAL_LIMITS,
                undeterminedLabValueCreator("corrected phosphorus"));
        map.put(EligibilityRule.HAS_POTASSIUM_MMOL_PER_L_OF_AT_LEAST_X, hasSufficientLabValueCreator(LabMeasurement.POTASSIUM));
        map.put(EligibilityRule.HAS_POTASSIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS, hasLabValueWithinRefCreator(LabMeasurement.POTASSIUM));
        map.put(EligibilityRule.HAS_CORRECTED_POTASSIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS,
                undeterminedLabValueCreator("corrected potassium"));
        map.put(EligibilityRule.HAS_POTENTIAL_HYPOKALEMIA, hasPotentialHypokalemiaCreator());
        map.put(EligibilityRule.HAS_POTENTIAL_HYPOMAGNESEMIA, hasPotentialHypomagnesemiaCreator());
        map.put(EligibilityRule.HAS_POTENTIAL_HYPOCALCEMIA, hasPotentialHypocalcemiaCreator());

        map.put(EligibilityRule.HAS_SERUM_TESTOSTERONE_NG_PER_DL_OF_AT_MOST_X, undeterminedLabValueCreator("serum testosterone"));

        map.put(EligibilityRule.HAS_AFP_ULN_OF_AT_LEAST_X, hasSufficientLabValueCreator(LabMeasurement.ALPHA_FETOPROTEIN));
        map.put(EligibilityRule.HAS_CA125_ULN_OF_AT_LEAST_X, hasSufficientLabValueCreator(LabMeasurement.CA_125));
        map.put(EligibilityRule.HAS_HCG_ULN_OF_AT_LEAST_X, hasSufficientLabValueCreator(LabMeasurement.HCG_AND_BETA_HCG));
        map.put(EligibilityRule.HAS_LDH_ULN_OF_AT_MOST_X, hasLimitedLabValueULNCreator(LabMeasurement.LACTATE_DEHYDROGENASE));
        map.put(EligibilityRule.HAS_PSA_UG_PER_L_OF_AT_LEAST_X, hasSufficientLabValueCreator(LabMeasurement.PSA));
        map.put(EligibilityRule.HAS_PSA_LLN_OF_AT_LEAST_X, hasSufficientLabValueLLNCreator(LabMeasurement.PSA));

        map.put(EligibilityRule.HAS_TOTAL_PROTEIN_IN_URINE_OF_AT_LEAST_X, hasSufficientLabValueCreator(LabMeasurement.TOTAL_PROTEIN_URINE));
        map.put(EligibilityRule.HAS_TOTAL_PROTEIN_IN_24H_URINE_OF_AT_LEAST_X, undeterminedLabValueCreator("protein in 24h urine"));

        map.put(EligibilityRule.HAS_GLUCOSE_PL_MMOL_PER_L_OF_AT_MOST_X, undeterminedLabValueCreator("Glucose"));

        return map;
    }

    @NotNull
    private FunctionCreator hasSufficientLabValueCreator(@NotNull LabMeasurement measurement) {
        return hasSufficientLabValueCreator(measurement, measurement.defaultUnit());
    }

    @NotNull
    private FunctionCreator hasSufficientLabValueCreator(@NotNull LabMeasurement measurement, @NotNull LabUnit targetUnit) {
        return function -> {
            double minValue = functionInputResolver().createOneDoubleInput(function);
            return createLabEvaluator(measurement, new HasSufficientLabValue(minValue, measurement, targetUnit));
        };
    }

    @NotNull
    private FunctionCreator hasSufficientLabValueLLNCreator(@NotNull LabMeasurement measurement) {
        return function -> {
            double minLLNFactor = functionInputResolver().createOneDoubleInput(function);
            return createLabEvaluator(measurement, new HasSufficientLabValueLLN(minLLNFactor));
        };
    }

    @NotNull
    private FunctionCreator hasLimitedLabValueCreator(@NotNull LabMeasurement measurement) {
        return hasLimitedLabValueCreator(measurement, measurement.defaultUnit());
    }

    @NotNull
    private FunctionCreator hasLimitedLabValueCreator(@NotNull LabMeasurement measurement, @NotNull LabUnit targetUnit) {
        return function -> {
            double maxValue = functionInputResolver().createOneDoubleInput(function);
            return createLabEvaluator(measurement, new HasLimitedLabValue(maxValue, measurement, targetUnit));
        };
    }

    @NotNull
    private FunctionCreator hasLimitedLabValueULNCreator(@NotNull LabMeasurement measurement) {
        return function -> {
            double maxULNFactor = functionInputResolver().createOneDoubleInput(function);
            return createLabEvaluator(measurement, new HasLimitedLabValueULN(maxULNFactor));
        };
    }

    @NotNull
    private FunctionCreator hasSufficientLabValueULNCreator(@NotNull LabMeasurement measurement) {
        return function -> {
            double minULNFactor = functionInputResolver().createOneDoubleInput(function);
            return createLabEvaluator(measurement, new HasSufficientLabValueULN(minULNFactor));
        };
    }

    @NotNull
    private FunctionCreator hasLabValueWithinRefCreator(@NotNull LabMeasurement measurement) {
        return function -> createLabEvaluator(measurement, new HasLabValueWithinRef());
    }

    @NotNull
    private FunctionCreator hasLimitedPTTCreator() {
        return function -> new HasLimitedPTT();
    }

    @NotNull
    private FunctionCreator hasLabValueOutsideRefLimitUpCreator(@NotNull LabMeasurement measurement) {
        return function -> createLabEvaluator(measurement, new HasLabValueOutsideRefLimitUp());
    }

    @NotNull
    private FunctionCreator hasLimitedBilirubinPercentageCreator() {
        return function -> {
            double maxPercentage = functionInputResolver().createOneDoubleInput(function);
            return createLabEvaluator(LabMeasurement.DIRECT_BILIRUBIN,
                    new HasLimitedBilirubinPercentageOfTotal(maxPercentage, minValidLabDate()));
        };
    }

    @NotNull
    private FunctionCreator hasSufficientCreatinineClearanceCreator(@NotNull CreatinineClearanceMethod method) {
        return function -> {
            double minCreatinineClearance = functionInputResolver().createOneDoubleInput(function);
            LabMeasurement measurement = retrieveForMethod(method);
            EvaluationFunction main = createLabEvaluator(measurement,
                    new HasSufficientLabValue(minCreatinineClearance, measurement, measurement.defaultUnit()));

            EvaluationFunction fallback = createLabEvaluator(LabMeasurement.CREATININE,
                    new HasSufficientDerivedCreatinineClearance(referenceDateProvider().year(), method, minCreatinineClearance));

            return new Fallback(main, fallback);
        };
    }

    @NotNull
    private FunctionCreator hasCreatinineClearanceBetweenValuesCreator(@NotNull CreatinineClearanceMethod method) {
        return function -> {
            TwoDoubles inputs = functionInputResolver().createTwoDoublesInput(function);
            LabMeasurement measurement = retrieveForMethod(method);

            EvaluationFunction minFunction = createLabEvaluator(measurement,
                    new HasSufficientDerivedCreatinineClearance(referenceDateProvider().year(), method, inputs.double1()));

            EvaluationFunction maxFunction = createLabEvaluator(measurement,
                    new HasLimitedDerivedCreatinineClearance(referenceDateProvider().year(), method, inputs.double2()));

            return new And(Lists.newArrayList(minFunction, maxFunction));
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
    private FunctionCreator hasPotentialHypokalemiaCreator() {
        return function -> {
            EvaluationFunction potassiumBelowLLN = new Not(createLabEvaluator(LabMeasurement.POTASSIUM, new HasSufficientLabValueLLN(1D)));
            EvaluationFunction hasHadPriorHypokalemia =
                    OtherConditionFunctionFactory.createPriorConditionWithDoidFunction(doidModel(), HYPOKALEMIA_DOID);
            return new Or(Lists.newArrayList(potassiumBelowLLN, hasHadPriorHypokalemia));
        };
    }

    @NotNull
    private FunctionCreator hasPotentialHypomagnesemiaCreator() {
        return function -> {
            EvaluationFunction magnesiumBelowLLN = new Not(createLabEvaluator(LabMeasurement.MAGNESIUM, new HasSufficientLabValueLLN(1D)));
            EvaluationFunction hasHadPriorHypomagnesemia =
                    OtherConditionFunctionFactory.createPriorConditionWithDoidFunction(doidModel(), PRIMARY_HYPOMAGNESEMIA_DOID);
            return new Or(Lists.newArrayList(magnesiumBelowLLN, hasHadPriorHypomagnesemia));
        };
    }

    @NotNull
    private FunctionCreator hasPotentialHypocalcemiaCreator() {
        return function -> {
            EvaluationFunction calciumBelowLLN = new Not(createLabEvaluator(LabMeasurement.CALCIUM, new HasSufficientLabValueLLN(1D)));
            EvaluationFunction hasHadPriorHypocalcemia =
                    OtherConditionFunctionFactory.createPriorConditionWithDoidFunction(doidModel(), AUTOSOMAL_DOMINANT_HYPOCALCEMIA_DOID);
            return new Or(Lists.newArrayList(calciumBelowLLN, hasHadPriorHypocalcemia));
        };
    }

    @NotNull
    private EvaluationFunction createLabEvaluator(@NotNull LabMeasurement measurement, @NotNull LabEvaluationFunction function) {
        return new LabMeasurementEvaluator(measurement, function, minValidLabDate());
    }

    @NotNull
    private LocalDate minValidLabDate() {
        return referenceDateProvider().date().minusDays(MAX_LAB_VALUE_AGE_DAYS);
    }

    @NotNull
    private static FunctionCreator undeterminedLabValueCreator(@NotNull String measure) {
        return function -> record -> EvaluationFactory.recoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("It is not clear yet under what code '" + measure + "' is measured")
                .build();
    }
}
