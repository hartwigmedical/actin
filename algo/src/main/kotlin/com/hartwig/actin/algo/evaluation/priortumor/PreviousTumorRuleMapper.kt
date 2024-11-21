package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule

class PreviousTumorRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {

    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_ACTIVE_SECOND_MALIGNANCY to hasActiveSecondMalignancyCreator(),
            EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY to hasHistoryOfSecondMalignancyCreator(),
            EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY_IGNORING_DOID_TERMS_X to hasHistoryOfSecondMalignancyIgnoringSomeDoidsCreator(),
            EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY_BELONGING_TO_DOID_TERM_X to hasHistoryOfSecondMalignancyWithDoidTermCreator(),
            EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY_WITHIN_X_YEARS to hasHistoryOfSecondMalignancyWithinYearsCreator(),
            EligibilityRule.HAS_HISTORY_OF_SECOND_MALIGNANCY_WITHIN_X_YEARS_IGNORING_DOID_TERMS_Y
                    to hasHistoryOfSecondMalignancyWithinYearsIgnoringSomeDoidTermsCreator(),
        )
    }

    private fun hasActiveSecondMalignancyCreator(): FunctionCreator {
        return { HasActiveSecondMalignancy() }
    }

    private fun hasHistoryOfSecondMalignancyCreator(): FunctionCreator {
        return { HasHistoryOfSecondMalignancy() }
    }

    private fun hasHistoryOfSecondMalignancyIgnoringSomeDoidsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val doidTermsToIgnore = functionInputResolver().createManyDoidTermsInput(function)
            HasHistoryOfSecondMalignancyIgnoringDoidTerms(doidModel(), doidTermsToIgnore, minDate = null)
        }
    }

    private fun hasHistoryOfSecondMalignancyWithDoidTermCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val doidTermToMatch = functionInputResolver().createOneDoidTermInput(function)
            HasHistoryOfSecondMalignancyWithDoid(doidModel(), doidModel().resolveDoidForTerm(doidTermToMatch)!!)
        }
    }

    private fun hasHistoryOfSecondMalignancyWithinYearsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val maxYears = functionInputResolver().createOneIntegerInput(function)
            val minDate = referenceDateProvider().date().minusYears(maxYears.toLong())
            HasHistoryOfSecondMalignancyWithinYears(minDate)
        }
    }

    private fun hasHistoryOfSecondMalignancyWithinYearsIgnoringSomeDoidTermsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val (maxYears, doidTermsToIgnore) = functionInputResolver().createOneIntegerManyDoidTermsInput(function)
            val minDate = referenceDateProvider().date().minusYears(maxYears.toLong())
            HasHistoryOfSecondMalignancyIgnoringDoidTerms(doidModel(), doidTermsToIgnore, minDate)
        }
    }
}