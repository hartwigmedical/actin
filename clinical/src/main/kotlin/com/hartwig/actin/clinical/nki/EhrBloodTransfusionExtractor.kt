package com.hartwig.actin.clinical.nki

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.BloodTransfusion

class EhrBloodTransfusionExtractor : EhrExtractor<List<BloodTransfusion>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<BloodTransfusion>> {
        return ExtractionResult(ehrPatientRecord.bloodTransfusions.map {
            BloodTransfusion(
                date = it.evaluationTime.toLocalDate(),
                product = mapTransfusionProduct(it.product)
            )
        }, ExtractionEvaluation())
    }

    private fun mapTransfusionProduct(product: EhrBloodTransfusionProduct): String {
        return when (product) {
            EhrBloodTransfusionProduct.PLASMA_A, EhrBloodTransfusionProduct.PLASMA_B, EhrBloodTransfusionProduct.PLASMA_O, EhrBloodTransfusionProduct.PLASMA_AB, EhrBloodTransfusionProduct.APHERESIS_PLASMA -> "Plasma"
            EhrBloodTransfusionProduct.PLATELETS_POOLED, EhrBloodTransfusionProduct.PLATELETS_APHERESIS, EhrBloodTransfusionProduct.PLATELETS_POOLED_RADIATED -> "Trombocyte"
            EhrBloodTransfusionProduct.ERTHROCYTES_FILTERED, EhrBloodTransfusionProduct.ERYTHROCYTES_RADIATED -> "Erythrocytes"
            else -> "Other"
        }
    }
}