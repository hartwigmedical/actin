package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.LabMeasurementConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.provided.ProvidedLabUnit
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabUnit
import com.hartwig.actin.datamodel.clinical.LabValue
import com.hartwig.feed.datamodel.FeedLabValue
import com.hartwig.feed.datamodel.FeedPatientRecord

class StandardLabValuesExtractor(private val labMeasurementCuration: CurationDatabase<LabMeasurementConfig>) :
    StandardDataExtractor<List<LabValue>> {
    override fun extract(ehrPatientRecord: FeedPatientRecord): ExtractionResult<List<LabValue>> {
        return ehrPatientRecord.labValues.map { providedLabValue ->
            val inputText = "${providedLabValue.measureCode} | ${providedLabValue.measure}"
            val curationResponse = CurationResponse.createFromConfigs(
                labMeasurementCuration.find(inputText),
                ehrPatientRecord.patientDetails.patientId,
                CurationCategory.LAB_MEASUREMENT,
                inputText,
                "lab measurement",
                true
            )
            val labValue = curationResponse.config()?.takeIf { !it.ignore }?.let {
                labValue(providedLabValue, it.labMeasurement)
            }
            ExtractionResult(listOfNotNull(labValue), curationResponse.extractionEvaluation)
        }
            .fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, result ->
                ExtractionResult(acc.extracted + result.extracted, acc.evaluation + result.evaluation)
            }
    }

    private fun labValue(it: FeedLabValue, labMeasurement: LabMeasurement) = LabValue(
        date = it.date,
        measurement = labMeasurement,
        unit = labUnit(it),
        value = it.value,
        comparator = it.comparator ?: "",
        refLimitUp = it.refUpperBound,
        refLimitLow = it.refLowerBound
    )

    private fun labUnit(it: FeedLabValue): LabUnit {
        val labUnit = ProvidedLabUnit.fromString(it.unit)
        return when (labUnit) {
            ProvidedLabUnit.MILLIONS_PER_LITER -> LabUnit.MILLIONS_PER_LITER
            ProvidedLabUnit.MILLIONS_PER_MILLILITER -> LabUnit.MILLIONS_PER_MILLILITER
            ProvidedLabUnit.BILLIONS_PER_LITER -> LabUnit.BILLIONS_PER_LITER
            ProvidedLabUnit.TRILLIONS_PER_LITER -> LabUnit.TRILLIONS_PER_LITER
            ProvidedLabUnit.OTHER -> LabUnit.NONE
            else -> LabUnit.valueOf(ProvidedLabUnit.fromString(it.unit).name)
        }
    }
}