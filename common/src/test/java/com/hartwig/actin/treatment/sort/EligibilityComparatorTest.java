package com.hartwig.actin.treatment.sort;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.ImmutableCriterionReference;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibility;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class EligibilityComparatorTest {

    @Test
    public void canSortEligibility() {
        List<Eligibility> eligibilities = Lists.newArrayList();

        eligibilities.add(createWithoutReferences());
        eligibilities.add(createWithReferenceId("Else"));
        eligibilities.add(createWithReferenceId("I-01"));
        eligibilities.add(createWithReferenceId("I-01"));
        eligibilities.add(createWithReferenceId("AAA"));
        eligibilities.add(createWithoutReferences());

        eligibilities.sort(new EligibilityComparator());

        assertEquals("I-01", eligibilities.get(0).references().iterator().next().id());
        assertEquals("I-01", eligibilities.get(1).references().iterator().next().id());
        assertEquals("AAA", eligibilities.get(2).references().iterator().next().id());
        assertEquals("Else", eligibilities.get(3).references().iterator().next().id());
        assertTrue(eligibilities.get(4).references().isEmpty());
        assertTrue(eligibilities.get(5).references().isEmpty());
    }

    @NotNull
    private static Eligibility createWithReferenceId(@NotNull String id) {
        return ImmutableEligibility.builder()
                .from(createWithoutReferences())
                .addReferences(ImmutableCriterionReference.builder().id(id).text(Strings.EMPTY).build())
                .build();
    }

    @NotNull
    private static Eligibility createWithoutReferences() {
        return ImmutableEligibility.builder()
                .function(ImmutableEligibilityFunction.builder().rule(EligibilityRule.IS_AT_LEAST_18_YEARS_OLD).build())
                .build();
    }
}