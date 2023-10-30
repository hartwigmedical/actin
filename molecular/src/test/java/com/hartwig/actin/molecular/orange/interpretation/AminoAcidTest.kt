package com.hartwig.actin.molecular.orange.interpretation

import org.junit.Assert
import org.junit.Test

class AminoAcidTest {
    @Test
    fun canConvertAminoAcidsToSingleLetter() {
        Assert.assertEquals("p.V600E", AminoAcid.forceSingleLetterAminoAcids("p.Val600Glu"))
        Assert.assertEquals("p.V600E", AminoAcid.forceSingleLetterAminoAcids("p.V600E"))
    }
}