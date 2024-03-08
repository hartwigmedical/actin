package com.hartwig.actin.trial.sort

import com.hartwig.actin.trial.datamodel.CriterionReference
import com.hartwig.actin.trial.datamodel.Eligibility
import com.hartwig.actin.trial.datamodel.EligibilityFunction
import com.hartwig.actin.trial.datamodel.EligibilityRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EligibilityComparatorTest {

    @Test
    fun `Should sort eligibilities`() {
        val eligibilities = listOf(
            createWithoutReferences(),
            createWithReferenceId("Else"),
            createWithReferenceId("I-01"),
            createWithReferenceId("I-01"),
            createWithReferenceId("AAA"),
            createWithoutReferences()
        ).sortedWith(EligibilityComparator())

        assertThat(eligibilities[0].references.first().id).isEqualTo("I-01")
        assertThat(eligibilities[1].references.first().id).isEqualTo("I-01")
        assertThat(eligibilities[2].references.first().id).isEqualTo("AAA")
        assertThat(eligibilities[3].references.first().id).isEqualTo("Else")
        assertThat(eligibilities[4].references).isEmpty()
        assertThat(eligibilities[5].references).isEmpty()
    }

    private fun createWithReferenceId(id: String): Eligibility {
        return createWithoutReferences().copy(references = setOf(CriterionReference(id = id, text = "")))
    }

    private fun createWithoutReferences(): Eligibility {
        return Eligibility(
            function = EligibilityFunction(rule = EligibilityRule.IS_AT_LEAST_X_YEARS_OLD, parameters = emptyList()),
            references = emptySet()
        )
    }
}