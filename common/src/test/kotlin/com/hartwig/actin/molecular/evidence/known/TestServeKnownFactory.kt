package com.hartwig.actin.molecular.evidence.known

import com.hartwig.actin.molecular.evidence.TestServeFactory
import com.hartwig.serve.datamodel.molecular.common.GeneAlteration
import com.hartwig.serve.datamodel.molecular.common.GeneRole
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect
import com.hartwig.serve.datamodel.molecular.fusion.ImmutableKnownFusion
import com.hartwig.serve.datamodel.molecular.gene.ImmutableKnownCopyNumber
import com.hartwig.serve.datamodel.molecular.gene.ImmutableKnownGene
import com.hartwig.serve.datamodel.molecular.hotspot.ImmutableKnownHotspot
import com.hartwig.serve.datamodel.molecular.range.ImmutableKnownCodon
import com.hartwig.serve.datamodel.molecular.range.ImmutableKnownExon

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
