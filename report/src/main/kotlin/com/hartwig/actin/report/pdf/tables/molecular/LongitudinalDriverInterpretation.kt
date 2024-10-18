package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.Fusion
import com.hartwig.actin.datamodel.molecular.GeneAlteration
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.Variant
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber

private val VUS_PROTEIN_EFFECTS =
    setOf(ProteinEffect.AMBIGUOUS, ProteinEffect.NO_EFFECT, ProteinEffect.NO_EFFECT_PREDICTED, ProteinEffect.UNKNOWN)

object LongitudinalDriverInterpretation {

    fun interpret(driver: GeneAlteration): String {
        val mutationTypeText = when (driver) {
            is Variant -> driver.canonicalImpact.codingEffect?.display() ?: ""
            is CopyNumber -> if (driver.type.isGain) "Amplification" else "Deletion"
            else -> null
        }
        val proteinEffectText = proteinEffect(driver.proteinEffect)
        val hotspotText = if ((driver as? Variant)?.isHotspot == true) "Hotspot" else null
        val vusText = if (driver.proteinEffect in VUS_PROTEIN_EFFECTS && hotspotText == null && driver !is CopyNumber) "VUS" else null
        return listOfNotNull(mutationTypeText, proteinEffectText, hotspotText, vusText).joinToString("\n")
    }

    fun interpret(fusion: Fusion): String {
        val description = "Fusion"
        val fusionType = fusion.driverType.display()
        return listOfNotNull(description, fusionType, proteinEffect(fusion.proteinEffect)).joinToString("\n")
    }

    private fun proteinEffect(proteinEffect: ProteinEffect): String {
        return when (proteinEffect) {
            ProteinEffect.GAIN_OF_FUNCTION,
            ProteinEffect.GAIN_OF_FUNCTION_PREDICTED -> "Gain of function"

            ProteinEffect.LOSS_OF_FUNCTION,
            ProteinEffect.LOSS_OF_FUNCTION_PREDICTED -> "Loss of function"

            ProteinEffect.UNKNOWN -> "Unknown protein effect"

            ProteinEffect.AMBIGUOUS -> "Unknown protein effect"

            ProteinEffect.NO_EFFECT,
            ProteinEffect.NO_EFFECT_PREDICTED -> "No protein effect"
        }
    }
}