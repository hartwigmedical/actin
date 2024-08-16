package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.clinical.datamodel.PriorIHCTest
import com.hartwig.actin.molecular.datamodel.OtherPriorMolecularTest
import com.hartwig.actin.molecular.datamodel.panel.PanelRecord
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.evidence.matching.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PriorMolecularTestInterpretersTest {

    private val evidenceDatabase = mockk<EvidenceDatabase> {
        every { evidenceForVariant(any()) } returns ActionabilityMatch(emptyList(), emptyList())
        every { geneAlterationForVariant(any()) } returns null
        every { evidenceForFusion(any()) } returns ActionabilityMatch(emptyList(), emptyList())
        every { lookupKnownFusion(any()) } returns null
    }

    private val panelVariantAnnotator = mockk<PanelVariantAnnotator> {
        every { annotate(any()) } returns emptySet()
    }

    private val panelFusionAnnotator = mockk<PanelFusionAnnotator> {
        every { annotate(any(), any()) } returns emptySet()
    }

    @Test
    fun `Should interpret list of molecular tests`() {
        val interpreters =
            PriorMolecularTestInterpreters.create(
                evidenceDatabase,
                panelVariantAnnotator,
                panelFusionAnnotator,
            )
        val priorMolecularTests = listOf(
            archerPriorMolecularVariantRecord(),
            avlPanelPriorMolecularVariantRecord(),
            freetextPriorMolecularFusionRecord(),
            ampliseqPriorMolecularVariantRecord(),
            PriorIHCTest("Unknown", impliesPotentialIndeterminateStatus = false)
        )
        val molecularTests = interpreters.process(priorMolecularTests)
        assertThat(molecularTests.filterIsInstance<PanelRecord>()).hasSize(4)
        assertThat(molecularTests.filterIsInstance<OtherPriorMolecularTest>()).hasSize(1)
    }
}