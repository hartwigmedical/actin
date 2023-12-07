package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.translation.BloodTransfusionTranslationFactory
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.clinical.curation.translation.TranslationDatabaseReader
import com.hartwig.actin.clinical.datamodel.BloodTransfusion
import com.hartwig.actin.clinical.datamodel.ImmutableBloodTransfusion
import com.hartwig.actin.clinical.feed.digitalfile.DigitalFileEntry

class BloodTransfusionsExtractor(private val bloodFusionTranslations: TranslationDatabase<String>) {

    fun extract(patientId: String, entries: List<DigitalFileEntry>): ExtractionResult<List<BloodTransfusion>> {
        return entries.map { entry: DigitalFileEntry ->
            val transfusionProduct = entry.itemAnswerValueValueString
            val curationResponse = CurationResponse.createFromTranslation(
                bloodFusionTranslations.translate(transfusionProduct),
                patientId,
                CurationCategory.BLOOD_TRANSFUSION_TRANSLATION,
                transfusionProduct,
                "blood transfusion with product"
            )
            val transfusion = ImmutableBloodTransfusion.builder()
                .date(entry.authored)
                .product(curationResponse.config()?.translated ?: transfusionProduct)
                .build()
            ExtractionResult(transfusion, curationResponse.extractionEvaluation)
        }.fold(ExtractionResult(emptyList(), ExtractionEvaluation())) { acc, extractionResult ->
            ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
        }
    }

    companion object {
        fun create(curationDir: String) =
            BloodTransfusionsExtractor(
                TranslationDatabaseReader.read(
                    curationDir,
                    TranslationDatabaseReader.BLOOD_TRANSFUSION_TRANSLATION_TSV,
                    BloodTransfusionTranslationFactory()
                )
            )
    }
}