package com.hartwig.actin.molecular.orange.evidence.actionability

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory
import com.hartwig.hmftools.datamodel.purple.CopyNumberInterpretation
import com.hartwig.hmftools.datamodel.purple.PurpleGainLoss
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.gene.GeneEvent
import org.junit.Assert
import org.junit.Test

class CopyNumberEvidenceTest {
    @Test
    fun canDetermineCopyNumberEvidence() {
        val gene1: ActionableGene? = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.AMPLIFICATION).gene("gene 1").build()
        val gene2: ActionableGene? = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.DELETION).gene("gene 2").build()
        val gene3: ActionableGene? = TestServeActionabilityFactory.geneBuilder().event(GeneEvent.INACTIVATION).gene("gene 1").build()
        val actionable: ActionableEvents? = ImmutableActionableEvents.builder().genes(Lists.newArrayList(gene1, gene2, gene3)).build()
        val copyNumberEvidence: CopyNumberEvidence = CopyNumberEvidence.Companion.create(actionable)
        val ampGene1: PurpleGainLoss? = TestPurpleFactory.gainLossBuilder().gene("gene 1").interpretation(CopyNumberInterpretation.FULL_GAIN).build()
        val ampMatches = copyNumberEvidence.findMatches(ampGene1)
        Assert.assertEquals(1, ampMatches.size.toLong())
        Assert.assertTrue(ampMatches.contains(gene1))
        val lossGene2: PurpleGainLoss? = TestPurpleFactory.gainLossBuilder().gene("gene 2").interpretation(CopyNumberInterpretation.FULL_LOSS).build()
        val delMatches = copyNumberEvidence.findMatches(lossGene2)
        Assert.assertEquals(1, delMatches.size.toLong())
        Assert.assertTrue(delMatches.contains(gene2))
        val lossGene1: PurpleGainLoss? = TestPurpleFactory.gainLossBuilder().gene("gene 1").interpretation(CopyNumberInterpretation.FULL_LOSS).build()
        Assert.assertTrue(copyNumberEvidence.findMatches(lossGene1).isEmpty())
    }
}