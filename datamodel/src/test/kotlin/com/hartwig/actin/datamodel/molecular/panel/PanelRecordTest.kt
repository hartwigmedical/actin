package com.hartwig.actin.datamodel.molecular.panel

import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import org.assertj.core.api.Assertions
import org.junit.Test

class PanelRecordTest {

    @Test
    fun `Should check panel specification for tested gene and target when targeted`() {
        val targets = listOf(MolecularTestTarget.MUTATION)
        val gene = "gene"
        val record =
            TestMolecularFactory.createMinimalTestPanelRecord().copy(targetSpecification = PanelTargetSpecification(mapOf(gene to targets)))
        Assertions.assertThat(record.testsGene(gene) { it == targets }).isTrue()
        Assertions.assertThat(record.testsGene(gene) { it == listOf(MolecularTestTarget.FUSION) }).isFalse()
    }
}