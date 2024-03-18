package com.hartwig.actin.trial.status.ctc

import com.hartwig.actin.trial.status.TrialStatus

object CTCStatusResolver {
    private val OPEN_STATES = setOf("Open")
    private val CLOSED_STATES = setOf("Gesloten", "Nog niet geopend", "Gesloten voor inclusie", "Onbekend", "Tijdelijk gesloten", "Closed")

    fun resolve(string: String): TrialStatus {
        return when {
            OPEN_STATES.any { it.equals(string, ignoreCase = true) } -> TrialStatus.OPEN

            CLOSED_STATES.any { it.equals(string, ignoreCase = true) } -> TrialStatus.CLOSED

            else -> {
                TrialStatus.UNINTERPRETABLE
            }
        }

    }
}