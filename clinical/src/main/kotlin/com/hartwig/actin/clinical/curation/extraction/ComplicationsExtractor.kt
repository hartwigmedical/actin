package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.config.ComplicationConfig
import com.hartwig.actin.clinical.datamodel.Complication
import com.hartwig.actin.clinical.feed.questionnaire.Questionnaire

class ComplicationsExtractor(private val complicationCuration: CurationDatabase<ComplicationConfig>) {

    fun extract(patientId: String, questionnaire: Questionnaire?): ExtractionResult<List<Complication>?> {
        if (questionnaire?.complications.isNullOrEmpty()) {
            return ExtractionResult(null, ExtractionEvaluation())
        }
        val (curation, validInputCount, unknownStateCount) = questionnaire!!.complications!!
            .map { CurationUtil.fullTrim(it) }
            .map {
                val configs = complicationCuration.curate(it)
                CurationResponse.createFromConfigs(
                    configs,
                    patientId,
                    CurationCategory.COMPLICATION,
                    it,
                    "complication"
                )
            }
            .map {
                Triple(
                    it,
                    if (it.configs.isNotEmpty()) 1 else 0,
                    if (it.configs.any { c -> c.impliesUnknownComplicationState == true }) 1 else 0
                )
            }
            .fold(Triple(CurationResponse<ComplicationConfig>(), 0, 0)) { acc, cur ->
                Triple(acc.first + cur.first, acc.second + cur.second, acc.third + cur.third)
            }

        // If there are complications but every single one of them implies an unknown state, return null
        val curated = if (unknownStateCount == validInputCount) null else curation.configs.mapNotNull(ComplicationConfig::curated)
        return ExtractionResult(curated, curation.extractionEvaluation)
    }

}