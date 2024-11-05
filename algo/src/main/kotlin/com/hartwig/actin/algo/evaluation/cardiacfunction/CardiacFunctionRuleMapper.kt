package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
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
        )
    }

    private fun hasPotentialSignificantHeartDiseaseCreator(): FunctionCreator {
        return { HasPotentialSignificantHeartDisease(doidModel()) }
    }

    private fun hasECGAberrationCreator(): FunctionCreator {
        return { HasECGAberration() }
    }

    private fun hasSufficientLVEFCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasSufficientLVEF(functionInputResolver().createOneDoubleInput(function))
        }
    }

    private fun hasLimitedQTCFCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            ECGMeasureEvaluationFunctions.hasLimitedQTCF(functionInputResolver().createOneDoubleInput(function))
        }
    }

    private fun hasLimitedQTCFWithGenderCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (maxQTCF, gender) = functionInputResolver().createOneDoubleOneGenderInput(function)
            HasQTCFOfAtMostWithGender(maxQTCF, gender)
        }
    }

    private fun hasSufficientQTCFCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            ECGMeasureEvaluationFunctions.hasSufficientQTCF(functionInputResolver().createOneDoubleInput(function))
        }
    }

    private fun hasSufficientQTCFWithGenderCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (minQTCF, gender) = functionInputResolver().createOneDoubleOneGenderInput(function)
            HasQTCFOfAtLeastWithGender(minQTCF, gender)
        }
    }

    private fun hasSufficientJTcCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            ECGMeasureEvaluationFunctions.hasSufficientJTc(functionInputResolver().createOneDoubleInput(function))
        }
    }

    private fun hasLongQTSyndromeCreator(): FunctionCreator {
        return { HasLongQTSyndrome(doidModel()) }
    }

    private fun hasNormalCardiacFunctionByMUGAOrTTECreator(): FunctionCreator {
        return { HasNormalCardiacFunctionByMUGAOrTTE() }
    }

    private fun hasFamilyHistoryOfIdiopathicSuddenDeathCreator(): FunctionCreator {
        return { HasFamilyHistoryOfIdiopathicSuddenDeath() }
    }

    private fun hasFamilyHistoryOfLongQTSyndromeCreator(): FunctionCreator {
        return { HasFamilyHistoryOfLongQTSyndrome() }
    }
}