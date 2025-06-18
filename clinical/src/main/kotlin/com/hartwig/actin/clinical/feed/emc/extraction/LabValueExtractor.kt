package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.LabMeasurementConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.emc.LabUnitResolver
import com.hartwig.actin.datamodel.clinical.LabValue
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.feed.datamodel.FeedLabValue

class LabValueExtractor(private val labMeasurementCuration: CurationDatabase<LabMeasurementConfig>) {

    fun extract(patientId: String, entries: List<FeedLabValue>): ExtractionResult<List<LabValue>> {
        return entries.map { entry ->
            val value = entry.value
            val isOutsideRef = if (entry.refLowerBound != null || entry.refUpperBound != null) {
                (entry.refLowerBound?.let { value < it } ?: false) ||
                        (entry.refUpperBound?.let { value > it } ?: false)
            } else null
            val inputText = "${entry.measureCode} | ${entry.measure}"
            val curationResponse = CurationResponse.createFromConfigs(
                labMeasurementCuration.find(inputText),
                patientId,
                CurationCategory.LAB_MEASUREMENT,
                inputText,
                "lab measurement",
                true
            )
            val curatedLab = curationResponse.config()?.takeIf { !it.ignore }?.let {
                LabValue(
                    measurement = it.labMeasurement,
                    date = entry.date,
                    comparator = entry.comparator
                        ?: throw IllegalStateException("Comparator missing for lab value $value for patient $patientId"),
                    value = value,
                    unit = LabUnitResolver.resolve(entry.unit),
                    refLimitLow = entry.refLowerBound,
                    refLimitUp = entry.refUpperBound,
                    isOutsideRef = isOutsideRef
                )
            }
            ExtractionResult(listOfNotNull(curatedLab), curationResponse.extractionEvaluation)
        }
            .fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, extractionResult ->
                ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
            }
    }

    companion object {
        fun create(curationDatabaseContext: CurationDatabaseContext) =
            LabValueExtractor(labMeasurementCuration = curationDatabaseContext.labMeasurementCuration)
    }
}