package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.curation.translation.LaboratoryIdentifiers
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.datamodel.LabValue

class EhrLabValuesExtractor(private val labTranslation: TranslationDatabase<LaboratoryIdentifiers>) : EhrExtractor<List<LabValue>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<LabValue>> {
        return ehrPatientRecord.labValues.map {
            val translation = labTranslation.find(LaboratoryIdentifiers(it.measureCode, it.measure))
            if (translation == null) {
                val warning = CurationWarning(
                    patientId = ehrPatientRecord.patientDetails.patientId,
                    category = CurationCategory.LABORATORY_TRANSLATION,
                    feedInput = it.measureCode,
                    message = "Could not find laboratory translation for lab value with code '${it.measureCode}' and name '${it.measure}'"
                )
                ExtractionResult(emptyList(), CurationExtractionEvaluation(warnings = setOf(warning)))
            } else {
                val newLabValue = LabValue(
                    date = it.evaluationTime.toLocalDate(),
                    name = translation.translated.name,
                    unit = LabUnit.valueOf(EhrLabUnit.fromString(it.unit).name),
                    value = it.value,
                    code = translation.translated.code,
                    comparator = it.comparator ?: "="
                )
                ExtractionResult(listOf(newLabValue), CurationExtractionEvaluation(laboratoryEvaluatedInputs = setOf(translation)))
            }
        }
            .fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, result ->
                ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
            }

    }
}