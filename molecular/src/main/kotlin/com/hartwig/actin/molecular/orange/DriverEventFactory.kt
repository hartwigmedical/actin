package com.hartwig.actin.molecular.orange

import com.hartwig.actin.molecular.util.ImpactDisplay
import com.hartwig.hmftools.datamodel.linx.LinxBreakend
import com.hartwig.hmftools.datamodel.linx.LinxFusion
import com.hartwig.hmftools.datamodel.linx.LinxHomozygousDisruption
import com.hartwig.hmftools.datamodel.purple.CopyNumberInterpretation
import com.hartwig.hmftools.datamodel.purple.PurpleCodingEffect
import com.hartwig.hmftools.datamodel.purple.PurpleGainLoss
import com.hartwig.hmftools.datamodel.purple.PurpleGeneCopyNumber
import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.hmftools.datamodel.purple.PurpleVariantEffect
import com.hartwig.hmftools.datamodel.virus.VirusInterpretation
import com.hartwig.hmftools.datamodel.virus.VirusInterpreterEntry

object DriverEventFactory {

    fun variantEvent(variant: PurpleVariant): String {
        return variant.gene() + " " + impact(variant)
    }

    private fun impact(variant: PurpleVariant): String {
        val canonical = variant.canonicalImpact()

        return ImpactDisplay.formatVariantImpact(
            canonical.hgvsProteinImpact(),
            canonical.hgvsCodingImpact(),
            canonical.codingEffect() == PurpleCodingEffect.SPLICE,
            canonical.effects().contains(PurpleVariantEffect.UPSTREAM_GENE),
            canonical.effects().joinToString("&") { it.toString() }
        )
    }

    fun gainLossEvent(gainLoss: PurpleGainLoss): String {
        return when (gainLoss.interpretation()) {
            CopyNumberInterpretation.PARTIAL_GAIN, CopyNumberInterpretation.FULL_GAIN -> {
                gainLoss.gene() + " amp"
            }

            CopyNumberInterpretation.PARTIAL_LOSS, CopyNumberInterpretation.FULL_LOSS -> {
                gainLoss.gene() + " del"
            }
        }
    }

    fun geneCopyNumberEvent(geneCopyNumber: PurpleGeneCopyNumber): String {
        return geneCopyNumber.gene() + " copy number"
    }

    fun homozygousDisruptionEvent(linxHomozygousDisruption: LinxHomozygousDisruption): String {
        return linxHomozygousDisruption.gene() + " hom disruption"
    }

    fun disruptionEvent(breakend: LinxBreakend): String {
        return breakend.gene() + " disruption"
    }

    fun fusionEvent(fusion: LinxFusion): String {
        return fusion.geneStart() + " - " + fusion.geneEnd() + " fusion"
    }

    fun virusEvent(virus: VirusInterpreterEntry): String {
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
}