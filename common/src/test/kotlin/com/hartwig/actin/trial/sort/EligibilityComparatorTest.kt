package com.hartwig.actin.trial.sort

import com.hartwig.actin.datamodel.trial.Eligibility
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.trial.input.EligibilityRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EligibilityComparatorTest {

    @Test
    fun `Should sort eligibilities`() {
        val eligibilities = listOf(
            createWithoutReferences(),
            createWithReference("Else"),
            createWithReference("I-01"),
            createWithReference("I-01"),
            createWithReference("AAA"),
            createWithoutReferences()
        ).sortedWith(EligibilityComparator())

        assertThat(eligibilities[0].references.first()).isEqualTo("I-01")
        assertThat(eligibilities[1].references.first()).isEqualTo("I-01")
        assertThat(eligibilities[2].references.first()).isEqualTo("AAA")
        assertThat(eligibilities[3].references.first()).isEqualTo("Else")
        assertThat(eligibilities[4].references).isEmpty()
        assertThat(eligibilities[5].references).isEmpty()
    }

    private fun createWithReference(reference: String): Eligibility {
        return createWithoutReferences().copy(references = setOf(reference))
    }

    private fun createWithoutReferences(): Eligibility {
        return Eligibility(
            function = EligibilityFunction(rule = EligibilityRule.IS_AT_LEAST_X_YEARS_OLD.name, parameters = emptyList()),
            references = emptySet()
        )
    }
}