package com.hartwig.actin.molecular.interpretation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;

import org.junit.Test;

public class MolecularInputCheckerTest {

    @Test
    public void canDetermineWhetherGeneIsValid() {
        MolecularInputChecker alwaysValid = MolecularInputChecker.createAnyGeneValid();
        MolecularInputChecker specific = MolecularInputChecker.createSpecificGenesValid(Sets.newHashSet("valid"));

        assertTrue(alwaysValid.isGene("valid"));
        assertTrue(specific.isGene("valid"));

        assertTrue(alwaysValid.isGene("invalid"));
        assertFalse(specific.isGene("invalid"));
    }

    @Test
    public void canDetermineIfStringIsHlaAllele() {
        assertTrue(MolecularInputChecker.isHlaAllele("A*02:01"));

        assertFalse(MolecularInputChecker.isHlaAllele("HLA-A*02:01"));
        assertFalse(MolecularInputChecker.isHlaAllele("A*02"));
        assertFalse(MolecularInputChecker.isHlaAllele("A:01*02"));
    }

    @Test
    public void canDetermineIfStringIsProteinImpact() {
        assertTrue(MolecularInputChecker.isProteinImpact("V600E"));
        assertTrue(MolecularInputChecker.isProteinImpact("M1X"));

        assertFalse(MolecularInputChecker.isProteinImpact("M0X"));
        assertFalse(MolecularInputChecker.isProteinImpact("not a protein impact"));
        assertFalse(MolecularInputChecker.isProteinImpact("Val600Glu"));
        assertFalse(MolecularInputChecker.isProteinImpact("600"));
        assertFalse(MolecularInputChecker.isProteinImpact("V600"));
        assertFalse(MolecularInputChecker.isProteinImpact("600E"));
        assertFalse(MolecularInputChecker.isProteinImpact("v600e"));
        assertFalse(MolecularInputChecker.isProteinImpact("BRAF"));
    }

    @Test
    public void canDetermineIfStringIsCodon() {
        assertTrue(MolecularInputChecker.isCodon("V600"));
        assertTrue(MolecularInputChecker.isCodon("M1"));

        assertFalse(MolecularInputChecker.isCodon("M0"));
        assertFalse(MolecularInputChecker.isCodon("not a codon"));
        assertFalse(MolecularInputChecker.isCodon("Val600"));
        assertFalse(MolecularInputChecker.isCodon("600"));
        assertFalse(MolecularInputChecker.isCodon("v600"));
        assertFalse(MolecularInputChecker.isCodon("BRAF"));
    }
}