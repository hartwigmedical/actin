package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.ComplicationConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.standard.ProvidedPatientRecord
import com.hartwig.actin.datamodel.clinical.Complication

class StandardComplicationExtractor(private val complicationCuration: CurationDatabase<ComplicationConfig>) :
    StandardDataExtractor<List<Complication>> {
    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<Complication>> {
        val complications = ehrPatientRecord.complications.map {
            val curatedComplication = CurationResponse.createFromConfigs(
                complicationCuration.find(it.name),
                ehrPatientRecord.patientDetails.hashedId,
                CurationCategory.COMPLICATION,
                it.name,
                "complication"
            )
            ExtractionResult(
                listOfNotNull(
                    curatedComplication.config()?.curated?.copy(
                        year = it.startDate.year,
                        month = it.startDate.monthValue
                    )
                ), curatedComplication.extractionEvaluation
            )
        }
        val complicationsFromPriorOtherConditions = ehrPatientRecord.priorOtherConditions.map {
            CurationResponse.createFromConfigs(
                complicationCuration.find(it.name),
                ehrPatientRecord.patientDetails.hashedId,
                CurationCategory.COMPLICATION,
                it.name,
                "complication"
            )
        }.map { ExtractionResult(it.configs.mapNotNull { c -> c.curated }, CurationExtractionEvaluation()) }
        return (complications + complicationsFromPriorOtherConditions).fold(
            ExtractionResult(
                emptyList(),
                CurationExtractionEvaluation()
            )
        ) { acc, extractionResult ->
            ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
        }
    }
}
