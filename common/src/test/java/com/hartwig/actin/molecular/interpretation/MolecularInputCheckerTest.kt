package com.hartwig.actin.molecular.interpretation

import com.hartwig.actin.molecular.filter.TestGeneFilterFactory.createAlwaysValid
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory.createValidForGenes
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker.Companion.isCodon
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker.Companion.isHlaAllele
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker.Companion.isProteinImpact
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test

class MolecularInputCheckerTest {
    @Test
    fun canDetermineWhetherGeneIsValid() {
        val alwaysValid = MolecularInputChecker(createAlwaysValid())
        val specific = MolecularInputChecker(createValidForGenes("valid"))
        Assert.assertTrue(alwaysValid.isGene("valid"))
        Assert.assertTrue(specific.isGene("valid"))
        Assert.assertTrue(alwaysValid.isGene("invalid"))
        Assert.assertFalse(specific.isGene("invalid"))
    }

    @Test
    fun canDetermineIfStringIsHlaAllele() {
        Assert.assertTrue(isHlaAllele("A*02:01"))
        Assert.assertFalse(isHlaAllele("HLA-A*02:01"))
        Assert.assertFalse(isHlaAllele("A*02"))
        Assert.assertFalse(isHlaAllele("A:01*02"))
    }

    @Test
    fun canDetermineIfStringIsProteinImpact() {
        Assert.assertTrue(isProteinImpact("?"))
        Assert.assertTrue(isProteinImpact("V600="))
        Assert.assertTrue(isProteinImpact("V600E"))
        Assert.assertTrue(isProteinImpact("V600*"))
        Assert.assertTrue(isProteinImpact("V600fs"))
        Assert.assertTrue(isProteinImpact("M1X"))
        Assert.assertTrue(isProteinImpact("H167_N173del"))
        Assert.assertTrue(isProteinImpact("N771_N773dup"))
        Assert.assertTrue(isProteinImpact("Ter1211Cext*?"))
        Assert.assertFalse(isProteinImpact(Strings.EMPTY))
        Assert.assertFalse(isProteinImpact("MG"))
        Assert.assertFalse(isProteinImpact("M0X"))
        Assert.assertFalse(isProteinImpact("not a protein impact"))
        Assert.assertFalse(isProteinImpact("Val600Glu"))
        Assert.assertFalse(isProteinImpact("600"))
        Assert.assertFalse(isProteinImpact("V600"))
        Assert.assertFalse(isProteinImpact("600E"))
        Assert.assertFalse(isProteinImpact("v600e"))
        Assert.assertFalse(isProteinImpact("BRAF"))
    }

    @Test
    fun canDetermineIfStringIsCodon() {
        Assert.assertTrue(isCodon("V600"))
        Assert.assertTrue(isCodon("M1"))
        Assert.assertFalse(isCodon(Strings.EMPTY))
        Assert.assertFalse(isCodon("M"))
        Assert.assertFalse(isCodon("M0"))
        Assert.assertFalse(isCodon("not a codon"))
        Assert.assertFalse(isCodon("Val600"))
        Assert.assertFalse(isCodon("600"))
        Assert.assertFalse(isCodon("v600"))
        Assert.assertFalse(isCodon("BRAF"))
    }
}