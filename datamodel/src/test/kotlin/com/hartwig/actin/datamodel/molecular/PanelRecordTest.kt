package com.hartwig.actin.datamodel.molecular

import org.assertj.core.api.Assertions
import org.junit.Test

class PanelRecordTest {

    @Test
    fun `Should check panel specification for tested gene and target when targeted`() {
        val targets = listOf(MolecularTestTarget.MUTATION)
        val gene = "gene"
        val record =
            TestMolecularFactory.createMinimalTestPanelRecord().copy(specification = KnownPanelSpecification(mapOf(gene to targets)))
        Assertions.assertThat(record.testsGene(gene) { it == targets }).isTrue()
        Assertions.assertThat(record.testsGene(gene) { it == listOf(MolecularTestTarget.FUSION) }).isFalse()
    }
}