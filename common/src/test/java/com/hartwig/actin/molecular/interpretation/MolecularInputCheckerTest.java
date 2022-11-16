package com.hartwig.actin.molecular.interpretation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MolecularInputCheckerTest {

    @Test
    public void canDetermineIfStringIsProteinImpact() {
        assertTrue(MolecularInputChecker.isProteinImpact("V600E"));
        assertTrue(MolecularInputChecker.isProteinImpact("M1X"));

        assertFalse(MolecularInputChecker.isProteinImpact("not a protein impact"));
        assertFalse(MolecularInputChecker.isProteinImpact("Val600Glu"));
        assertFalse(MolecularInputChecker.isProteinImpact("600"));
        assertFalse(MolecularInputChecker.isProteinImpact("V600"));
        assertFalse(MolecularInputChecker.isProteinImpact("600E"));
        assertFalse(MolecularInputChecker.isProteinImpact("v600e"));
        assertFalse(MolecularInputChecker.isProteinImpact("BRAF"));
    }
}