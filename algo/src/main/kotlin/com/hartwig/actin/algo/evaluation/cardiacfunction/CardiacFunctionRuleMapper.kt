package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.treatment.datamodel.EligibilityFunction
import com.hartwig.actin.treatment.datamodel.EligibilityRule

class CardiacFunctionRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {
    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_POTENTIAL_SIGNIFICANT_HEART_DISEASE to hasPotentialSignificantHeartDiseaseCreator(),
            EligibilityRule.HAS_CARDIAC_ARRHYTHMIA to hasAnyTypeOfCardiacArrhythmiaCreator(),
            EligibilityRule.HAS_LVEF_OF_AT_LEAST_X to hasSufficientLVEFCreator(false),
            EligibilityRule.HAS_LVEF_OF_AT_LEAST_X_IF_KNOWN to hasSufficientLVEFCreator(true),
            EligibilityRule.HAS_QTC_OF_AT_MOST_X to hasLimitedQTCFCreator(),
            EligibilityRule.HAS_QTCF_OF_AT_MOST_X to hasLimitedQTCFCreator(),
            EligibilityRule.HAS_QTCF_OF_AT_LEAST_X to hasSufficientQTCFCreator(),
            EligibilityRule.HAS_JTC_OF_AT_LEAST_X to hasSufficientJTcCreator(),
            EligibilityRule.HAS_LONG_QT_SYNDROME to hasLongQTSyndromeCreator(),
            EligibilityRule.HAS_NORMAL_CARDIAC_FUNCTION_BY_MUGA_OR_TTE to hasNormalCardiacFunctionByMUGAOrTTECreator(),
            EligibilityRule.HAS_FAMILY_HISTORY_OF_IDIOPATHIC_SUDDEN_DEATH to hasFamilyHistoryOfIdiopathicSuddenDeathCreator(),
            EligibilityRule.HAS_FAMILY_HISTORY_OF_LONG_QT_SYNDROME to hasFamilyHistoryOfLongQTSyndromeCreator(),
        )
    }

    private fun hasPotentialSignificantHeartDiseaseCreator(): FunctionCreator {
        return FunctionCreator { HasPotentialSignificantHeartDisease(doidModel()) }
    }

    private fun hasAnyTypeOfCardiacArrhythmiaCreator(): FunctionCreator {
        return FunctionCreator { HasCardiacArrhythmia() }
    }

    private fun hasSufficientLVEFCreator(passIfUnknown: Boolean): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minLVEF = functionInputResolver().createOneDoubleInput(function)
            HasSufficientLVEF(minLVEF, passIfUnknown)
        }
    }

    private fun hasLimitedQTCFCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            ECGMeasureEvaluationFunctions.hasLimitedQTCF(
                functionInputResolver().createOneDoubleInput(
                    function
                )
            )
        }
    }

    private fun hasSufficientQTCFCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            ECGMeasureEvaluationFunctions.hasSufficientQTCF(
                functionInputResolver().createOneDoubleInput(
                    function
                )
            )
        }
    }

    private fun hasSufficientJTcCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            ECGMeasureEvaluationFunctions.hasSufficientJTc(
                functionInputResolver().createOneDoubleInput(
                    function
                )
            )
        }
    }

    private fun hasLongQTSyndromeCreator(): FunctionCreator {
        return FunctionCreator { HasLongQTSyndrome(doidModel()) }
    }

    private fun hasNormalCardiacFunctionByMUGAOrTTECreator(): FunctionCreator {
        return FunctionCreator { HasNormalCardiacFunctionByMUGAOrTTE() }
    }

    private fun hasFamilyHistoryOfIdiopathicSuddenDeathCreator(): FunctionCreator {
        return FunctionCreator { HasFamilyHistoryOfIdiopathicSuddenDeath() }
    }

    private fun hasFamilyHistoryOfLongQTSyndromeCreator(): FunctionCreator {
        return FunctionCreator { HasFamilyHistoryOfLongQTSyndrome() }
    }
}