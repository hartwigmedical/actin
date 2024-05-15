package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.OtherPriorMolecularTest
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanel
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanel
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PriorMolecularTestInterpretersTest {

    private val evidenceDatabase = mockk<EvidenceDatabase> {
        every { evidenceForVariant(any()) } returns ActionabilityMatch(emptyList(), emptyList())
        every { geneAlterationForVariant(any()) } returns null
    }

    @Test
    fun `Should invoke interpret list of molecular tests`() {
        val interpreters = PriorMolecularTestInterpreters.create(evidenceDatabase)
        val priorMolecularTests = listOf(
            archerPriorMolecularVariantRecord(),
            avlPanelPriorMolecularVariantRecord(),
            freetextPriorMolecularFusionRecord(),
            PriorMolecularTest("Unknown", impliesPotentialIndeterminateStatus = false)
        )
        val molecularTests = interpreters.process(priorMolecularTests)
        assertThat(molecularTests.filterIsInstance<ArcherPanel>()).hasSize(1)
        assertThat(molecularTests.filterIsInstance<GenericPanel>()).hasSize(2)
        assertThat(molecularTests.filterIsInstance<OtherPriorMolecularTest>()).hasSize(1)
    }
}