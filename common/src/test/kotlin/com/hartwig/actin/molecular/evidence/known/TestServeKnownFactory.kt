package com.hartwig.actin.molecular.evidence.known

import com.hartwig.serve.datamodel.molecular.MutationType
import com.hartwig.serve.datamodel.molecular.common.GeneAlteration
import com.hartwig.serve.datamodel.molecular.common.GeneRole
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect
import com.hartwig.serve.datamodel.molecular.fusion.FusionPair
import com.hartwig.serve.datamodel.molecular.fusion.ImmutableKnownFusion
import com.hartwig.serve.datamodel.molecular.gene.GeneAnnotation
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.molecular.gene.ImmutableKnownCopyNumber
import com.hartwig.serve.datamodel.molecular.gene.ImmutableKnownGene
import com.hartwig.serve.datamodel.molecular.hotspot.ImmutableKnownHotspot
import com.hartwig.serve.datamodel.molecular.hotspot.VariantHotspot
import com.hartwig.serve.datamodel.molecular.range.ImmutableKnownCodon
import com.hartwig.serve.datamodel.molecular.range.ImmutableKnownExon
import com.hartwig.serve.datamodel.molecular.range.RangeAnnotation

object TestServeKnownFactory {

    fun hotspotBuilder(): ImmutableKnownHotspot.Builder {
        return ImmutableKnownHotspot.builder()
            .from(createEmptyGeneAlteration())
            .from(createEmptyHotspot())
            .inputProteinAnnotation("")
    }

    fun codonBuilder(): ImmutableKnownCodon.Builder {
        return ImmutableKnownCodon.builder()
            .from(createEmptyGeneAlteration())
            .from(createEmptyRangeAnnotation())
            .inputTranscript("")
            .inputCodonRank(0)
    }

    fun exonBuilder(): ImmutableKnownExon.Builder {
        return ImmutableKnownExon.builder()
            .from(createEmptyGeneAlteration())
            .from(createEmptyRangeAnnotation())
            .inputTranscript("")
            .inputExonRank(0)
    }

    fun geneBuilder(): ImmutableKnownGene.Builder {
        return ImmutableKnownGene.builder().gene("").geneRole(GeneRole.UNKNOWN)
    }

    fun copyNumberBuilder(): ImmutableKnownCopyNumber.Builder {
        return ImmutableKnownCopyNumber.builder().from(createEmptyGeneAlteration()).from(createEmptyGeneAnnotation())
    }

    fun fusionBuilder(): ImmutableKnownFusion.Builder {
        return ImmutableKnownFusion.builder().from(createEmptyFusionPair()).proteinEffect(ProteinEffect.UNKNOWN)
    }

    private fun createEmptyGeneAlteration(): GeneAlteration {
        return createGeneAlteration(GeneRole.UNKNOWN, ProteinEffect.UNKNOWN)
    }

    @JvmOverloads
    fun createGeneAlteration(
        geneRole: GeneRole,
        proteinEffect: ProteinEffect,
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

    private fun createEmptyHotspot(): VariantHotspot {
        return object : VariantHotspot {
            override fun gene(): String {
                return ""
            }

            override fun ref(): String {
                return ""
            }

            override fun alt(): String {
                return ""
            }

            override fun chromosome(): String {
                return ""
            }

            override fun position(): Int {
                return 0
            }
        }
    }

    private fun createEmptyRangeAnnotation(): RangeAnnotation {
        return object : RangeAnnotation {
            override fun gene(): String {
                return ""
            }

            override fun applicableMutationType(): MutationType {
                return MutationType.ANY
            }

            override fun chromosome(): String {
                return ""
            }

            override fun start(): Int {
                return 0
            }

            override fun end(): Int {
                return 0
            }
        }
    }

    private fun createEmptyGeneAnnotation(): GeneAnnotation {
        return object : GeneAnnotation {
            override fun gene(): String {
                return ""
            }

            override fun event(): GeneEvent {
                return GeneEvent.ANY_MUTATION
            }
        }
    }

    private fun createEmptyFusionPair(): FusionPair {
        return object : FusionPair {
            override fun geneUp(): String {
                return ""
            }

            override fun minExonUp(): Int? {
                return null
            }

            override fun maxExonUp(): Int? {
                return null
            }

            override fun geneDown(): String {
                return ""
            }

            override fun minExonDown(): Int? {
                return null
            }

            override fun maxExonDown(): Int? {
                return null
            }
        }
    }
}
