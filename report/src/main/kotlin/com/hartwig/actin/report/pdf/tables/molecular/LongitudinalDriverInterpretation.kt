package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.Fusion
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.Variant
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.orange.driver.FusionDriverType

private val VUS_PROTEIN_EFFECTS =
    setOf(ProteinEffect.AMBIGUOUS, ProteinEffect.NO_EFFECT, ProteinEffect.NO_EFFECT_PREDICTED, ProteinEffect.UNKNOWN)

object LongitudinalDriverInterpretation {

    fun interpret(variant: Variant): String {
        val mutationTypeText = variant.canonicalImpact.codingEffect?.display() ?: ""
        val proteinEffectText = proteinEffect(variant.proteinEffect)
        val hotspotText = if ((variant as? Variant)?.isHotspot == true) "Hotspot" else null
        val vusText = if (variant.proteinEffect in VUS_PROTEIN_EFFECTS && hotspotText == null) "VUS" else null
        return listOfNotNull(mutationTypeText, proteinEffectText, hotspotText, vusText).joinToString("\n")
    }

    fun interpret(copyNumber: CopyNumber): String {
        val mutationTypeText = if (copyNumber.type.isGain) "Amplification" else "Deletion"
        val proteinEffectText = proteinEffect(copyNumber.proteinEffect)
        return listOfNotNull(mutationTypeText, proteinEffectText).joinToString("\n")
    }

    fun interpret(fusion: Fusion): String {
        val description = "Fusion"
        val fusionType = if (fusion.driverType != FusionDriverType.NONE) fusion.driverType.display() else null
        return listOfNotNull(description, fusionType, proteinEffect(fusion.proteinEffect)).joinToString("\n")
    }

    private fun proteinEffect(proteinEffect: ProteinEffect): String {
        return when (proteinEffect) {
            ProteinEffect.GAIN_OF_FUNCTION,
            ProteinEffect.GAIN_OF_FUNCTION_PREDICTED -> "Gain of function"

            ProteinEffect.LOSS_OF_FUNCTION,
            ProteinEffect.LOSS_OF_FUNCTION_PREDICTED -> "Loss of function"

            ProteinEffect.UNKNOWN -> "Unknown protein effect"

            ProteinEffect.AMBIGUOUS -> "Ambiguous protein effect"

            ProteinEffect.NO_EFFECT,
            ProteinEffect.NO_EFFECT_PREDICTED -> "No protein effect"
        }
    }
}