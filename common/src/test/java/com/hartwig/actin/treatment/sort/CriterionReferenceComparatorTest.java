package com.hartwig.actin.treatment.sort;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.treatment.datamodel.CriterionReference;
import com.hartwig.actin.treatment.datamodel.ImmutableCriterionReference;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class CriterionReferenceComparatorTest {

    @Test
    public void canSortCriterionReferences() {
        List<CriterionReference> references = Lists.newArrayList();

        references.add(createReference("E-05"));
        references.add(createReference("I-02"));
        references.add(createReference("Something else"));
        references.add(createReference("AAA"));
        references.add(createReference("I-03"));
        references.add(createReference("E-01"));

        references.sort(new CriterionReferenceComparator());

        assertEquals("I-02", references.get(0).id());
        assertEquals("I-03", references.get(1).id());
        assertEquals("AAA", references.get(2).id());
        assertEquals("E-01", references.get(3).id());
        assertEquals("E-05", references.get(4).id());
        assertEquals("Something else", references.get(5).id());
    }

    @NotNull
    private static CriterionReference createReference(@NotNull String id) {
        return ImmutableCriterionReference.builder().id(id).text(Strings.EMPTY).build();
    }
}