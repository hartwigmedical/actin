package com.hartwig.actin.treatment.sort

import com.google.common.collect.Lists
import com.hartwig.actin.treatment.datamodel.Eligibility
import com.hartwig.actin.treatment.datamodel.ImmutableCriterionReference
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test

class EligibilityComparatorTest {
    @Test
    fun canSortEligibility() {
        val eligibilities: MutableList<Eligibility> = Lists.newArrayList()
        eligibilities.add(createWithoutReferences())
        eligibilities.add(createWithReferenceId("Else"))
        eligibilities.add(createWithReferenceId("I-01"))
        eligibilities.add(createWithReferenceId("I-01"))
        eligibilities.add(createWithReferenceId("AAA"))
        eligibilities.add(createWithoutReferences())
        eligibilities.sort(EligibilityComparator())
        assertEquals("I-01", eligibilities[0].references().iterator().next().id())
        assertEquals("I-01", eligibilities[1].references().iterator().next().id())
        assertEquals("AAA", eligibilities[2].references().iterator().next().id())
        assertEquals("Else", eligibilities[3].references().iterator().next().id())
        Assert.assertTrue(eligibilities[4].references().isEmpty())
        Assert.assertTrue(eligibilities[5].references().isEmpty())
    }

    companion object {
        private fun createWithReferenceId(id: String): Eligibility {
            return ImmutableEligibility.builder()
                .from(createWithoutReferences())
                .addReferences(ImmutableCriterionReference.builder().id(id).text(Strings.EMPTY).build())
                .build()
        }

        private fun createWithoutReferences(): Eligibility {
            return ImmutableEligibility.builder()
                .function(ImmutableEligibilityFunction.builder().rule(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD).build())
                .build()
        }
    }
}