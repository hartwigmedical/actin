package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


private const val PREFIX = "Mutation in"

private const val GENE = "GENE"

class TargetCoveragePredicateTest {

    @Test
    fun `Should test for all targets when any predicate`() {
        val predicate = all(PREFIX)
        assertThat(predicate.test(listOf(MolecularTestTarget.MUTATION))).isFalse()
        assertThat(predicate.test(MolecularTestTarget.entries)).isTrue()
        assertThat(predicate.message(GENE)).isEqualTo("Mutation in GENE undetermined (not tested for fusions, mutations, amplifications and deletions)")
    }

    @Test
    fun `Should test for any targets when any predicate`() {
        val predicate = any(PREFIX)
        assertThat(predicate.test(listOf(MolecularTestTarget.MUTATION))).isTrue()
        assertThat(predicate.test(listOf(MolecularTestTarget.FUSION))).isTrue()
        assertThat(predicate.test(listOf(MolecularTestTarget.AMPLIFICATION))).isTrue()
        assertThat(predicate.test(listOf(MolecularTestTarget.DELETION))).isTrue()
        assertThat(predicate.message(GENE)).isEqualTo("Mutation in GENE undetermined (not tested for fusions, mutations, amplifications or deletions)")
    }

    @Test
    fun `Should test for all of list of targets when and predicate`() {
        val predicate = and(MolecularTestTarget.FUSION, MolecularTestTarget.MUTATION, messagePrefix = PREFIX)
        assertThat(predicate.test(listOf(MolecularTestTarget.MUTATION, MolecularTestTarget.FUSION))).isTrue()
        assertThat(predicate.test(listOf(MolecularTestTarget.FUSION))).isFalse()
        assertThat(predicate.message(GENE)).isEqualTo("Mutation in GENE undetermined (not tested for fusions and mutations)")
    }

    @Test
    fun `Should test for one of list of targets when or predicate`() {
        val predicate = or(MolecularTestTarget.MUTATION, MolecularTestTarget.FUSION, messagePrefix = PREFIX)
        assertThat(predicate.test(listOf(MolecularTestTarget.MUTATION))).isTrue()
        assertThat(predicate.test(listOf(MolecularTestTarget.FUSION, MolecularTestTarget.AMPLIFICATION))).isTrue()
        assertThat(predicate.test(listOf(MolecularTestTarget.AMPLIFICATION))).isFalse()
        assertThat(predicate.test(listOf(MolecularTestTarget.DELETION))).isFalse()
        assertThat(predicate.message(GENE)).isEqualTo("Mutation in GENE undetermined (not tested for mutations or fusions)")
    }

    @Test
    fun `Should test for single target when at least predicate`() {
        val predicate = atLeast(MolecularTestTarget.MUTATION, PREFIX)
        assertThat(predicate.test(listOf(MolecularTestTarget.MUTATION))).isTrue()
        assertThat(predicate.test(listOf(MolecularTestTarget.FUSION))).isFalse()
        assertThat(predicate.test(listOf(MolecularTestTarget.AMPLIFICATION))).isFalse()
        assertThat(predicate.test(listOf(MolecularTestTarget.DELETION))).isFalse()
        assertThat(predicate.message(GENE)).isEqualTo("Mutation in GENE undetermined (not tested for mutations)")
    }
}
