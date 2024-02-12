package com.hartwig.actin.clinical.feed.emc.questionnaire

data class ValidatedQuestionnaireCuration<T>(val curated: T?, val errors: List<QuestionnaireCurationError> = emptyList())