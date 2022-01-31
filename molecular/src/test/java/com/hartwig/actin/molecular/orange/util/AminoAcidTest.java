package com.hartwig.actin.molecular.orange.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AminoAcidTest {

    @Test
    public void canConvertAminoAcidsToSingleLetter() {
        assertEquals("p.V600E", AminoAcid.forceSingleLetterAminoAcids("p.Val600Glu"));
        assertEquals("p.V600E", AminoAcid.forceSingleLetterAminoAcids("p.V600E"));
    }
}