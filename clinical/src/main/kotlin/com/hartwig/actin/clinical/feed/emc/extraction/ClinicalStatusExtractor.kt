package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.config.ComorbidityConfig
import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.emc.questionnaire.Questionnaire
import com.hartwig.actin.datamodel.clinical.ClinicalStatus

class ClinicalStatusExtractor(
    private val comorbidityCuration: CurationDatabase<ComorbidityConfig>
) {

    fun extract(questionnaire: Questionnaire?, hasComplications: Boolean): ExtractionResult<ClinicalStatus> {
        val clinicalStatus = questionnaire?.let {
            ClinicalStatus(
                who = questionnaire.whoStatus,
                lvef = determineLVEF(questionnaire.nonOncologicalHistory),
                hasComplications = hasComplications
            )
        } ?: ClinicalStatus()

        return ExtractionResult(clinicalStatus, CurationExtractionEvaluation())
    }

    private fun determineLVEF(nonOncologicalHistoryEntries: List<String>?): Double? {
        // We do not raise warnings or propagate evaluated inputs here since we use the same configs for otherConditions
        return nonOncologicalHistoryEntries?.asSequence()
            ?.flatMap(comorbidityCuration::find)
            ?.filterNot(CurationConfig::ignore)
            ?.firstNotNullOfOrNull { it.lvef }
    }

    companion object {
        fun create(curationDatabaseContext: CurationDatabaseContext) =
            ClinicalStatusExtractor(comorbidityCuration = curationDatabaseContext.comorbidityCuration)
    }
}