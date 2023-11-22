package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.datamodel.BloodTransfusion
import com.hartwig.actin.clinical.datamodel.ImmutableBloodTransfusion
import com.hartwig.actin.clinical.feed.digitalfile.DigitalFileEntry

class BloodTransfusionsExtractor(private val curation: CurationDatabase) {

    fun extract(patientId: String, entries: List<DigitalFileEntry>): ExtractionResult<List<BloodTransfusion>> {
        return entries.map { entry: DigitalFileEntry ->
            val transfusionProduct = entry.itemAnswerValueValueString
            val curationResponse = CurationResponse.createFromTranslation(
                curation.translateBloodTransfusion(transfusionProduct),
                patientId,
                CurationCategory.BLOOD_TRANSFUSION_TRANSLATION,
                transfusionProduct,
                "blood transfusion product"
            )
            val transfusion = ImmutableBloodTransfusion.builder()
                .date(entry.authored)
                .product(curationResponse.config() ?: transfusionProduct)
                .build()
            ExtractionResult(transfusion, curationResponse.extractionEvaluation)
        }.fold(ExtractionResult(emptyList(), ExtractionEvaluation())) { acc, extractionResult ->
            ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
        }
    }

}