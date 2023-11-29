package com.hartwig.actin.trial.ctc

import org.apache.logging.log4j.LogManager

internal enum class CTCStatus {
    OPEN, CLOSED, UNINTERPRETABLE;

    companion object {
        private val LOGGER = LogManager.getLogger(CTCStatus::class.java)

        private val OPEN_STATES = setOf("Open")
        private val CLOSED_STATES = setOf("Gesloten", "Nog niet geopend", "Gesloten voor inclusie", "Onbekend", "Tijdelijk gesloten")

        fun fromStatusString(string: String): CTCStatus {
            return when {
                OPEN_STATES.any { it.equals(string, ignoreCase = true) } -> OPEN

                CLOSED_STATES.any { it.equals(string, ignoreCase = true) } -> CLOSED

                else -> {
                    UNINTERPRETABLE
                }
            }
        }
    }
}