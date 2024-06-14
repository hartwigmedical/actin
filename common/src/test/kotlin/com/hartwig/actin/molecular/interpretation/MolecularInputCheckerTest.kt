package com.hartwig.actin.molecular.interpretation

import com.hartwig.actin.molecular.filter.TestGeneFilterFactory.createAlwaysValid
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory.createValidForGenes
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker.Companion.isCodon
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker.Companion.isCyp
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker.Companion.isHaplotype
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker.Companion.isHlaAllele
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker.Companion.isProteinImpact
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MolecularInputCheckerTest {

    @Test
    fun `Should determine whether gene is valid`() {
        val alwaysValid = MolecularInputChecker(createAlwaysValid())
        val specific = MolecularInputChecker(createValidForGenes("valid"))
        assertThat(alwaysValid.isGene("valid")).isTrue
        assertThat(specific.isGene("valid")).isTrue
        assertThat(alwaysValid.isGene("invalid")).isTrue
        assertThat(specific.isGene("invalid")).isFalse
    }

    @Test
    fun `Should determine if string is HLA allele`() {
        assertThat(isHlaAllele("A*02:01")).isTrue
        assertThat(isHlaAllele("HLA-A*02:01")).isFalse
        assertThat(isHlaAllele("A*02")).isFalse
        assertThat(isHlaAllele("A:01*02")).isFalse
    }

    @Test
    fun `Should determine if string is haplotype`() {
        assertThat(isHaplotype("*1_HOM")).isTrue
        assertThat(isHaplotype("UGT1A1_1_HOM")).isFalse
        assertThat(isHaplotype("_1*HOM")).isFalse
        assertThat(isHaplotype("*_HOM")).isFalse
    }
    
    @Test
    fun `Should determine if string is protein impact`() {
        assertThat(isProteinImpact("?")).isTrue
        assertThat(isProteinImpact("V600=")).isTrue
        assertThat(isProteinImpact("V600E")).isTrue
        assertThat(isProteinImpact("V600*")).isTrue
        assertThat(isProteinImpact("V600fs")).isTrue
        assertThat(isProteinImpact("M1F")).isTrue
        assertThat(isProteinImpact("H167_N173del")).isTrue
        assertThat(isProteinImpact("N771_N773dup")).isTrue
        assertThat(isProteinImpact("Ter1211Cext*?")).isTrue
        assertThat(isProteinImpact("")).isFalse
        assertThat(isProteinImpact("MG")).isFalse
        assertThat(isProteinImpact("M0F")).isFalse
        assertThat(isProteinImpact("M100X")).isFalse
        assertThat(isProteinImpact("not a protein impact")).isFalse
        assertThat(isProteinImpact("Val600Glu")).isFalse
        assertThat(isProteinImpact("600")).isFalse
        assertThat(isProteinImpact("V600")).isFalse
        assertThat(isProteinImpact("600E")).isFalse
        assertThat(isProteinImpact("v600e")).isFalse
        assertThat(isProteinImpact("BRAF")).isFalse
    }

    @Test
    fun `Should determine if string is CYP`() {
        assertThat(isCyp("3A4")).isTrue
        assertThat(isCyp("CYP3A4")).isFalse
        assertThat(isCyp("A4")).isFalse
        assertThat(isCyp("A4A")).isFalse
        assertThat(isCyp("3A")).isFalse
    }

    @Test
    fun `Should determine if string is codon`() {
        assertThat(isCodon("V600")).isTrue
        assertThat(isCodon("M1")).isTrue
        assertThat(isCodon("")).isFalse
        assertThat(isCodon("M")).isFalse
        assertThat(isCodon("M0")).isFalse
        assertThat(isCodon("not a codon")).isFalse
        assertThat(isCodon("Val600")).isFalse
        assertThat(isCodon("600")).isFalse
        assertThat(isCodon("v600")).isFalse
        assertThat(isCodon("BRAF")).isFalse
    }
}