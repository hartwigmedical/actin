package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.sort.LabValueDescendingDateComparator

class LabValueExtractor(private val curation: CurationDatabase) {

    fun extract(patientId: String, rawValues: List<LabValue>): ExtractionResult<List<LabValue>> {
        val extractedValues = rawValues.map { input ->
                val trimmedName = input.name().trim { it <= ' ' }
                val translation = curation.translateLabValue(input.code(), trimmedName)
            if (translation == null) {
                val warning = CurationWarning(
                    patientId = patientId,
                    category = CurationCategory.LABORATORY_TRANSLATION,
                    feedInput = input.code(),
                    message = "Could not find laboratory translation for lab value with code '${input.code()}' and name '$trimmedName'"
                )
                ExtractionResult(emptyList(), ExtractionEvaluation(warnings = setOf(warning)))
            } else {
                val newLabValue = ImmutableLabValue.builder()
                    .from(input)
                    .code(translation.translatedCode)
                    .name(translation.translatedName)
                    .build()
                ExtractionResult(listOf(newLabValue), ExtractionEvaluation(laboratoryEvaluatedInputs = setOf(translation)))
            }
            }
            .fold(ExtractionResult(emptyList<LabValue>(), ExtractionEvaluation())) { acc, result ->
                ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
            }

        return extractedValues.copy(extracted = extractedValues.extracted.sortedWith(LabValueDescendingDateComparator()))
    }
}