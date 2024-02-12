package com.hartwig.actin.molecular.orange.evidence.matching

import com.hartwig.actin.molecular.datamodel.driver.CodingEffect
import com.hartwig.actin.molecular.datamodel.driver.VariantType
import com.hartwig.actin.molecular.orange.evidence.TestMolecularFactory.minimalTranscriptImpact
import com.hartwig.actin.molecular.orange.evidence.TestMolecularFactory.minimalVariant
import com.hartwig.serve.datamodel.MutationType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MutationTypeMatchingTest {

    @Test
    fun worksForEveryCodingEffect() {
        val nonCoding = minimalVariant().copy(canonicalImpact = minimalTranscriptImpact().copy(codingEffect = CodingEffect.NONE))
        for (type in MutationType.values()) {
            assertFalse(MutationTypeMatching.matches(type, nonCoding))
        }
    }

    @Test
    fun canMatchMutationTypes() {
        val nonsenseOrFrameshift = minimalVariant().copy(canonicalImpact = minimalTranscriptImpact().copy(codingEffect = CodingEffect.NONSENSE_OR_FRAMESHIFT))
        assertTrue(MutationTypeMatching.matches(MutationType.NONSENSE_OR_FRAMESHIFT, nonsenseOrFrameshift))
        assertTrue(MutationTypeMatching.matches(MutationType.ANY, nonsenseOrFrameshift))

        val splice = minimalVariant().copy(canonicalImpact = minimalTranscriptImpact().copy(codingEffect = CodingEffect.SPLICE))
        assertTrue(MutationTypeMatching.matches(MutationType.SPLICE, splice))
        assertTrue(MutationTypeMatching.matches(MutationType.ANY, splice))

        val inframe = minimalVariant().copy(canonicalImpact = minimalTranscriptImpact().copy(codingEffect = CodingEffect.MISSENSE), type = VariantType.MNV, ref = "AAG", alt = "TTG")
        assertTrue(MutationTypeMatching.matches(MutationType.MISSENSE, inframe))
        assertTrue(MutationTypeMatching.matches(MutationType.INFRAME, inframe))
        assertFalse(MutationTypeMatching.matches(MutationType.INFRAME_DELETION, inframe))
        assertFalse(MutationTypeMatching.matches(MutationType.INFRAME_INSERTION, inframe))
        assertTrue(MutationTypeMatching.matches(MutationType.ANY, splice))

        val inframeDeletion = minimalVariant().copy(canonicalImpact = minimalTranscriptImpact().copy(codingEffect = CodingEffect.MISSENSE), type = VariantType.DELETE, ref = "ATGATG", alt = "TTT")
        assertTrue(MutationTypeMatching.matches(MutationType.MISSENSE, inframeDeletion))
        assertTrue(MutationTypeMatching.matches(MutationType.INFRAME, inframeDeletion))
        assertTrue(MutationTypeMatching.matches(MutationType.INFRAME_DELETION, inframeDeletion))
        assertFalse(MutationTypeMatching.matches(MutationType.INFRAME_INSERTION, inframeDeletion))
        assertTrue(MutationTypeMatching.matches(MutationType.ANY, inframeDeletion))

        val inframeInsertion = minimalVariant().copy(canonicalImpact = minimalTranscriptImpact().copy(codingEffect = CodingEffect.MISSENSE), type = VariantType.INSERT, ref = "TTT", alt = "ATGATG")
        assertTrue(MutationTypeMatching.matches(MutationType.MISSENSE, inframeInsertion))
        assertTrue(MutationTypeMatching.matches(MutationType.INFRAME, inframeInsertion))
        assertFalse(MutationTypeMatching.matches(MutationType.INFRAME_DELETION, inframeInsertion))
        assertTrue(MutationTypeMatching.matches(MutationType.INFRAME_INSERTION, inframeInsertion))
        assertTrue(MutationTypeMatching.matches(MutationType.ANY, inframeInsertion))

        val missense = minimalVariant().copy(canonicalImpact = minimalTranscriptImpact().copy(codingEffect = CodingEffect.MISSENSE), type = VariantType.SNV)
        assertTrue(MutationTypeMatching.matches(MutationType.MISSENSE, missense))
        assertFalse(MutationTypeMatching.matches(MutationType.INFRAME, missense))
        assertFalse(MutationTypeMatching.matches(MutationType.INFRAME_DELETION, missense))
        assertFalse(MutationTypeMatching.matches(MutationType.INFRAME_INSERTION, missense))
        assertTrue(MutationTypeMatching.matches(MutationType.ANY, missense))
    }
}