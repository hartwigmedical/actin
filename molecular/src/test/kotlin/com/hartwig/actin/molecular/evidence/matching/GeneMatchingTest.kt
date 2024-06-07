package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.molecular.datamodel.CodingEffect.MISSENSE
import com.hartwig.actin.molecular.datamodel.CodingEffect.NONE
import com.hartwig.serve.datamodel.gene.GeneAnnotation
import com.hartwig.serve.datamodel.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val MATCHING_GENE = "gene 1"

private val MATCHING_CRITERIA = VARIANT_CRITERIA.copy(gene = MATCHING_GENE, codingEffect = MISSENSE)
private val ANNOTATION = object : GeneAnnotation {
    override fun gene(): String {
        return MATCHING_GENE
    }

    override fun event(): GeneEvent {
        return GeneEvent.ANY_MUTATION
    }
}

class GeneMatchingTest {

    @Test
    fun `Should return true on matching gene`() {
        assertThat(GeneMatching.isMatch(ANNOTATION, MATCHING_CRITERIA)).isTrue()
    }

    @Test
    fun `Should return false on non-matching gene`() {
        assertThat(GeneMatching.isMatch(ANNOTATION, MATCHING_CRITERIA.copy(gene = "gene 2"))).isFalse()
    }

    @Test
    fun `Should return false on non-matching coding effect`() {
        assertThat(GeneMatching.isMatch(ANNOTATION, MATCHING_CRITERIA.copy(codingEffect = NONE))).isFalse()
    }
}