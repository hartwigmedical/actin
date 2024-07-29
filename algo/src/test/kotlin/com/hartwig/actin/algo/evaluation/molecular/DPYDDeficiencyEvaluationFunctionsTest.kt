package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.molecular.datamodel.orange.pharmaco.Haplotype
import com.hartwig.actin.molecular.datamodel.orange.pharmaco.PharmacoEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DPYDDeficiencyEvaluationFunctionsTest {

    private val homozygousEntry =
        PharmacoEntry(gene = "DPYD", haplotypes = setOf(Haplotype(allele = "*1", alleleCount = 2, function = "Reduced Function")))
    private val heterozygousEntry = PharmacoEntry(
        gene = "DPYD",
        haplotypes = setOf(
            Haplotype(allele = "*1", alleleCount = 1, function = "No Function"),
            Haplotype(allele = "*2", alleleCount = 1, function = "Normal function")
        )
    )
    private val proficientEntry =
        PharmacoEntry(gene = "DPYD", haplotypes = setOf(Haplotype(allele = "*1", alleleCount = 2, function = "Normal Function")))
    private val unexpectedEntry = PharmacoEntry(
        gene = "DPYD",
        haplotypes = setOf(
            Haplotype(allele = "*1", alleleCount = 1, function = "Normal Function"),
            Haplotype(allele = "*2", alleleCount = 1, function = "Unexpected Function")
        )
    )

    @Test
    fun `Should return true if patient has homozygous DPYD haplotypes with reduced function`() {
        val function = DPYDDeficiencyEvaluationFunctions.isHomozygousDeficient(homozygousEntry)
        assertThat(function).isTrue()
    }

    @Test
    fun `Should return false if patient has heterozygous or proficient DPYD haplotypes`() {
        val functionHeterozygous = DPYDDeficiencyEvaluationFunctions.isHomozygousDeficient(heterozygousEntry)
        assertThat(functionHeterozygous).isFalse()
        val functionProficient = DPYDDeficiencyEvaluationFunctions.isHomozygousDeficient(proficientEntry)
        assertThat(functionProficient).isFalse()
    }

    @Test
    fun `Should return false if patient has homozygous or heterozygous DPYD haplotypes with reduced function`() {
        val functionHomozygous = DPYDDeficiencyEvaluationFunctions.isProficient(homozygousEntry)
        assertThat(functionHomozygous).isFalse()
        val functionHeterozygous = DPYDDeficiencyEvaluationFunctions.isProficient(heterozygousEntry)
        assertThat(functionHeterozygous).isFalse()
    }

    @Test
    fun `Should return true if patient has proficient DPYD haplotypes with reduced function`() {
        val function = DPYDDeficiencyEvaluationFunctions.isProficient(proficientEntry)
        assertThat(function).isTrue()
    }

    @Test
    fun `Should return true if unexpected DPYD haplotype`() {
        val function = DPYDDeficiencyEvaluationFunctions.containsUnexpectedHaplotypeFunction(unexpectedEntry)
        assertThat(function).isTrue()
    }

    @Test
    fun `Should return false if homozygous DPYD haplotype`() {
        val function = DPYDDeficiencyEvaluationFunctions.containsUnexpectedHaplotypeFunction(homozygousEntry)
        assertThat(function).isFalse()
    }
}