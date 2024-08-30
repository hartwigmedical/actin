package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.datamodel.molecular.orange.pharmaco.Haplotype
import com.hartwig.actin.datamodel.molecular.orange.pharmaco.HaplotypeFunction
import com.hartwig.actin.datamodel.molecular.orange.pharmaco.PharmacoEntry
import com.hartwig.actin.datamodel.molecular.orange.pharmaco.PharmacoGene
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DPYDDeficiencyEvaluationFunctionsTest {

    private val homozygousEntry =
        PharmacoEntry(gene = PharmacoGene.DPYD, haplotypes = setOf(Haplotype(allele = "*1", alleleCount = 2, function = HaplotypeFunction.REDUCED_FUNCTION)))
    private val heterozygousEntry = PharmacoEntry(
        gene = PharmacoGene.DPYD,
        haplotypes = setOf(
            Haplotype(allele = "*1", alleleCount = 1, function = HaplotypeFunction.NO_FUNCTION),
            Haplotype(allele = "*2", alleleCount = 1, function = HaplotypeFunction.NORMAL_FUNCTION)
        )
    )
    private val proficientEntry =
        PharmacoEntry(gene = PharmacoGene.DPYD, haplotypes = setOf(Haplotype(allele = "*1", alleleCount = 2, function = HaplotypeFunction.NORMAL_FUNCTION)))

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
}