package com.hartwig.actin.trial.status.ctc

import com.hartwig.actin.trial.status.TrialStatus

object CTCStatusResolver {
    private val OPEN_STATES = setOf("Open").map { it.lowercase() }
    private val CLOSED_STATES =
        setOf("Gesloten", "Nog niet geopend", "Gesloten voor inclusie", "Onbekend", "Tijdelijk gesloten", "Closed").map { it.lowercase() }

    fun resolve(string: String): TrialStatus {
        return when (string.lowercase()) {
            in OPEN_STATES -> TrialStatus.OPEN
            in CLOSED_STATES -> TrialStatus.CLOSED

            else -> TrialStatus.UNINTERPRETABLE
        }

    }
}