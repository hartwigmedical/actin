package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.evaluation.composite.Or
import com.hartwig.actin.algo.evaluation.othercondition.HasHadOtherConditionComplicationOrToxicityWithIcdCode
import com.hartwig.actin.algo.evaluation.othercondition.HasSpecificFamilyHistory
import com.hartwig.actin.algo.evaluation.othercondition.UndeterminedFamilyConditions
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule

class CardiacFunctionRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {

    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_POTENTIAL_SIGNIFICANT_HEART_DISEASE to hasPotentialSignificantHeartDiseaseCreator(),
            EligibilityRule.HAS_ECG_ABERRATION to hasECGAberrationCreator(),
            EligibilityRule.HAS_LVEF_OF_AT_LEAST_X to hasSufficientLVEFCreator(),
            EligibilityRule.HAS_QTC_OF_AT_MOST_X to hasLimitedQTCFCreator(),
            EligibilityRule.HAS_QTCF_OF_AT_MOST_X to hasLimitedQTCFCreator(),
            EligibilityRule.HAS_QTCF_OF_AT_MOST_X_WITH_GENDER_Y to hasLimitedQTCFWithGenderCreator(),
            EligibilityRule.HAS_QTCF_OF_AT_LEAST_X to hasSufficientQTCFCreator(),
            EligibilityRule.HAS_QTCF_OF_AT_LEAST_X_WITH_GENDER_Y to hasSufficientQTCFWithGenderCreator(),
            EligibilityRule.HAS_JTC_OF_AT_LEAST_X to hasSufficientJTcCreator(),
            EligibilityRule.HAS_LONG_QT_SYNDROME to hasLongQTSyndromeCreator(),
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
                    HasEcgAberration(),
                    HasHadOtherConditionComplicationOrToxicityWithIcdCode(
                        icdModel(),
                        IcdConstants.HEART_DISEASE_SET.map { IcdCode(it) }.toSet(),
                        "potential significant heart disease",
                        referenceDateProvider().date()
                    )
                )
            )
        }
    }

    private fun hasECGAberrationCreator(): FunctionCreator {
        return { HasEcgAberration() }
    }

    private fun hasSufficientLVEFCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasSufficientLVEF(functionInputResolver().createOneDoubleInput(function))
        }
    }

    private fun hasLimitedQTCFCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            EcgMeasureEvaluationFunctions.hasLimitedQtcf(functionInputResolver().createOneDoubleInput(function))
        }
    }

    private fun hasLimitedQTCFWithGenderCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (maxQTCF, gender) = functionInputResolver().createOneDoubleOneGenderInput(function)
            HasQtcfWithGender(maxQTCF, gender, EcgMeasureEvaluationFunctions::hasLimitedQtcf)
        }
    }

    private fun hasSufficientQTCFCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            EcgMeasureEvaluationFunctions.hasSufficientQtcf(functionInputResolver().createOneDoubleInput(function))
        }
    }

    private fun hasSufficientQTCFWithGenderCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (minQTCF, gender) = functionInputResolver().createOneDoubleOneGenderInput(function)
            HasQtcfWithGender(minQTCF, gender, EcgMeasureEvaluationFunctions::hasSufficientQtcf)
        }
    }

    private fun hasSufficientJTcCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            EcgMeasureEvaluationFunctions.hasSufficientJTc(functionInputResolver().createOneDoubleInput(function))
        }
    }

    private fun hasLongQTSyndromeCreator(): FunctionCreator {
        return { HasLongQTSyndrome(icdModel()) }
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