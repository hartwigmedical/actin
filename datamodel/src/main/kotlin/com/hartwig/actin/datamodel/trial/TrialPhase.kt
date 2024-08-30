package com.hartwig.actin.datamodel.trial

import com.hartwig.actin.datamodel.Displayable

enum class TrialPhase(private val text: String, private val includePrefix: Boolean = true) : Displayable {
    PHASE_4("4"),
    PHASE_3("3"),
    PHASE_2_3("2/3"),
    PHASE_2("2"),
    PHASE_1_2("1/2"),
    PHASE_1("1"),
    COMPASSIONATE_USE("Compassionate Use", includePrefix = false);

    override fun display(): String {
        return if (includePrefix) "Phase $text" else text
    }

    companion object {
        private val phasesByName = values().associateBy { it.text }

        fun fromString(input: String): TrialPhase? {
            return phasesByName[input]
        }
    }
}