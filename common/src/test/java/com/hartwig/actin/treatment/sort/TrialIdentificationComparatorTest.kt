package com.hartwig.actin.treatment.sort

import com.hartwig.actin.treatment.datamodel.ImmutableTrialIdentification

class TrialIdentificationComparatorTest {
    @org.junit.Test
    fun canSortTrialIdentifications() {
        val identification1: TrialIdentification =
            ImmutableTrialIdentification.builder().trialId("1").open(true).acronym("First").title("Real First").build()
        val identification2: TrialIdentification =
            ImmutableTrialIdentification.builder().trialId("1").open(true).acronym("First").title("Wants to be first").build()
        val identification3: TrialIdentification =
            ImmutableTrialIdentification.builder().trialId("1").open(true).acronym("Second").title("Second").build()
        val identification4: TrialIdentification =
            ImmutableTrialIdentification.builder().trialId("2").open(true).acronym("Third").title("Third").build()
        val identifications: List<TrialIdentification> = com.google.common.collect.Lists.newArrayList<TrialIdentification>(
            identification1,
            identification2,
            identification3,
            identification4
        )
        identifications.sort(TrialIdentificationComparator())
        org.junit.Assert.assertEquals(identification1, identifications[0])
        org.junit.Assert.assertEquals(identification2, identifications[1])
        org.junit.Assert.assertEquals(identification3, identifications[2])
        org.junit.Assert.assertEquals(identification4, identifications[3])
    }
}