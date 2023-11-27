package com.hartwig.actin.molecular.orange.evidence.matching

import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory
import com.hartwig.hmftools.datamodel.purple.PurpleCodingEffect
import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.serve.datamodel.gene.GeneAnnotation
import com.hartwig.serve.datamodel.gene.GeneEvent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneMatchingTest {

    @Test
    fun canMatchGenes() {
        val annotation = createAnnotation("gene 1")

        val match = createVariant("gene 1", PurpleCodingEffect.MISSENSE)
        assertTrue(GeneMatching.isMatch(annotation, match))

        val wrongGene = createVariant("gene 2", PurpleCodingEffect.MISSENSE)
        assertFalse(GeneMatching.isMatch(annotation, wrongGene))

        val nonCoding = createVariant("gene 1", PurpleCodingEffect.NONE)
        assertFalse(GeneMatching.isMatch(annotation, nonCoding))
    }

    companion object {
        private fun createVariant(gene: String, codingEffect: PurpleCodingEffect): PurpleVariant {
            return TestPurpleFactory.variantBuilder()
                .gene(gene)
                .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder().codingEffect(codingEffect).build())
                .build()
        }

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