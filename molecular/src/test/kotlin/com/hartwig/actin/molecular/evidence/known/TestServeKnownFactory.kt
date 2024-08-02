package com.hartwig.actin.molecular.evidence.known

import com.hartwig.actin.serve.TestServeFactory
import com.hartwig.serve.datamodel.common.GeneAlteration
import com.hartwig.serve.datamodel.common.GeneRole
import com.hartwig.serve.datamodel.common.ProteinEffect
import com.hartwig.serve.datamodel.fusion.ImmutableKnownFusion
import com.hartwig.serve.datamodel.gene.ImmutableKnownCopyNumber
import com.hartwig.serve.datamodel.gene.ImmutableKnownGene
import com.hartwig.serve.datamodel.hotspot.ImmutableKnownHotspot
import com.hartwig.serve.datamodel.range.ImmutableKnownCodon
import com.hartwig.serve.datamodel.range.ImmutableKnownExon

object TestServeKnownFactory {

    fun hotspotBuilder(): ImmutableKnownHotspot.Builder {
        return ImmutableKnownHotspot.builder()
            .from(createEmptyGeneAlteration())
            .from(TestServeFactory.createEmptyHotspot())
            .inputProteinAnnotation("")
    }

    fun codonBuilder(): ImmutableKnownCodon.Builder {
        return ImmutableKnownCodon.builder()
            .from(createEmptyGeneAlteration())
            .from(TestServeFactory.createEmptyRangeAnnotation())
            .inputTranscript("")
            .inputCodonRank(0)
    }

    fun exonBuilder(): ImmutableKnownExon.Builder {
        return ImmutableKnownExon.builder()
            .from(createEmptyGeneAlteration())
            .from(TestServeFactory.createEmptyRangeAnnotation())
            .inputTranscript("")
            .inputExonRank(0)
    }

    fun geneBuilder(): ImmutableKnownGene.Builder {
        return ImmutableKnownGene.builder().gene("").geneRole(GeneRole.UNKNOWN)
    }

    fun copyNumberBuilder(): ImmutableKnownCopyNumber.Builder {
        return ImmutableKnownCopyNumber.builder().from(createEmptyGeneAlteration()).from(TestServeFactory.createEmptyGeneAnnotation())
    }

    fun fusionBuilder(): ImmutableKnownFusion.Builder {
        return ImmutableKnownFusion.builder().from(TestServeFactory.createEmptyFusionPair()).proteinEffect(ProteinEffect.UNKNOWN)
    }

    private fun createEmptyGeneAlteration(): GeneAlteration {
        return createGeneAlteration(GeneRole.UNKNOWN, ProteinEffect.UNKNOWN)
    }

    @JvmOverloads
    fun createGeneAlteration(
        geneRole: GeneRole, proteinEffect: ProteinEffect,
        associatedWithDrugResistance: Boolean? = null
    ): GeneAlteration {
        return object : GeneAlteration {
            override fun geneRole(): GeneRole {
                return geneRole
            }

            override fun proteinEffect(): ProteinEffect {
                return proteinEffect
            }

            override fun associatedWithDrugResistance(): Boolean? {
                return associatedWithDrugResistance
            }
        }
    }
}
