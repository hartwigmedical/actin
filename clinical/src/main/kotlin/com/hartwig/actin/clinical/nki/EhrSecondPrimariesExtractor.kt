package com.hartwig.actin.clinical.nki

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.TumorStatus

class EhrSecondPrimariesExtractor(private val tumorStatusTranslation: TranslationDatabase<String>) :
    EhrExtractor<List<PriorSecondPrimary>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<PriorSecondPrimary>> {
        return ehrPatientRecord.priorPrimaries.map {
            val statusTranslation = CurationResponse.createFromTranslation(
                tumorStatusTranslation.find(it.statusDetails),
                ehrPatientRecord.patientDetails.patientId,
                CurationCategory.SECOND_PRIMARY,
                it.statusDetails,
                "tumor status"
            )
            ExtractionResult(listOfNotNull(statusTranslation.config()?.let { t ->
                ImmutablePriorSecondPrimary.builder().tumorLocation(it.tumorLocalization).tumorType(it.tumorTypeDetails)
                    .status(TumorStatus.valueOf(t.translated)).diagnosedYear(it.diagnosisDate.year)
                    .diagnosedMonth(it.diagnosisDate.monthValue)
                    .tumorSubLocation("")
                    .tumorSubType("")
                    .treatmentHistory("")
                    .build()
            }), statusTranslation.extractionEvaluation)
        }.fold(ExtractionResult(emptyList(), ExtractionEvaluation())) { acc, extractionResult ->
            ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
        }
    }
}