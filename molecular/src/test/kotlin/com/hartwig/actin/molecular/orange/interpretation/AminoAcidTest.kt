package com.hartwig.actin.molecular.orange.interpretation

import org.junit.Assert.assertEquals
import org.junit.Test

class AminoAcidTest {

    @Test
    fun canConvertAminoAcidsToSingleLetter() {
        assertEquals("p.V600E", AminoAcid.forceSingleLetterAminoAcids("p.Val600Glu"))
        assertEquals("p.V600E", AminoAcid.forceSingleLetterAminoAcids("p.V600E"))
    }
}