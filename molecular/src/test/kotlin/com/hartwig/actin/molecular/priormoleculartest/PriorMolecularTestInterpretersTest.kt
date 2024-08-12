package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.OtherPriorMolecularTest
import com.hartwig.actin.molecular.datamodel.panel.PanelRecord
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.evidence.matching.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import com.hartwig.actin.molecular.paver.PaveCodingEffect
import com.hartwig.actin.molecular.paver.PaveImpact
import com.hartwig.actin.molecular.paver.PaveQuery
import com.hartwig.actin.molecular.paver.PaveResponse
import com.hartwig.actin.molecular.paver.Paver
import com.hartwig.actin.tools.pave.ImmutableVariantTranscriptImpact
import com.hartwig.actin.tools.pave.PaveLite
import com.hartwig.actin.tools.variant.ImmutableVariant
import com.hartwig.actin.tools.variant.VariantAnnotator
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PriorMolecularTestInterpretersTest {

    private val evidenceDatabase = mockk<EvidenceDatabase> {
        every { evidenceForVariant(any()) } returns ActionabilityMatch(emptyList(), emptyList())
        every { geneAlterationForVariant(any()) } returns null
    }
    private val geneDriverLikelihoodModel = mockk<GeneDriverLikelihoodModel> {
        every { evaluate(any(), any(), any()) } returns null
    }
    private val transvarAnnotator = mockk<VariantAnnotator> {
        every { resolve(any(), any(), any()) } returns ImmutableVariant.builder().alt("G").ref("T")
            .chromosome("1").position(1).build()
    }
    private val paveLite = mockk<PaveLite> {
        every { run(any(), any(), any()) } returns ImmutableVariantTranscriptImpact.builder().affectedExon(1).affectedCodon(1).build()
    }
    private val paver = mockk<Paver> {
        every { run(any<List<PaveQuery>>()) } answers {
            val arg = firstArg<List<PaveQuery>>()
            if (arg.isEmpty()) {
                emptyList()
            } else {
                listOf(PaveResponse(id = "0", impact = paveImpact(), transcriptImpact = emptyList()))
            }
        }
    }

    @Test
    fun `Should interpret list of molecular tests`() {
        val interpreters = PriorMolecularTestInterpreters.create(evidenceDatabase, geneDriverLikelihoodModel, transvarAnnotator, paver, paveLite)
        val priorMolecularTests = listOf(
            archerPriorMolecularVariantRecord(),
            avlPanelPriorMolecularVariantRecord(),
            freetextPriorMolecularFusionRecord(),
            PriorMolecularTest("Unknown", impliesPotentialIndeterminateStatus = false)
        )
        val molecularTests = interpreters.process(priorMolecularTests)
        assertThat(molecularTests.filterIsInstance<PanelRecord>()).hasSize(3)
        assertThat(molecularTests.filterIsInstance<OtherPriorMolecularTest>()).hasSize(1)
    }

    private fun paveImpact(): PaveImpact {
        return PaveImpact(
            gene = "",
            transcript = "",
            canonicalEffect = "",
            canonicalCodingEffect = PaveCodingEffect.NONE,
            spliceRegion = false,
            hgvsCodingImpact = "",
            hgvsProteinImpact = "",
            otherReportableEffects = null,
            worstCodingEffect = PaveCodingEffect.NONE,
            genesAffected = 1
        )

    }
}