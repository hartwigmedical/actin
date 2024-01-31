package com.hartwig.actin.clinical.feed.questionnaire

data class QuestionnaireCurationError(val subject: String, val message: String) : Comparable<QuestionnaireCurationError> {

    override fun compareTo(other: QuestionnaireCurationError): Int {
        return Comparator.comparing(QuestionnaireCurationError::subject)
            .thenComparing(QuestionnaireCurationError::message)
            .compare(this, other)
    }
}