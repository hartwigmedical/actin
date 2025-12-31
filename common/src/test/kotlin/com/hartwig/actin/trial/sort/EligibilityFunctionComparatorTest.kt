package com.hartwig.actin.trial.sort

import com.hartwig.actin.datamodel.trial.EligibilityFunction
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EligibilityFunctionComparatorTest {

    @Test
    fun `Should sort eligibility functions`() {
        val functions = listOf(
            EligibilityFunction(rule = "IS_AT_LEAST_X_YEARS_OLD", parameters = emptyList()),
            EligibilityFunction(rule = "IS_AT_LEAST_X_YEARS_OLD", parameters = emptyList()),
            EligibilityFunction(
                rule = "NOT",
                parameters = listOf(EligibilityFunction(rule = "HAS_EXHAUSTED_SOC_TREATMENTS", parameters = emptyList()))
            ),
            EligibilityFunction(
                rule = "NOT",
                parameters = listOf(EligibilityFunction(rule = "HAS_EXHAUSTED_SOC_TREATMENTS", parameters = emptyList()))
            ),
            EligibilityFunction(
                rule = "AND",
                parameters = listOf(
                    EligibilityFunction(
                        rule = "NOT",
                        parameters = listOf(
                            EligibilityFunction(rule = "HAS_EXHAUSTED_SOC_TREATMENTS", parameters = emptyList())
                        )
                    ),
                    EligibilityFunction(rule = "HAS_ACTIVE_INFECTION", parameters = emptyList())
                )
            ),
            EligibilityFunction(
                rule = "AND",
                parameters = listOf(
                    EligibilityFunction(
                        rule = "NOT",
                        parameters = listOf(
                            EligibilityFunction(rule = "HAS_EXHAUSTED_SOC_TREATMENTS", parameters = emptyList())
                        )
                    ),
                    EligibilityFunction(rule = "HAS_ACTIVE_INFECTION", parameters = emptyList())
                )
            ),
            EligibilityFunction(rule = "HAS_INR_ULN_OF_AT_MOST_X", parameters = listOf("5")),
            EligibilityFunction(rule = "HAS_INR_ULN_OF_AT_MOST_X", parameters = listOf("6"))
        ).sortedWith(EligibilityFunctionComparator())

        listOf(
            "AND",
            "AND",
            "HAS_INR_ULN_OF_AT_MOST_X",
            "HAS_INR_ULN_OF_AT_MOST_X",
            "IS_AT_LEAST_X_YEARS_OLD",
            "IS_AT_LEAST_X_YEARS_OLD",
            "NOT",
            "NOT"
        ).zip(functions).forEach { (expected, actual) ->
            assertThat(actual.rule).isEqualTo(expected)
        }
    }
}