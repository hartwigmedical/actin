package com.hartwig.actin.datamodel.trial

import com.hartwig.actin.datamodel.Displayable

enum class TrialPhase(val text: String, val isLatePhase: Boolean, private val includePrefix: Boolean = true) : Displayable {
    PHASE_4("4", true),
    PHASE_3("3", true),
    PHASE_2_3("2/3", true),
    PHASE_2_OR_MORE("≥2", true),
    PHASE_2("2", true),
    PHASE_1_2("1/2", false),
    PHASE_1_OR_MORE("1, ≥2", false),
    PHASE_1("1", false),
    COMPASSIONATE_USE("Compassionate Use", true, includePrefix = false);

    override fun display(): String {
        return if (includePrefix) "Phase $text" else text
    }

    companion object {
        private val phasesByName = entries.associateBy { it.text }

        fun fromString(input: String): TrialPhase? {
            return phasesByName[input]
        }
    }
}