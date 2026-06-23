package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.datamodel.trial.VariantTypeInput
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

fun toProteinImpact(hgvsProteinImpact: String): String {
    val impact = if (hgvsProteinImpact.startsWith("p.")) hgvsProteinImpact.substring(2) else hgvsProteinImpact
    if (impact.isEmpty()) return impact
    if (!MolecularInputChecker.isProteinImpact(impact)) {
        logger.warn { "Cannot convert hgvs protein impact to a usable protein impact: $hgvsProteinImpact" }
    }
    return impact
}

fun variantTypesForInput(variantTypeInput: VariantTypeInput): Set<VariantType> {
    return when (variantTypeInput) {
        VariantTypeInput.SNV -> setOf(VariantType.SNV)
        VariantTypeInput.MNV -> setOf(VariantType.MNV)
        VariantTypeInput.INSERT -> setOf(VariantType.INSERT)
        VariantTypeInput.DELETE -> setOf(VariantType.DELETE)
        VariantTypeInput.INDEL -> setOf(VariantType.INSERT, VariantType.DELETE)
    }
}
