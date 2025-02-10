package com.hartwig.actin.clinical.feed.emc.questionnaire

internal class LesionData(private val present: Boolean?, private val active: Boolean?) {
    fun present(): Boolean? {
        return present
    }

    fun active(): Boolean? {
        return active
    }

    companion object {
        fun fromString(subject: String, presentInput: String, activeInput: String): ValidatedQuestionnaireCuration<LesionData> {
            val present = curateQuestionnaireOption(subject, presentInput)
            val active = if (present.curated != null) curateQuestionnaireOption(subject, activeInput)
            else ValidatedQuestionnaireCuration(null)

            return ValidatedQuestionnaireCuration(
                LesionData(present.curated, active.curated),
                present.errors + active.errors
            )
        }

        private fun curateQuestionnaireOption(subject: String, input: String): ValidatedQuestionnaireCuration<Boolean> =
            ValidatedQuestionnaireCuration(QuestionnaireCuration.toBoolean(subject, input).curated)

    }
}