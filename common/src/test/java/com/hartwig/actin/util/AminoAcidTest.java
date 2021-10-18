package com.hartwig.actin.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AminoAcidTest {

    @Test
    public void canConvertAminoAcidsToSingleLetter() {
        assertEquals("BRAF p.V600E", AminoAcid.forceSingleLetterAminoAcids("BRAF p.Val600Glu"));
        assertEquals("BRAF p.V600E", AminoAcid.forceSingleLetterAminoAcids("BRAF p.V600E"));
    }
}