package com.hartwig.actin.molecular.findings

import com.hartwig.actin.molecular.util.FormatFunctions
import com.hartwig.hmftools.datamodel.finding.Disruption
import com.hartwig.hmftools.datamodel.finding.SmallVariant
import com.hartwig.hmftools.datamodel.finding.Virus
import com.hartwig.hmftools.datamodel.hla.LilacAllele
import com.hartwig.hmftools.datamodel.linx.LinxBreakend
import com.hartwig.hmftools.datamodel.linx.LinxFusion
import com.hartwig.hmftools.datamodel.purple.CopyNumberInterpretation
import com.hartwig.hmftools.datamodel.purple.PurpleCodingEffect
import com.hartwig.hmftools.datamodel.purple.PurpleGainDeletion
import com.hartwig.hmftools.datamodel.purple.PurpleGeneCopyNumber
import com.hartwig.hmftools.datamodel.purple.PurpleVariantEffect
import com.hartwig.hmftools.datamodel.virus.VirusInterpretation
import com.hartwig.hmftools.datamodel.virus.VirusInterpreterEntry

object DriverEventFactory {

    fun variantEvent(variant: SmallVariant): String {
        return variant.gene() + " " + impact(variant)
    }

    private fun impact(variant: SmallVariant): String {
        val canonical = variant.transcriptImpact()

        return FormatFunctions.formatVariantImpact(
            canonical.hgvsProteinImpact(),
            canonical.hgvsCodingImpact(),
            canonical.codingEffect() == PurpleCodingEffect.SPLICE,
            canonical.effects().contains(PurpleVariantEffect.UPSTREAM_GENE),
            canonical.effects().joinToString("&") { it.toString() }
        )
    }

    fun gainDelEvent(gainDel: PurpleGainDeletion): String {
        return when (gainDel.interpretation()) {
            CopyNumberInterpretation.PARTIAL_GAIN -> gainDel.gene() + " partial amp"
            CopyNumberInterpretation.FULL_GAIN -> gainDel.gene() + " amp"
            CopyNumberInterpretation.PARTIAL_DEL, CopyNumberInterpretation.FULL_DEL -> gainDel.gene() + " del"
        }
    }

    fun geneCopyNumberEvent(geneCopyNumber: PurpleGeneCopyNumber): String {
        return geneCopyNumber.gene() + " copy number"
    }

    fun homozygousDisruptionEvent(disruption: Disruption): String {
        return disruption.gene() + " hom disruption"
    }

    fun disruptionEvent(disruption: Disruption): String {
        return disruption.gene() + " disruption"
    }

    fun fusionEvent(fusion: com.hartwig.hmftools.datamodel.finding.Fusion): String {
        return FormatFunctions.formatFusionEvent(
            geneUp = fusion.geneStart(),
            exonUp = fusion.fusedExonUp(),
            geneDown = fusion.geneEnd(),
            exonDown = fusion.fusedExonDown()
        )
    }

    fun virusEvent(virus: Virus): String {
        val label = when (val interpretation = virus.interpretation()) {
            null -> {
                virus.name()
            }

            VirusInterpretation.HPV -> {
                "$interpretation (${virus.name()})"
            }

            else -> {
                interpretation
            }
        }
        return "$label positive"
    }

    fun immunologyEvent(hla: com.hartwig.hmftools.datamodel.finding.HlaAllele): String {
        return "HLA-${hla.allele()}"
    }
}