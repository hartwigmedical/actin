package com.hartwig.actin.clinical.feed.questionnaire

internal class LesionData(private val present: Boolean?, private val active: Boolean?) {
    fun present(): Boolean? {
        return present
    }

    fun active(): Boolean? {
        return active
    }

    companion object {
        fun fromString(presentInput: String, activeInput: String): LesionData {
            val present = QuestionnaireCuration.toOption(presentInput)
            var active: Boolean? = null
            if (present != null) {
                val activeOption = QuestionnaireCuration.toOption(activeInput)
                if (activeOption != null) {
                    active = if (present) activeOption else false
                }
            }
            return LesionData(present, active)
        }
    }
}