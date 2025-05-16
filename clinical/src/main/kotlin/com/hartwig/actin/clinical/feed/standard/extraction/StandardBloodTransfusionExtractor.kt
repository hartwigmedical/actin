package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.provided.ProvidedBloodTransfusionProduct
import com.hartwig.actin.datamodel.clinical.BloodTransfusion
import com.hartwig.feed.datamodel.FeedPatientRecord

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
        val product = enumeratedInput<ProvidedBloodTransfusionProduct>(input)
        return when (product) {
            ProvidedBloodTransfusionProduct.PLASMA_A, ProvidedBloodTransfusionProduct.PLASMA_B, ProvidedBloodTransfusionProduct.PLASMA_O, ProvidedBloodTransfusionProduct.PLASMA_AB, ProvidedBloodTransfusionProduct.APHERESIS_PLASMA -> "Plasma"
            ProvidedBloodTransfusionProduct.PLATELETS_POOLED, ProvidedBloodTransfusionProduct.PLATELETS_APHERESIS, ProvidedBloodTransfusionProduct.PLATELETS_POOLED_RADIATED -> "Trombocyte"
            ProvidedBloodTransfusionProduct.ERTHROCYTES_FILTERED, ProvidedBloodTransfusionProduct.ERYTHROCYTES_RADIATED -> "Erythrocytes"
            else -> input
        }
    }
}