package com.hartwig.actin.molecular.findings

import com.hartwig.actin.molecular.util.FormatFunctions
import com.hartwig.hmftools.finding.datamodel.Disruption
import com.hartwig.hmftools.finding.datamodel.Fusion
import com.hartwig.hmftools.finding.datamodel.GainDeletion
import com.hartwig.hmftools.finding.datamodel.GainDeletion.GeneExtent
import com.hartwig.hmftools.finding.datamodel.HlaAllele
import com.hartwig.hmftools.finding.datamodel.SmallVariant
import com.hartwig.hmftools.finding.datamodel.Virus

object DriverEventFactory {

    const val FULL: String = "full"
    const val PARTIAL: String = "partial"
    const val GAIN: String = "gain"
    const val DEL: String = "del"
    const val HET_DEL: String = "het del"
    const val CN_NEUTRAL_LOH: String = "cn neutral loh"


    fun event(variant: SmallVariant): String {
        return variant.gene() + " " + impact(variant)
    }

    private fun impact(variant: SmallVariant): String {
        val canonical = variant.transcriptImpact()

        return FormatFunctions.formatVariantImpact(
            canonical.hgvsProteinImpact(),
            canonical.hgvsCodingImpact(),
            canonical.codingEffect() == SmallVariant.CodingEffect.SPLICE,
            canonical.effects().contains(SmallVariant.VariantEffect.UPSTREAM_GENE),
            canonical.effects().joinToString("&") { it.toString() }
        )
    }

    fun event(gainDeletion: GainDeletion): String {
        return geneExtend(gainDeletion.geneExtent()) + " " + type(gainDeletion.somaticType())
    }

    private fun geneExtend(geneExtent: GeneExtent): String {
        return when (geneExtent) {
            GeneExtent.FULL_GENE -> FULL
            GeneExtent.PARTIAL_GENE -> PARTIAL
        }
    }

    private fun type(type: GainDeletion.Type): String {
        return when (type) {
            GainDeletion.Type.GAIN -> GAIN
            GainDeletion.Type.HOM_DEL -> DEL
            GainDeletion.Type.HET_DEL -> HET_DEL
            GainDeletion.Type.CN_NEUTRAL_LOH -> CN_NEUTRAL_LOH
            GainDeletion.Type.NONE -> throw IllegalArgumentException("Unexpected type: " + type)
        }
    }

    fun event(disruption: Disruption): String {
        return disruption.gene() + (if (disruption.isHomozygous) " hom" else "") + " disruption"
    }

    fun event(fusion: Fusion): String {
        return FormatFunctions.formatFusionEvent(
            geneUp = fusion.geneStart(),
            exonUp = fusion.fusedExonUp(),
            geneDown = fusion.geneEnd(),
            exonDown = fusion.fusedExonDown()
        )
    }

    fun event(virus: Virus): String {
        val label = when (val interpretation = virus.oncogenicVirus()) {
            null -> {
                virus.name()
            }

            Virus.OncogenicVirus.HPV -> {
                "$interpretation (${virus.name()})"
            }

            else -> {
                interpretation
            }
        }
        return "$label positive"
    }

    fun event(hla: HlaAllele): String {
        return "HLA-${hla.allele()}"
    }
}