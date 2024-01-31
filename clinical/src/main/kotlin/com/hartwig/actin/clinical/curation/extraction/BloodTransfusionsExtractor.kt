package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.clinical.datamodel.BloodTransfusion
import com.hartwig.actin.clinical.feed.digitalfile.DigitalFileEntry

class BloodTransfusionsExtractor(private val bloodFusionTranslations: TranslationDatabase<String>) {

    fun extract(patientId: String, entries: List<DigitalFileEntry>): ExtractionResult<List<BloodTransfusion>> {
        return entries.map { entry: DigitalFileEntry ->
            val transfusionProduct = entry.itemAnswerValueValueString
            val curationResponse = CurationResponse.createFromTranslation(
                bloodFusionTranslations.find(transfusionProduct),
                patientId,
                CurationCategory.BLOOD_TRANSFUSION_TRANSLATION,
                transfusionProduct,
                "blood transfusion with product"
            )
            val transfusion = BloodTransfusion(
                date = entry.authored,
                product = curationResponse.config()?.translated ?: transfusionProduct
            )
            ExtractionResult(transfusion, curationResponse.extractionEvaluation)
        }.fold(ExtractionResult(emptyList(), ExtractionEvaluation())) { acc, extractionResult ->
            ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
        }
    }

    companion object {
        fun create(curationDatabaseContext: CurationDatabaseContext) = BloodTransfusionsExtractor(curationDatabaseContext.bloodTransfusionTranslation)
    }
}