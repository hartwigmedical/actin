package com.hartwig.actin.molecular.orange.evidence.matching

import com.hartwig.actin.molecular.datamodel.driver.CodingEffect.MISSENSE
import com.hartwig.actin.molecular.datamodel.driver.CodingEffect.NONE
import com.hartwig.actin.molecular.orange.evidence.TestMolecularFactory.minimalTranscriptImpact
import com.hartwig.actin.molecular.orange.evidence.TestMolecularFactory.minimalVariant
import com.hartwig.serve.datamodel.gene.GeneAnnotation
import com.hartwig.serve.datamodel.gene.GeneEvent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneMatchingTest {

    @Test
    fun canMatchGenes() {
        val annotation = createAnnotation("gene 1")

        val match = minimalVariant().copy(gene = "gene 1", canonicalImpact = minimalTranscriptImpact().copy(codingEffect = MISSENSE))
        assertTrue(GeneMatching.isMatch(annotation, match))

        val wrongGene = minimalVariant().copy(gene = "gene 2", canonicalImpact = minimalTranscriptImpact().copy(codingEffect = MISSENSE))
        assertFalse(GeneMatching.isMatch(annotation, wrongGene))

        val nonCoding = minimalVariant().copy(gene = "gene 1", canonicalImpact = minimalTranscriptImpact().copy(codingEffect = NONE))
        assertFalse(GeneMatching.isMatch(annotation, nonCoding))
    }

    companion object {

        private fun createAnnotation(gene: String): GeneAnnotation {
            return object : GeneAnnotation {
                override fun gene(): String {
                    return gene
                }

                override fun event(): GeneEvent {
                    return GeneEvent.ANY_MUTATION
                }
            }
        }
    }
}