package com.hartwig.actin.clinical.feed.emc.questionnaire

import com.hartwig.actin.datamodel.clinical.ingestion.QuestionnaireCurationError

data class ValidatedQuestionnaireCuration<T>(val curated: T?, val errors: List<QuestionnaireCurationError> = emptyList())