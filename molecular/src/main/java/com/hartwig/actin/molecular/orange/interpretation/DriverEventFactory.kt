package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.hmftools.datamodel.linx.HomozygousDisruption
import com.hartwig.hmftools.datamodel.linx.LinxBreakend
import com.hartwig.hmftools.datamodel.linx.LinxFusion
import com.hartwig.hmftools.datamodel.purple.CopyNumberInterpretation
import com.hartwig.hmftools.datamodel.purple.PurpleCodingEffect
import com.hartwig.hmftools.datamodel.purple.PurpleGainLoss
import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.hmftools.datamodel.purple.PurpleVariantEffect
import com.hartwig.hmftools.datamodel.virus.AnnotatedVirus
import com.hartwig.hmftools.datamodel.virus.VirusInterpretation
import org.apache.logging.log4j.LogManager
import java.util.*

object DriverEventFactory {
    private val LOGGER = LogManager.getLogger(DriverEventFactory::class.java)
    fun variantEvent(variant: PurpleVariant): String {
        return variant.gene() + " " + impact(variant)
    }

    private fun impact(variant: PurpleVariant): String {
        val canonical = variant.canonicalImpact()
        if (!canonical.hgvsProteinImpact().isEmpty() && canonical.hgvsProteinImpact() != "p.?") {
            return reformatProteinImpact(canonical.hgvsProteinImpact())
        }
        if (!canonical.hgvsCodingImpact().isEmpty()) {
            return if (canonical.codingEffect() == PurpleCodingEffect.SPLICE) canonical.hgvsCodingImpact() + " splice" else canonical.hgvsCodingImpact()
        }
        if (canonical.effects().contains(PurpleVariantEffect.UPSTREAM_GENE)) {
            return "upstream"
        }
        val joiner = StringJoiner("&")
        for (effect in canonical.effects()) {
            joiner.add(effect.toString())
        }
        return joiner.toString()
    }

    private fun reformatProteinImpact(proteinImpact: String): String {
        val reformatted = if (proteinImpact.startsWith("p.")) proteinImpact.substring(2) else proteinImpact
        return AminoAcid.forceSingleLetterAminoAcids(reformatted)
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
        LOGGER.warn("Unexpected copy number interpretation for generating event: {}", gainLoss.interpretation())
        return gainLoss.gene() + " unknown copy number event"
    }

    fun homozygousDisruptionEvent(homozygousDisruption: HomozygousDisruption): String {
        return homozygousDisruption.gene() + " hom disruption"
    }

    fun disruptionEvent(breakend: LinxBreakend): String {
        return breakend.gene() + " disruption"
    }

    fun fusionEvent(fusion: LinxFusion): String {
        return fusion.geneStart() + " - " + fusion.geneEnd() + " fusion"
    }

    fun virusEvent(virus: AnnotatedVirus): String {
        val interpretation = virus.interpretation()
        return if (interpretation != null) {
            if (virus.interpretation() == VirusInterpretation.HPV) {
                String.format("%s (%s) positive", interpretation, virus.name())
            } else {
                String.format("%s positive", interpretation)
            }
        } else {
            String.format("%s positive", virus.name())
        }
    }
}
