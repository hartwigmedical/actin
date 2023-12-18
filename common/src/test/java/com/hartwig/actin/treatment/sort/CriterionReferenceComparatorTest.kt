package com.hartwig.actin.treatment.sort

import com.google.common.collect.Lists
import com.hartwig.actin.treatment.datamodel.ImmutableCriterionReference
import org.apache.logging.log4j.util.Strings
import org.junit.Test

class CriterionReferenceComparatorTest {
    @Test
    fun canSortCriterionReferences() {
        val references: MutableList<CriterionReference> = Lists.newArrayList<CriterionReference>()
        references.add(createReference("E-05"))
        references.add(createReference("I-02"))
        references.add(createReference("I-02"))
        references.add(createReference("Something else"))
        references.add(createReference("AAA"))
        references.add(createReference("I-03"))
        references.add(createReference("E-01"))
        references.sort(CriterionReferenceComparator())
        assertEquals("I-02", references[0].id())
        assertEquals("I-02", references[1].id())
        assertEquals("I-03", references[2].id())
        assertEquals("AAA", references[3].id())
        assertEquals("E-01", references[4].id())
        assertEquals("E-05", references[5].id())
        assertEquals("Something else", references[6].id())
    }

    companion object {
        private fun createReference(id: String): CriterionReference {
            return ImmutableCriterionReference.builder().id(id).text(Strings.EMPTY).build()
        }
    }
}