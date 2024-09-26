package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.Fusion
import com.hartwig.actin.datamodel.molecular.GeneAlteration
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.Variant
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber

object LongitudinalDriverInterpretation {

    fun interpret(driver: GeneAlteration): String {
        val mutationTypeText = when (driver) {
            is Variant -> driver.canonicalImpact.codingEffect?.display() ?: ""
            is CopyNumber -> if (driver.type.isGain) "Amplification" else "Deletion"
            else -> null
        }
        val proteinEffectText = proteinEffect(driver.proteinEffect)
        val hotspotText = if ((driver as? Variant)?.isHotspot == true) "Hotspot" else null
        val vusText = if (proteinEffectText == null && hotspotText == null && driver !is CopyNumber) "VUS" else null
        return listOfNotNull(mutationTypeText, proteinEffectText, hotspotText, vusText).joinToString("\n")
    }

    fun interpret(fusion: Fusion): String {
        return proteinEffect(fusion.proteinEffect) ?: "N/A"
    }

    private fun proteinEffect(proteinEffect: ProteinEffect): String? {
        return when (proteinEffect) {
            ProteinEffect.GAIN_OF_FUNCTION,
            ProteinEffect.GAIN_OF_FUNCTION_PREDICTED -> "Gain of function"

            ProteinEffect.LOSS_OF_FUNCTION,
            ProteinEffect.LOSS_OF_FUNCTION_PREDICTED -> "Loss of function"

            else -> null
        }
    }
}