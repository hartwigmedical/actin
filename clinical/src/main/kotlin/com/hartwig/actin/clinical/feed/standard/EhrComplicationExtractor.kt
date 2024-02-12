package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.ComplicationConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.datamodel.Complication

class EhrComplicationExtractor(private val complicationCuration: CurationDatabase<ComplicationConfig>) : EhrExtractor<List<Complication>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<Complication>> {
        return ehrPatientRecord.complications.map {
            val curatedComplication = CurationResponse.createFromConfigs(
                complicationCuration.find(it.name),
                ehrPatientRecord.patientDetails.hashedIdBase64(),
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
        }.fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { acc, extractionResult ->
            ExtractionResult(acc.extracted + extractionResult.extracted, acc.evaluation + extractionResult.evaluation)
        }
    }
}
