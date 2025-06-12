package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.datamodel.clinical.BloodTransfusion
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.feed.datamodel.FeedBloodTransfusion

class BloodTransfusionsExtractor(private val bloodFusionTranslations: TranslationDatabase<String>) {

    fun extract(patientId: String, entries: List<FeedBloodTransfusion>): ExtractionResult<List<BloodTransfusion>> {
        return entries.map { entry: FeedBloodTransfusion ->
            val transfusionProduct = entry.name
            val curationResponse = CurationResponse.createFromTranslation(
                bloodFusionTranslations.find(transfusionProduct),
                patientId,
                CurationCategory.BLOOD_TRANSFUSION_TRANSLATION,
                transfusionProduct,
                "blood transfusion with product"
            )
            val transfusion = BloodTransfusion(
                date = entry.evaluationDate,
                product = curationResponse.config()?.translated ?: transfusionProduct
            )
            ExtractionResult(transfusion, curationResponse.extractionEvaluation)
        }.fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, extractionResult ->
            ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
        }
    }

    companion object {
        fun create(curationDatabaseContext: CurationDatabaseContext) =
            BloodTransfusionsExtractor(curationDatabaseContext.bloodTransfusionTranslation)
    }
}