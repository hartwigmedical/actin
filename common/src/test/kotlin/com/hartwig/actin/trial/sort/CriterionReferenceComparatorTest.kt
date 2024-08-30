package com.hartwig.actin.trial.sort

import com.hartwig.actin.datamodel.trial.CriterionReference
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CriterionReferenceComparatorTest {

    @Test
    fun `Should sort criterion references`() {
        val references = listOf(
            createReference("E-05"),
            createReference("I-02"),
            createReference("I-02"),
            createReference("Something else"),
            createReference("AAA"),
            createReference("I-03"),
            createReference("E-01")
        ).sortedWith(CriterionReferenceComparator())

        assertThat(references[0].id).isEqualTo("I-02")
        assertThat(references[1].id).isEqualTo("I-02")
        assertThat(references[2].id).isEqualTo("I-03")
        assertThat(references[3].id).isEqualTo("AAA")
        assertThat(references[4].id).isEqualTo("E-01")
        assertThat(references[5].id).isEqualTo("E-05")
        assertThat(references[6].id).isEqualTo("Something else")
    }

    private fun createReference(id: String): CriterionReference {
        return CriterionReference(id, "")
    }
}