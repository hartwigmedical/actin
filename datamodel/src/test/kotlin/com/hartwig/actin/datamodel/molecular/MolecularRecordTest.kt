package com.hartwig.actin.datamodel.molecular

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MolecularRecordTest {

    @Test
    fun `Should check panel specification for tested gene and target when targeted`() {
        val targets = listOf(MolecularTestTarget.MUTATION)
        val gene = "gene"
        val record =
            TestMolecularFactory.createMinimalTestMolecularRecord()
                .copy(experimentType = ExperimentType.HARTWIG_TARGETED, specification = PanelSpecification(mapOf(gene to targets)))
        assertThat(record.testsGene(gene) { it == targets }).isTrue()
        assertThat(record.testsGene(gene) { it == listOf(MolecularTestTarget.FUSION) }).isFalse()
    }

    @Test
    fun `Should always return gene is tested for any target when WGS`() {
        val record =
            TestMolecularFactory.createMinimalTestMolecularRecord().copy(specification = null)
        assertThat(record.testsGene("gene") { it == listOf(MolecularTestTarget.FUSION) }).isTrue()
    }
}