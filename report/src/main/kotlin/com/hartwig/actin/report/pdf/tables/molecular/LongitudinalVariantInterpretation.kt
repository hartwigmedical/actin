package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.molecular.datamodel.GeneAlteration
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.Variant
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumber

object LongitudinalVariantInterpretation {

     fun interpret(driver: GeneAlteration): String {
        val mutationType = when (driver) {
            is Variant -> driver.canonicalImpact.codingEffect?.display() ?: ""
            is CopyNumber -> if (driver.type.isGain) "Amplification" else "Deletion"
            else -> ""
        }
        val proteinEffect = proteinEffect(driver)?.let { "\n$it" } ?: ""
        val isHotspot = if ((driver as? Variant)?.isHotspot == true) "\nHotspot" else ""
        val isVUS = if (proteinEffect.isEmpty() && isHotspot.isEmpty() && driver !is CopyNumber) "\nVUS" else ""
        return "$mutationType$proteinEffect$isHotspot$isVUS"
    }

    private fun proteinEffect(driver: GeneAlteration): String? {
        return when (driver.proteinEffect) {
            ProteinEffect.GAIN_OF_FUNCTION,
            ProteinEffect.GAIN_OF_FUNCTION_PREDICTED -> "Gain of function"

            ProteinEffect.LOSS_OF_FUNCTION,
            ProteinEffect.LOSS_OF_FUNCTION_PREDICTED -> "Loss of function"

            else -> null
        }
    }
}