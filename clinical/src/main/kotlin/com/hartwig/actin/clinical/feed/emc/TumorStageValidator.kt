package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.clinical.feed.emc.questionnaire.ValidatedQuestionnaireCuration
import com.hartwig.actin.datamodel.clinical.TumorStage
import com.hartwig.actin.datamodel.clinical.ingestion.QuestionnaireCurationError

object TumorStageValidator {

    fun validate(patientId: String, input: String?): ValidatedQuestionnaireCuration<TumorStage> {
        if (input.isNullOrEmpty()) {
            return ValidatedQuestionnaireCuration(null)
        }
        if (!TumorStageResolver.isValid(input)) {
            return ValidatedQuestionnaireCuration(
                null,
                listOf(QuestionnaireCurationError(patientId, "Unrecognized questionnaire tumor stage: '$input'"))
            )
        }
        return ValidatedQuestionnaireCuration(TumorStageResolver.resolve(input))
    }
}