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
            val ehrLabValue = labValue(it)
            if (translation == null) {
                val warning = CurationWarning(
                    patientId = ehrPatientRecord.patientDetails.hashedId,
                    category = CurationCategory.LABORATORY_TRANSLATION,
                    feedInput = it.measureCode,
                    message = "Could not find laboratory translation for lab value with code '${it.measureCode}' and name '${it.measure}'"
                )
                ExtractionResult(
                    listOf(ehrLabValue), CurationExtractionEvaluation(warnings = setOf(warning))
                )
            } else {
                val newLabValue = ehrLabValue.copy(name = translation.translated.name, code = translation.translated.code)
                ExtractionResult(listOf(newLabValue), CurationExtractionEvaluation(laboratoryEvaluatedInputs = setOf(translation)))
            }
        }
            .fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, result ->
                ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
            }

    }

    private fun labValue(it: EhrLabValue) = LabValue(
        date = it.evaluationTime.toLocalDate(),
        name = it.measure,
        unit = labUnit(it),
        value = it.value,
        code = it.measureCode,
        comparator = it.comparator ?: "",
        refLimitUp = it.refUpperBound,
        refLimitLow = it.refLowerBound
    )

    private fun labUnit(it: EhrLabValue): LabUnit {
        val labUnit = EhrLabUnit.fromString(it.unit)
        return when (labUnit) {
            EhrLabUnit.MILLIONS_PER_LITER -> LabUnit.MILLIONS_PER_LITER
            EhrLabUnit.MILLIONS_PER_MILLILITER -> LabUnit.MILLIONS_PER_MILLILITER
            EhrLabUnit.BILLIONS_PER_LITER -> LabUnit.BILLIONS_PER_LITER
            EhrLabUnit.TRILLIONS_PER_LITER -> LabUnit.TRILLIONS_PER_LITER
            EhrLabUnit.OTHER -> LabUnit.NONE
            else -> LabUnit.valueOf(EhrLabUnit.fromString(it.unit).name)
        }
    }
}