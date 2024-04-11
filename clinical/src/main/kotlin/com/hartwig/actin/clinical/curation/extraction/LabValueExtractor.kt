package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.translation.LaboratoryIdentifiers
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.sort.LabValueDescendingDateComparator

class LabValueExtractor(private val laboratoryTranslation: TranslationDatabase<LaboratoryIdentifiers>) {

    fun extract(patientId: String, rawValues: List<LabValue>): ExtractionResult<List<LabValue>> {
        val extractedValues = rawValues.map { input ->
            val trimmedName = input.name.trim { it <= ' ' }
            val translation = laboratoryTranslation.find(LaboratoryIdentifiers(input.code, trimmedName))
            if (translation == null) {
                val warning = CurationWarning(
                    patientId = patientId,
                    category = CurationCategory.LABORATORY_TRANSLATION,
                    feedInput = "$trimmedName | ${input.code}",
                    message = "Could not find laboratory translation for lab value with code '${input.code}' and name '$trimmedName'"
                )
                ExtractionResult(emptyList(), CurationExtractionEvaluation(warnings = setOf(warning)))
            } else {
                val newLabValue = input.copy(
                    code = translation.translated.code,
                    name = translation.translated.name
                )
                ExtractionResult(listOf(newLabValue), CurationExtractionEvaluation(laboratoryEvaluatedInputs = setOf(translation)))
            }
        }
            .fold(ExtractionResult(emptyList<LabValue>(), CurationExtractionEvaluation())) { acc, result ->
                ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
            }

        return extractedValues.copy(extracted = extractedValues.extracted.sortedWith(LabValueDescendingDateComparator()))
    }

    companion object {
        fun create(curationDatabaseContext: CurationDatabaseContext) =
            LabValueExtractor(laboratoryTranslation = curationDatabaseContext.laboratoryTranslation)
    }
}