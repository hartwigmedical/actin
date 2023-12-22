package com.hartwig.actin.clinical.feed.questionnaire

internal class LesionData(private val present: Boolean?, private val active: Boolean?) {
    fun present(): Boolean? {
        return present
    }

    fun active(): Boolean? {
        return active
    }

    companion object {
        fun fromString(presentInput: String, activeInput: String): ValidatedQuestionnaireCuration<LesionData> {
            val present = curateQuestionnaireOption(presentInput)
            val active = if (present.curated != null) curateQuestionnaireOption(activeInput)
            else ValidatedQuestionnaireCuration(null)

            return ValidatedQuestionnaireCuration(
                LesionData(present.curated, active.curated),
                present.errors + active.errors
            )
        }

        private fun curateQuestionnaireOption(input: String): ValidatedQuestionnaireCuration<Boolean> =
            ValidatedQuestionnaireCuration(QuestionnaireCuration.toOption(input).curated)

    }
}