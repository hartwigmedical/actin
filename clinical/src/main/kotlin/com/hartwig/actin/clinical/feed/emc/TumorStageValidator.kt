package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.clinical.feed.emc.questionnaire.ValidatedQuestionnaireCuration
import com.hartwig.actin.datamodel.clinical.TumorStage
import com.hartwig.actin.datamodel.clinical.ingestion.QuestionnaireCurationError

object TumorStageValidator {

    fun validate(patientId: String, input: String?): ValidatedQuestionnaireCuration<TumorStage> {
        val stage = input?.let { TumorStageResolver.resolve(it) }

        val errors = if (input.isNullOrEmpty() || stage != null) {
            emptyList()
        } else {
            listOf(QuestionnaireCurationError(patientId, "Unrecognized questionnaire tumor stage: '$input'"))
        }

        return ValidatedQuestionnaireCuration(stage, errors)
    }

}