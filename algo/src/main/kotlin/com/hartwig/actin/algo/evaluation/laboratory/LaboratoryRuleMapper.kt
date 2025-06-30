package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.evaluation.composite.And
import com.hartwig.actin.algo.evaluation.composite.Fallback
import com.hartwig.actin.algo.evaluation.composite.Not
import com.hartwig.actin.algo.evaluation.composite.Or
import com.hartwig.actin.algo.evaluation.othercondition.HasPotentialSymptomaticHypercalcemia
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionFunctionFactory
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabUnit
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule
import java.time.LocalDate

class LaboratoryRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {

    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_ADEQUATE_ORGAN_FUNCTION to hasAdequateOrganFunctionCreator(),
            EligibilityRule.HAS_ALBI_GRADE_X to hasSpecificAlbiGradeCreator(),
            EligibilityRule.HAS_LEUKOCYTES_ABS_OF_AT_LEAST_X to hasSufficientLabValueCreator(LabMeasurement.LEUKOCYTES_ABS),
            EligibilityRule.HAS_LEUKOCYTES_ABS_LLN_OF_AT_LEAST_X to hasSufficientLabValueLLNCreator(LabMeasurement.LEUKOCYTES_ABS),
            EligibilityRule.HAS_LYMPHOCYTES_ABS_OF_AT_LEAST_X to hasSufficientLabValueCreator(LabMeasurement.LYMPHOCYTES_ABS),
            EligibilityRule.HAS_LYMPHOCYTES_CELLS_PER_MM3_OF_AT_LEAST_X to hasSufficientLabValueCreator(
                LabMeasurement.LYMPHOCYTES_ABS,
                LabUnit.CELLS_PER_CUBIC_MILLIMETER
            ),
            EligibilityRule.HAS_POTENTIAL_LEUKOCYTOSIS to hasPotentialLeukocytosisCreator(),
            EligibilityRule.HAS_NEUTROPHILS_ABS_OF_AT_LEAST_X to hasSufficientLabValueCreator(LabMeasurement.NEUTROPHILS_ABS),
            EligibilityRule.HAS_THROMBOCYTES_ABS_OF_AT_LEAST_X to hasSufficientLabValueCreator(LabMeasurement.THROMBOCYTES_ABS),
            EligibilityRule.HAS_HEMOGLOBIN_G_PER_DL_OF_AT_LEAST_X to hasSufficientLabValueCreator(
                LabMeasurement.HEMOGLOBIN,
                LabUnit.GRAMS_PER_DECILITER
            ),
            EligibilityRule.HAS_HEMOGLOBIN_MMOL_PER_L_OF_AT_LEAST_X to hasSufficientLabValueCreator(
                LabMeasurement.HEMOGLOBIN,
                LabUnit.MILLIMOLES_PER_LITER
            ),
            EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X to hasLimitedLabValueULNCreator(LabMeasurement.INTERNATIONAL_NORMALIZED_RATIO),
            EligibilityRule.HAS_PT_ULN_OF_AT_MOST_X to hasLimitedLabValueULNCreator(LabMeasurement.PROTHROMBIN_TIME),
            EligibilityRule.HAS_PT_WITHIN_INSTITUTIONAL_NORMAL_LIMITS to hasLabValueWithinInstitutionalNormalLimitCreator(LabMeasurement.PROTHROMBIN_TIME),
            EligibilityRule.HAS_APTT_ULN_OF_AT_MOST_X to hasLimitedLabValueULNCreator(LabMeasurement.ACTIVATED_PARTIAL_THROMBOPLASTIN_TIME),
            EligibilityRule.HAS_APTT_WITHIN_INSTITUTIONAL_NORMAL_LIMITS to hasLabValueWithinInstitutionalNormalLimitCreator(LabMeasurement.ACTIVATED_PARTIAL_THROMBOPLASTIN_TIME),
            EligibilityRule.HAS_PTT_ULN_OF_AT_MOST_X to hasLimitedPTTCreator(),
            EligibilityRule.HAS_ALBUMIN_G_PER_DL_OF_AT_LEAST_X to hasSufficientLabValueCreator(
                LabMeasurement.ALBUMIN,
                LabUnit.GRAMS_PER_DECILITER
            ),
            EligibilityRule.HAS_ALBUMIN_LLN_OF_AT_LEAST_X to hasSufficientLabValueLLNCreator(LabMeasurement.ALBUMIN),
            EligibilityRule.HAS_ASAT_ULN_OF_AT_MOST_X to hasLimitedLabValueULNCreator(LabMeasurement.ASPARTATE_AMINOTRANSFERASE),
            EligibilityRule.HAS_ALAT_ULN_OF_AT_MOST_X to hasLimitedLabValueULNCreator(LabMeasurement.ALANINE_AMINOTRANSFERASE),
            EligibilityRule.HAS_ASAT_AND_ALAT_ULN_OF_AT_MOST_X_OR_AT_MOST_Y_WHEN_LIVER_METASTASES_PRESENT to hasLimitedAsatAndAlatDependingOnLiverMetastasesCreator(),
            EligibilityRule.HAS_ALP_ULN_OF_AT_MOST_X to hasLimitedLabValueULNCreator(LabMeasurement.ALKALINE_PHOSPHATASE),
            EligibilityRule.HAS_ALP_ULN_OF_AT_LEAST_X to hasSufficientLabValueULNCreator(LabMeasurement.ALKALINE_PHOSPHATASE),
            EligibilityRule.HAS_TOTAL_BILIRUBIN_ULN_OF_AT_MOST_X to hasLimitedLabValueULNCreator(LabMeasurement.TOTAL_BILIRUBIN),
            EligibilityRule.HAS_TOTAL_BILIRUBIN_ULN_OF_AT_MOST_X_OR_Y_IF_GILBERT_DISEASE to hasLimitedTotalBilirubinULNOrLimitedOtherMeasureULNDependingOnGilbertDiseaseCreator(
                LabMeasurement.TOTAL_BILIRUBIN
            ),
            EligibilityRule.HAS_TOTAL_BILIRUBIN_ULN_OF_AT_MOST_X_OR_DIRECT_BILIRUBIN_ULN_OF_AT_MOST_Y_IF_GILBERT_DISEASE to hasLimitedTotalBilirubinULNOrLimitedOtherMeasureULNDependingOnGilbertDiseaseCreator(
                LabMeasurement.DIRECT_BILIRUBIN
            ),
            EligibilityRule.HAS_TOTAL_BILIRUBIN_UMOL_PER_L_OF_AT_MOST_X to hasLimitedLabValueCreator(LabMeasurement.TOTAL_BILIRUBIN),
            EligibilityRule.HAS_TOTAL_BILIRUBIN_MG_PER_DL_OF_AT_MOST_X to hasLimitedLabValueCreator(
                LabMeasurement.TOTAL_BILIRUBIN,
                LabUnit.MILLIGRAMS_PER_DECILITER
            ),
            EligibilityRule.HAS_DIRECT_BILIRUBIN_ULN_OF_AT_MOST_X to hasLimitedLabValueULNCreator(LabMeasurement.DIRECT_BILIRUBIN),
            EligibilityRule.HAS_DIRECT_BILIRUBIN_PERCENTAGE_OF_TOTAL_OF_AT_MOST_X to hasLimitedBilirubinPercentageCreator(),
            EligibilityRule.HAS_INDIRECT_BILIRUBIN_ULN_OF_AT_MOST_X to hasLimitedIndirectBilirubinCreator(),
            EligibilityRule.HAS_CREATININE_MG_PER_DL_OF_AT_MOST_X to hasLimitedLabValueCreator(
                LabMeasurement.CREATININE,
                LabUnit.MILLIGRAMS_PER_DECILITER
            ),
            EligibilityRule.HAS_CREATININE_ULN_OF_AT_MOST_X to hasLimitedLabValueULNCreator(LabMeasurement.CREATININE),
            EligibilityRule.HAS_EGFR_CKD_EPI_OF_AT_LEAST_X to hasSufficientCreatinineClearanceCreator(CreatinineClearanceMethod.EGFR_CKD_EPI),
            EligibilityRule.HAS_EGFR_MDRD_OF_AT_LEAST_X to hasSufficientCreatinineClearanceCreator(CreatinineClearanceMethod.EGFR_MDRD),
            EligibilityRule.HAS_CREATININE_CLEARANCE_CG_OF_AT_LEAST_X to hasSufficientCreatinineClearanceCreator(CreatinineClearanceMethod.COCKCROFT_GAULT),
            EligibilityRule.HAS_CREATININE_CLEARANCE_BETWEEN_X_AND_Y to hasCreatinineClearanceBetweenValuesCreator(CreatinineClearanceMethod.COCKCROFT_GAULT),
            EligibilityRule.HAS_MEASURED_CREATININE_CLEARANCE_OF_AT_LEAST_X to hasSufficientMeasuredCreatinineClearanceCreator(),
            EligibilityRule.HAS_BNP_ULN_OF_AT_MOST_X to hasLimitedLabValueULNCreator(LabMeasurement.NT_PRO_BNP),
            EligibilityRule.HAS_TROPONIN_I_OR_T_ULN_OF_AT_MOST_X to hasLimitedLabValueULNCreator(LabMeasurement.HIGH_SENSITIVITY_TROPONIN_T),
            EligibilityRule.HAS_TRIGLYCERIDE_MMOL_PER_L_OF_AT_MOST_X to hasLimitedLabValueCreator(LabMeasurement.TRIGLYCERIDE),
            EligibilityRule.HAS_AMYLASE_ULN_OF_AT_MOST_X to hasLimitedLabValueULNCreator(LabMeasurement.AMYLASE),
            EligibilityRule.HAS_LIPASE_ULN_OF_AT_MOST_X to hasLimitedLabValueULNCreator(LabMeasurement.LIPASE),
            EligibilityRule.HAS_ABNORMAL_ELECTROLYTE_LEVELS to { HasAbnormalElectrolyteLevels(minValidLabDate(), minPassLabDate()) },
            EligibilityRule.HAS_CALCIUM_MG_PER_DL_OF_AT_MOST_X to hasLimitedLabValueCreator(
                LabMeasurement.CALCIUM,
                LabUnit.MILLIGRAMS_PER_DECILITER
            ),
            EligibilityRule.HAS_IONIZED_CALCIUM_MMOL_PER_L_OF_AT_MOST_X to hasLimitedLabValueCreator(LabMeasurement.IONIZED_CALCIUM),
            EligibilityRule.HAS_CORRECTED_CALCIUM_ULN_OF_AT_MOST_X to hasLimitedLabValueULNCreator(LabMeasurement.CORRECTED_CALCIUM),
            EligibilityRule.HAS_CALCIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS to hasLabValueWithinInstitutionalNormalLimitCreator(
                LabMeasurement.CALCIUM
            ),
            EligibilityRule.HAS_CORRECTED_CALCIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS to hasLabValueWithinInstitutionalNormalLimitCreator(
                LabMeasurement.CORRECTED_CALCIUM
            ),
            EligibilityRule.HAS_MAGNESIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS to hasLabValueWithinInstitutionalNormalLimitCreator(
                LabMeasurement.MAGNESIUM
            ),
            EligibilityRule.HAS_CORRECTED_MAGNESIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS to undeterminedLabValueCreator("corrected magnesium"),
            EligibilityRule.HAS_PHOSPHATE_MMOL_PER_L_OF_AT_MOST_X to hasLimitedLabValueCreator(LabMeasurement.PHOSPHATE),
            EligibilityRule.HAS_PHOSPHATE_ULN_OF_AT_MOST_X to hasLimitedLabValueULNCreator(LabMeasurement.PHOSPHATE),
            EligibilityRule.HAS_PHOSPHATE_WITHIN_INSTITUTIONAL_NORMAL_LIMITS to hasLabValueWithinInstitutionalNormalLimitCreator(
                LabMeasurement.PHOSPHATE
            ),
            EligibilityRule.HAS_POTASSIUM_MMOL_PER_L_OF_AT_LEAST_X to hasSufficientLabValueCreator(LabMeasurement.POTASSIUM),
            EligibilityRule.HAS_POTASSIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS to hasLabValueWithinInstitutionalNormalLimitCreator(
                LabMeasurement.POTASSIUM
            ),
            EligibilityRule.HAS_CORRECTED_POTASSIUM_WITHIN_INSTITUTIONAL_NORMAL_LIMITS to undeterminedLabValueCreator("corrected potassium"),
            EligibilityRule.HAS_POTENTIAL_HYPOKALEMIA to hasPotentialHypokalemiaCreator(),
            EligibilityRule.HAS_POTENTIAL_HYPOMAGNESEMIA to hasPotentialHypomagnesemiaCreator(),
            EligibilityRule.HAS_POTENTIAL_HYPOCALCEMIA to hasPotentialHypocalcemiaCreator(),
            EligibilityRule.HAS_POTENTIAL_SYMPTOMATIC_HYPERCALCEMIA to hasPotentialSymptomaticHypercalcemiaCreator(),
            EligibilityRule.HAS_SERUM_TESTOSTERONE_NG_PER_DL_OF_AT_MOST_X to hasLimitedLabValueCreator(
                LabMeasurement.TESTOSTERONE,
                LabUnit.NANOGRAMS_PER_DECILITER
            ),
            EligibilityRule.HAS_CORTISOL_LLN_OF_AT_LEAST_X to hasSufficientLabValueLLNCreator(LabMeasurement.CORTISOL),
            EligibilityRule.HAS_AFP_ULN_OF_AT_LEAST_X to hasSufficientLabValueCreator(LabMeasurement.ALPHA_FETOPROTEIN),
            EligibilityRule.HAS_CA125_ULN_OF_AT_LEAST_X to hasSufficientLabValueCreator(LabMeasurement.CARBOHYDRATE_ANTIGEN_125),
            EligibilityRule.HAS_HCG_ULN_OF_AT_LEAST_X to hasSufficientLabValueCreator(LabMeasurement.HUMAN_CHORIONIC_GONADOTROPIN),
            EligibilityRule.HAS_LDH_ULN_OF_AT_MOST_X to hasLimitedLabValueULNCreator(LabMeasurement.LACTATE_DEHYDROGENASE),
            EligibilityRule.HAS_PSA_UG_PER_L_OF_AT_LEAST_X to hasSufficientLabValueCreator(LabMeasurement.PROSTATE_SPECIFIC_ANTIGEN),
            EligibilityRule.HAS_PSA_LLN_OF_AT_LEAST_X to hasSufficientLabValueLLNCreator(LabMeasurement.PROSTATE_SPECIFIC_ANTIGEN),
            EligibilityRule.HAS_TOTAL_PROTEIN_G_PER_L_IN_URINE_OF_AT_LEAST_X to hasSufficientLabValueCreator(LabMeasurement.TOTAL_PROTEIN_URINE),
            EligibilityRule.HAS_TOTAL_PROTEIN_G_IN_24H_URINE_OF_AT_LEAST_X to hasSufficientLabValueCreator(LabMeasurement.TOTAL_PROTEIN_24U),
            EligibilityRule.HAS_GLUCOSE_FASTING_PLASMA_MMOL_PER_L_OF_AT_MOST_X to undeterminedLabValueCreator("fasting plasma glucose"),
            EligibilityRule.HAS_FREE_THYROXINE_WITHIN_INSTITUTIONAL_NORMAL_LIMITS to hasLabValueWithinInstitutionalNormalLimitCreator(
                LabMeasurement.FREE_THYROXINE
            ),
            EligibilityRule.HAS_FREE_TRIIODOTHYRONINE_WITHIN_INSTITUTIONAL_NORMAL_LIMITS to hasLabValueWithinInstitutionalNormalLimitCreator(
                LabMeasurement.FREE_TRIIODOTHYRONINE
            ),
            EligibilityRule.HAS_BOUND_TRIIODOTHYRONINE_WITHIN_INSTITUTIONAL_NORMAL_LIMITS to hasLabValueWithinInstitutionalNormalLimitCreator(
                LabMeasurement.BOUND_TRIIODOTHYRONINE
            ),
            EligibilityRule.HAS_TSH_WITHIN_INSTITUTIONAL_NORMAL_LIMITS to hasLabValueWithinInstitutionalNormalLimitCreator(
                LabMeasurement.THYROID_STIMULATING_HORMONE
            ),
            EligibilityRule.HAS_ANTI_HLA_ANTIBODIES_AGAINST_PDC_LINE to undeterminedLabValueCreator("HLA-antibodies against PDC line"),
            EligibilityRule.HAS_CD4_POSITIVE_CELLS_MILLIONS_PER_LITER_OF_AT_LEAST_X to hasSufficientLabValueCreator(
                LabMeasurement.CD4_POSITIVE_CELLS_ABS,
                LabUnit.MILLIONS_PER_LITER
            )
        )
    }

    private fun hasAdequateOrganFunctionCreator(): FunctionCreator {
        return { HasAdequateOrganFunction(minValidLabDate(), icdModel()) }
    }

    private fun hasSpecificAlbiGradeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val albiGrade = functionInputResolver().createOneAlbiGradeInput(function)
            HasSpecificAlbiGrade(albiGrade, minValidLabDate(), minPassLabDate())
        }
    }

    private fun hasLimitedPTTCreator(): FunctionCreator {
        return { HasLimitedPTT() }
    }

    private fun hasSufficientLabValueCreator(
        measurement: LabMeasurement,
        targetUnit: LabUnit = measurement.defaultUnit
    ): FunctionCreator {
        return { function: EligibilityFunction ->
            val minValue = functionInputResolver().createOneDoubleInput(function)
            createLabEvaluator(measurement, HasSufficientLabValue(minValue, measurement, targetUnit), false)
        }
    }

    private fun hasSufficientLabValueLLNCreator(measurement: LabMeasurement): FunctionCreator {
        return { function: EligibilityFunction ->
            val minLLNFactor = functionInputResolver().createOneDoubleInput(function)
            createLabEvaluator(measurement, HasSufficientLabValueLLN(minLLNFactor), false)
        }
    }

    private fun hasLimitedLabValueCreator(measurement: LabMeasurement, targetUnit: LabUnit = measurement.defaultUnit): FunctionCreator {
        return { function: EligibilityFunction ->
            val maxValue = functionInputResolver().createOneDoubleInput(function)
            createLabEvaluator(measurement, HasLimitedLabValue(maxValue, measurement, targetUnit))
        }
    }

    private fun hasLimitedLabValueULNCreator(measurement: LabMeasurement): FunctionCreator {
        return { function: EligibilityFunction ->
            val maxULNFactor = functionInputResolver().createOneDoubleInput(function)
            createLabEvaluator(measurement, HasLimitedLabValueULN(maxULNFactor))
        }
    }

    private fun hasSufficientLabValueULNCreator(measurement: LabMeasurement): FunctionCreator {
        return { function: EligibilityFunction ->
            val minULNFactor = functionInputResolver().createOneDoubleInput(function)
            createLabEvaluator(measurement, HasSufficientLabValueULN(minULNFactor))
        }
    }

    private fun hasLabValueWithinInstitutionalNormalLimitCreator(measurement: LabMeasurement): FunctionCreator {
        return { createLabEvaluator(measurement, HasLabValueWithinInstitutionalNormalLimit()) }
    }

    private fun hasLimitedAsatAndAlatDependingOnLiverMetastasesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (maxULNWithoutLiverMetastases, maxULNWithLiverMetastases) = functionInputResolver().createTwoDoublesInput(function)
            HasLimitedAsatAndAlatDependingOnLiverMetastases(
                maxULNWithoutLiverMetastases, maxULNWithLiverMetastases, minValidLabDate(), minPassLabDate()
            )
        }
    }

    private fun hasLimitedTotalBilirubinULNOrLimitedOtherMeasureULNDependingOnGilbertDiseaseCreator(
        labMeasureWithGilbertDisease: LabMeasurement
    ): FunctionCreator {
        return { function: EligibilityFunction ->
            val (maxUlnWithoutGilbertDisease, maxUlnWithGilbertDisease) = functionInputResolver().createTwoDoublesInput(function)
            HasLimitedTotalBilirubinULNOrLimitedOtherMeasureULNDependingOnGilbertDisease(
                maxUlnWithoutGilbertDisease,
                labMeasureWithGilbertDisease,
                maxUlnWithGilbertDisease,
                minValidLabDate(),
                minPassLabDate(),
                icdModel()
            )
        }
    }

    private fun hasLimitedBilirubinPercentageCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val maxPercentage = functionInputResolver().createOneDoubleInput(function)
            createLabEvaluator(LabMeasurement.DIRECT_BILIRUBIN, HasLimitedBilirubinPercentageOfTotal(maxPercentage, minValidLabDate()))
        }
    }

    private fun hasLimitedIndirectBilirubinCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val maxValue = functionInputResolver().createOneDoubleInput(function)
            createLabEvaluator(LabMeasurement.DIRECT_BILIRUBIN, HasLimitedIndirectBilirubinULN(maxValue, minValidLabDate()))
        }
    }

    private fun hasSufficientCreatinineClearanceCreator(method: CreatinineClearanceMethod): FunctionCreator {
        return { function: EligibilityFunction ->
            val minCreatinineClearance = functionInputResolver().createOneDoubleInput(function)
            val measurement = retrieveForMethod(method)
            val minimalDateWeightMeasurements = referenceDateProvider().date().minusMonths(BODY_WEIGHT_MAX_AGE_MONTHS.toLong())
            val main =
                createLabEvaluator(measurement, HasSufficientLabValue(minCreatinineClearance, measurement, measurement.defaultUnit), false)

            val fallback = createLabEvaluator(
                LabMeasurement.CREATININE,
                HasSufficientDerivedCreatinineClearance(
                    referenceDateProvider().year(), method, minCreatinineClearance, minimalDateWeightMeasurements
                ),
                false
            )
            Fallback(main, fallback)
        }
    }

    private fun hasCreatinineClearanceBetweenValuesCreator(method: CreatinineClearanceMethod): FunctionCreator {
        return { function: EligibilityFunction ->
            val inputs = functionInputResolver().createTwoDoublesInput(function)
            val measurement = retrieveForMethod(method)
            val mininumDateForBodyWeights = referenceDateProvider().date().minusMonths(BODY_WEIGHT_MAX_AGE_MONTHS.toLong())
            val minFunction = createLabEvaluator(
                measurement,
                HasSufficientDerivedCreatinineClearance(referenceDateProvider().year(), method, inputs.double1, mininumDateForBodyWeights),
                false
            )
            val maxFunction = createLabEvaluator(
                measurement,
                HasLimitedDerivedCreatinineClearance(referenceDateProvider().year(), method, inputs.double2, mininumDateForBodyWeights)
            )
            And(listOf(minFunction, maxFunction))
        }
    }

    private fun hasSufficientMeasuredCreatinineClearanceCreator(): FunctionCreator {
        return { HasSufficientMeasuredCreatinineClearance() }
    }

    private fun hasPotentialLeukocytosisCreator(): FunctionCreator {
        return { createLabEvaluator(LabMeasurement.LEUKOCYTES_ABS, HasSufficientLabValueULN(1.0)) }
    }

    private fun hasPotentialHypokalemiaCreator(): FunctionCreator {
        return {
            val potassiumBelowLLN: EvaluationFunction =
                Not(createLabEvaluator(LabMeasurement.POTASSIUM, HasSufficientLabValueLLN(1.0), false))
            val hasHadPriorHypokalemia =
                OtherConditionFunctionFactory.createPriorConditionWithIcdCodeFunction(
                    icdModel(),
                    setOf(IcdCode(IcdConstants.HYPOKALEMIA_CODE)),
                    "potential hypokalemia"
                )
            Or(listOf(potassiumBelowLLN, hasHadPriorHypokalemia))
        }
    }

    private fun hasPotentialHypomagnesemiaCreator(): FunctionCreator {
        return {
            val magnesiumBelowLLN: EvaluationFunction =
                Not(createLabEvaluator(LabMeasurement.MAGNESIUM, HasSufficientLabValueLLN(1.0), false))
            val hasHadPriorHypomagnesemia = OtherConditionFunctionFactory.createPriorConditionWithIcdCodeFunction(
                icdModel(),
                setOf(IcdCode(IcdConstants.HYPOMAGNESEMIA_CODE)),
                "potential hypomagnesemia"
            )
            Or(listOf(magnesiumBelowLLN, hasHadPriorHypomagnesemia))
        }
    }

    private fun hasPotentialHypocalcemiaCreator(): FunctionCreator {
        return {
            val calciumBelowLLN: EvaluationFunction = Not(createLabEvaluator(LabMeasurement.CALCIUM, HasSufficientLabValueLLN(1.0), false))
            val hasHadPriorHypocalcemia = OtherConditionFunctionFactory.createPriorConditionWithIcdCodeFunction(
                icdModel(),
                setOf(IcdCode(IcdConstants.CALCIUM_DEFICIENCY_CODE)),
                "potential hypocalcemia"
            )
            Or(listOf(calciumBelowLLN, hasHadPriorHypocalcemia))
        }
    }

    private fun hasPotentialSymptomaticHypercalcemiaCreator(): FunctionCreator {
        return { HasPotentialSymptomaticHypercalcemia(minValidLabDate()) }
    }

    private fun createLabEvaluator(
        measurement: LabMeasurement,
        function: LabEvaluationFunction,
        highestFirst: Boolean = true
    ): EvaluationFunction {
        return LabMeasurementEvaluator(measurement, function, minValidLabDate(), minPassLabDate(), highestFirst)
    }

    private fun minValidLabDate(): LocalDate {
        return referenceDateProvider().date().minusDays(MAX_LAB_VALUE_AGE_DAYS_FOR_VALIDITY.toLong())
    }

    private fun minPassLabDate(): LocalDate {
        return referenceDateProvider().date().minusDays(MAX_LAB_VALUE_AGE_DAYS_FOR_PASS.toLong())
    }

    companion object {
        private const val MAX_LAB_VALUE_AGE_DAYS_FOR_VALIDITY = 90
        private const val MAX_LAB_VALUE_AGE_DAYS_FOR_PASS = 30
        private const val BODY_WEIGHT_MAX_AGE_MONTHS = 1
        private fun retrieveForMethod(method: CreatinineClearanceMethod): LabMeasurement {
            return when (method) {
                CreatinineClearanceMethod.EGFR_MDRD -> LabMeasurement.EGFR_MDRD
                CreatinineClearanceMethod.EGFR_CKD_EPI -> LabMeasurement.EGFR_CKD_EPI
                CreatinineClearanceMethod.COCKCROFT_GAULT -> LabMeasurement.CREATININE_CLEARANCE_CG
            }
        }

        private fun undeterminedLabValueCreator(measure: String): FunctionCreator {
            return {
                object : EvaluationFunction {
                    override fun evaluate(record: PatientRecord): Evaluation {
                        return EvaluationFactory.recoverableUndetermined(
                            "Lab measure '$measure' undetermined"
                        )
                    }
                }
            }
        }
    }
}