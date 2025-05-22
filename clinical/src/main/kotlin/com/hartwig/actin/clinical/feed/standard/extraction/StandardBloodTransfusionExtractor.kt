package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.BloodTransfusion
import com.hartwig.feed.datamodel.FeedPatientRecord

private enum class BloodTransfusionProduct {
    PLASMA_A,
    PLASMA_B,
    PLASMA_O,
    PLASMA_AB,
    PLATELETS_POOLED,
    PLATELETS_POOLED_RADIATED,
    ERYTHROCYTES_RADIATED,
    APHERESIS_PLASMA,
    ERTHROCYTES_FILTERED,
    PLATELETS_APHERESIS
}

class StandardBloodTransfusionExtractor : StandardDataExtractor<List<BloodTransfusion>> {
    override fun extract(ehrPatientRecord: FeedPatientRecord): ExtractionResult<List<BloodTransfusion>> {
        return ExtractionResult(ehrPatientRecord.bloodTransfusions.map {
            BloodTransfusion(
                date = it.evaluationDate,
                product = mapTransfusionProduct(it.name)
            )
        }, CurationExtractionEvaluation())
    }

    private fun mapTransfusionProduct(input: String): String {
        val product = enumeratedInput<BloodTransfusionProduct>(input)
        return when (product) {
            BloodTransfusionProduct.PLASMA_A, BloodTransfusionProduct.PLASMA_B, BloodTransfusionProduct.PLASMA_O, BloodTransfusionProduct.PLASMA_AB, BloodTransfusionProduct.APHERESIS_PLASMA -> "Plasma"
            BloodTransfusionProduct.PLATELETS_POOLED, BloodTransfusionProduct.PLATELETS_APHERESIS, BloodTransfusionProduct.PLATELETS_POOLED_RADIATED -> "Trombocyte"
            BloodTransfusionProduct.ERTHROCYTES_FILTERED, BloodTransfusionProduct.ERYTHROCYTES_RADIATED -> "Erythrocytes"
            else -> input
        }
    }
}