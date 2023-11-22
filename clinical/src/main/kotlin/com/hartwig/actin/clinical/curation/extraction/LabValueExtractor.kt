package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.feed.lab.LabEntry
import com.hartwig.actin.clinical.feed.lab.LabExtraction
import com.hartwig.actin.clinical.sort.LabValueDescendingDateComparator

class LabValueExtractor(private val curation: CurationDatabase) {

    fun extract(patientId: String, labEntries: List<LabEntry>): ExtractionResult<List<LabValue>> {
        val extractedValues = labEntries.map { LabExtraction.extract(it) }
            .map { input ->
                val trimmedName = input.name().trim { it <= ' ' }
                val translation = curation.translateLabValue(input.code(), trimmedName)
                val translationResponse = CurationResponse.create(
                    CurationCategory.LABORATORY_TRANSLATION,
                    input.code(),
                    setOfNotNull(translation),
                    translation?.let { emptySet() } ?: setOf(
                        CurationWarning(
                            patientId = patientId,
                            category = CurationCategory.LABORATORY_TRANSLATION,
                            feedInput = input.code(),
                            message = "Could not find laboratory translation for lab value with code '${input.code()}' and name '$trimmedName'"
                        )
                    )
                )
                val newLabValue = translation?.let {
                    ImmutableLabValue.builder().from(input).code(translation.translatedCode).name(translation.translatedName).build()
                } ?: input
                ExtractionResult(listOf(newLabValue), translationResponse.extractionEvaluation)
            }
            .fold(ExtractionResult(emptyList<LabValue>(), ExtractionEvaluation())) { acc, result ->
                ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
            }

        return extractedValues.copy(extracted = extractedValues.extracted.sortedWith(LabValueDescendingDateComparator()))
    }
}