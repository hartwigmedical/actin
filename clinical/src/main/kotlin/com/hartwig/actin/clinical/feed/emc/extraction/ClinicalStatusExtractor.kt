package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.config.ComorbidityConfig
import com.hartwig.actin.clinical.curation.config.CurationConfig
import com.hartwig.actin.clinical.feed.emc.questionnaire.Questionnaire
import com.hartwig.actin.datamodel.clinical.ClinicalStatus
import com.hartwig.actin.datamodel.clinical.OtherCondition

class ClinicalStatusExtractor(
    private val comorbidityCuration: CurationDatabase<ComorbidityConfig>
) {

    fun extract(questionnaire: Questionnaire?, curatedInfection: OtherCondition?, hasComplications: Boolean): ClinicalStatus {
        return questionnaire?.let {
            ClinicalStatus(
                who = questionnaire.whoStatus,
                infectionStatus = questionnaire.infectionStatus?.copy(description = curatedInfection?.name),
                lvef = determineLVEF(questionnaire.nonOncologicalHistory),
                hasComplications = hasComplications
            )
        } ?: ClinicalStatus()
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