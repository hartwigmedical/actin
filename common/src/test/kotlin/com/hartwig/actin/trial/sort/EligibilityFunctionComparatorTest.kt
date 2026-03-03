package com.hartwig.actin.trial.sort

import com.hartwig.actin.datamodel.trial.DoubleParameter
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.FunctionParameter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val HAS_EXHAUSTED_SOC_TREATMENTS = "HAS_EXHAUSTED_SOC_TREATMENTS"
private const val HAS_INR_ULN_OF_AT_MOST_X = "HAS_INR_ULN_OF_AT_MOST_X"
private const val IS_AT_LEAST_X_YEARS_OLD = "IS_AT_LEAST_X_YEARS_OLD"
private const val AND = "AND"
private const val NOT = "NOT"

class EligibilityFunctionComparatorTest {

    @Test
    fun `Should sort eligibility functions`() {
        val functions = listOf(
            EligibilityFunction(rule = IS_AT_LEAST_X_YEARS_OLD, parameters = emptyList()),
            EligibilityFunction(rule = IS_AT_LEAST_X_YEARS_OLD, parameters = emptyList()),
            EligibilityFunction(
                rule = NOT,
                parameters = listOf(
                    FunctionParameter(EligibilityFunction(rule = HAS_EXHAUSTED_SOC_TREATMENTS, parameters = emptyList()))
                )
            ),
            EligibilityFunction(
                rule = NOT,
                parameters = listOf(
                    FunctionParameter(EligibilityFunction(rule = HAS_EXHAUSTED_SOC_TREATMENTS, parameters = emptyList()))
                )
            ),
            EligibilityFunction(
                rule = AND,
                parameters = listOf(
                    FunctionParameter(
                        EligibilityFunction(
                            rule = NOT,
                            parameters = listOf(
                                FunctionParameter(EligibilityFunction(rule = HAS_EXHAUSTED_SOC_TREATMENTS, parameters = emptyList()))
                            )
                        )
                    ),
                    FunctionParameter(EligibilityFunction(rule = "HAS_ACTIVE_INFECTION", parameters = emptyList()))
                )
            ),
            EligibilityFunction(
                rule = AND,
                parameters = listOf(
                    FunctionParameter(
                        EligibilityFunction(
                            rule = NOT,
                            parameters = listOf(
                                FunctionParameter(EligibilityFunction(rule = HAS_EXHAUSTED_SOC_TREATMENTS, parameters = emptyList()))
                            )
                        )
                    ),
                    FunctionParameter(EligibilityFunction(rule = "HAS_ACTIVE_INFECTION", parameters = emptyList()))
                )
            ),
            EligibilityFunction(rule = HAS_INR_ULN_OF_AT_MOST_X, parameters = listOf(DoubleParameter(5.0))),
            EligibilityFunction(rule = HAS_INR_ULN_OF_AT_MOST_X, parameters = listOf(DoubleParameter(6.0)))
        ).sortedWith(EligibilityFunctionComparator())

        listOf(
            AND,
            AND,
            HAS_INR_ULN_OF_AT_MOST_X,
            HAS_INR_ULN_OF_AT_MOST_X,
            IS_AT_LEAST_X_YEARS_OLD,
            IS_AT_LEAST_X_YEARS_OLD,
            NOT,
            NOT
        ).zip(functions).forEach { (expected, actual) ->
            assertThat(actual.rule).isEqualTo(expected)
        }
    }
}
