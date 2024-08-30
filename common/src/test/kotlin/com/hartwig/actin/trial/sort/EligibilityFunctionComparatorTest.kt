package com.hartwig.actin.trial.sort

import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EligibilityFunctionComparatorTest {

    @Test
    fun `Should sort eligibility functions`() {
        val functions = listOf(
            EligibilityFunction(rule = EligibilityRule.IS_AT_LEAST_X_YEARS_OLD, parameters = emptyList()),
            EligibilityFunction(rule = EligibilityRule.IS_AT_LEAST_X_YEARS_OLD, parameters = emptyList()),
            EligibilityFunction(
                rule = EligibilityRule.NOT,
                parameters = listOf(EligibilityFunction(rule = EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS, parameters = emptyList()))
            ),
            EligibilityFunction(
                rule = EligibilityRule.NOT,
                parameters = listOf(EligibilityFunction(rule = EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS, parameters = emptyList()))
            ),
            EligibilityFunction(
                rule = EligibilityRule.AND,
                parameters = listOf(
                    EligibilityFunction(
                        rule = EligibilityRule.NOT,
                        parameters = listOf(
                            EligibilityFunction(rule = EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS, parameters = emptyList())
                        )
                    ),
                    EligibilityFunction(rule = EligibilityRule.HAS_ACTIVE_INFECTION, parameters = emptyList())
                )
            ),
            EligibilityFunction(
                rule = EligibilityRule.AND,
                parameters = listOf(
                    EligibilityFunction(
                        rule = EligibilityRule.NOT,
                        parameters = listOf(
                            EligibilityFunction(rule = EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS, parameters = emptyList())
                        )
                    ),
                    EligibilityFunction(rule = EligibilityRule.HAS_ACTIVE_INFECTION, parameters = emptyList())
                )
            ),
            EligibilityFunction(rule = EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X, parameters = listOf("5")),
            EligibilityFunction(rule = EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X, parameters = listOf("6"))
        ).sortedWith(EligibilityFunctionComparator())

        listOf(
            EligibilityRule.AND,
            EligibilityRule.AND,
            EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X,
            EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X,
            EligibilityRule.IS_AT_LEAST_X_YEARS_OLD,
            EligibilityRule.IS_AT_LEAST_X_YEARS_OLD,
            EligibilityRule.NOT,
            EligibilityRule.NOT
        ).zip(functions).forEach { (expected, actual) ->
            assertThat(actual.rule).isEqualTo(expected)
        }
    }
}