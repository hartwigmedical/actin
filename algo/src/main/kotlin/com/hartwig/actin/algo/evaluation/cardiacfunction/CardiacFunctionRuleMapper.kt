package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.evaluation.comorbidity.HasHadComorbidityWithIcdCode
import com.hartwig.actin.algo.evaluation.comorbidity.HasSpecificFamilyHistory
import com.hartwig.actin.algo.evaluation.comorbidity.UndeterminedFamilyConditions
import com.hartwig.actin.algo.evaluation.composite.Or
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.clinical.Gender
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.trial.DoubleParameter
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.Parameter
import com.hartwig.actin.trial.input.EligibilityRule

class CardiacFunctionRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {

    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_POTENTIAL_SIGNIFICANT_HEART_DISEASE to hasPotentialSignificantHeartDiseaseCreator(),
            EligibilityRule.HAS_ECG_ABERRATION to hasECGAberrationCreator(),
            EligibilityRule.HAS_LVEF_OF_AT_LEAST_X to hasSufficientLVEFCreator(),
            EligibilityRule.HAS_QTC_OF_AT_MOST_X to hasLimitedQTCFCreator(),
            EligibilityRule.HAS_QTCF_OF_AT_MOST_X to hasLimitedQTCFCreator(),
            EligibilityRule.HAS_QTCF_OF_AT_MOST_X_FOR_FEMALE_OR_Y_FOR_MALE to hasLimitedQTCFWithGenderCreator(),
            EligibilityRule.HAS_QTCF_OF_AT_LEAST_X to hasSufficientQTCFCreator(),
            EligibilityRule.HAS_QTCF_OF_AT_LEAST_X_FOR_FEMALE_OR_Y_FOR_MALE to hasSufficientQTCFWithGenderCreator(),
            EligibilityRule.HAS_JTC_OF_AT_LEAST_X to hasSufficientJTcCreator(),
            EligibilityRule.HAS_LONG_QT_SYNDROME to hasLongQTSyndromeCreator(),
            EligibilityRule.HAS_TORSADES_DE_POINTES to hasTorsadesDePointesCreator(),
            EligibilityRule.HAS_NORMAL_CARDIAC_FUNCTION_BY_MUGA_OR_TTE to hasNormalCardiacFunctionByMUGAOrTTECreator(),
            EligibilityRule.HAS_FAMILY_HISTORY_OF_IDIOPATHIC_SUDDEN_DEATH to hasFamilyHistoryOfIdiopathicSuddenDeathCreator(),
            EligibilityRule.HAS_FAMILY_HISTORY_OF_LONG_QT_SYNDROME to hasFamilyHistoryOfLongQTSyndromeCreator(),
            EligibilityRule.MEETS_REQUIREMENTS_DURING_CARDIAC_STRESS_TEST to { MeetsCardiacStressTestRequirements() },
        )
    }

    private fun hasPotentialSignificantHeartDiseaseCreator(): FunctionCreator {
        return {
            Or(
                listOf(
                    HasEcgAberration(icdModel()),
                    HasHadComorbidityWithIcdCode(
                        icdModel(),
                        IcdConstants.HEART_DISEASE_SET.filterNot { it == IcdConstants.CARDIAC_ARRHYTHMIA_BLOCK }.map { IcdCode(it) }
                            .toSet(),
                        "potential significant heart disease",
                        referenceDateProvider().date()
                    )
                )
            )
        }
    }

    private fun hasECGAberrationCreator(): FunctionCreator {
        return { HasEcgAberration(icdModel()) }
    }

    private fun hasSufficientLVEFCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasSufficientLVEF(function.param<DoubleParameter>(0).value)
        }
    }

    private fun hasLimitedQTCFCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            EcgMeasureEvaluationFunctions.hasLimitedQtcf(function.param<DoubleParameter>(0).value)
        }
    }

    private fun hasLimitedQTCFWithGenderCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.DOUBLE, Parameter.Type.DOUBLE)
            val femaleQTCF = function.param<DoubleParameter>(0).value
            val maleQTCF = function.param<DoubleParameter>(1).value
            Or(
                listOf(
                    HasQtcfWithGender(femaleQTCF, Gender.FEMALE, EcgMeasureEvaluationFunctions::hasLimitedQtcf),
                    HasQtcfWithGender(maleQTCF, Gender.MALE, EcgMeasureEvaluationFunctions::hasLimitedQtcf)
                )
            )
        }
    }

    private fun hasSufficientQTCFCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            EcgMeasureEvaluationFunctions.hasSufficientQtcf(function.param<DoubleParameter>(0).value)
        }
    }

    private fun hasSufficientQTCFWithGenderCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.DOUBLE, Parameter.Type.DOUBLE)
            val femaleQTCF = function.param<DoubleParameter>(0).value
            val maleQTCF = function.param<DoubleParameter>(1).value
            Or(
                listOf(
                    HasQtcfWithGender(femaleQTCF, Gender.FEMALE, EcgMeasureEvaluationFunctions::hasSufficientQtcf),
                    HasQtcfWithGender(maleQTCF, Gender.MALE, EcgMeasureEvaluationFunctions::hasSufficientQtcf)
                )
            )
        }
    }

    private fun hasSufficientJTcCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            EcgMeasureEvaluationFunctions.hasSufficientJTc(function.param<DoubleParameter>(0).value)
        }
    }

    private fun hasLongQTSyndromeCreator(): FunctionCreator {
        return { HasLongQTSyndrome(icdModel()) }
    }

    private fun hasTorsadesDePointesCreator(): FunctionCreator {
        return {
            HasHadComorbidityWithIcdCode(
                icdModel(),
                setOf(IcdCode(IcdConstants.TORSADES_DE_POINTES_CODE)),
                "Torsades de Pointes",
                referenceDateProvider().date()
            )
        }
    }

    private fun hasNormalCardiacFunctionByMUGAOrTTECreator(): FunctionCreator {
        return { HasNormalCardiacFunctionByMugaOrTte() }
    }

    private fun hasFamilyHistoryOfIdiopathicSuddenDeathCreator(): FunctionCreator {
        return {
            HasSpecificFamilyHistory(
                icdModel(),
                "idiopathic sudden death",
                undeterminedFamilyConditions = UndeterminedFamilyConditions(
                    "cardiovascular disease",
                    setOf(IcdCode(IcdConstants.FAMILY_HISTORY_OF_CARDIOVASCULAR_DISEASE_CODE))
                )
            )
        }
    }

    private fun hasFamilyHistoryOfLongQTSyndromeCreator(): FunctionCreator {
        return {
            HasSpecificFamilyHistory(
                icdModel(),
                "long QT syndrome",
                undeterminedFamilyConditions = UndeterminedFamilyConditions(
                    "cardiovascular disease",
                    setOf(IcdCode(IcdConstants.FAMILY_HISTORY_OF_CARDIOVASCULAR_DISEASE_CODE))
                )
            )
        }
    }
}
