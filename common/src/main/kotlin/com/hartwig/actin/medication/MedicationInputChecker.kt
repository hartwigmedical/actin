package com.hartwig.actin.medication

import com.hartwig.actin.datamodel.clinical.Transporter

object MedicationInputChecker {

    fun isCyp(string: String): Boolean {
        if (string.length < 3 || string.length > 5) {
            return false
        }
        val hasValidStart = string[0].isDigit()
        val hasValidMid = string[1].isLetter()
        val hasValidEnd = string[string.length - 1].isDigit()
        return hasValidStart && hasValidMid && hasValidEnd
    }

    fun isTransporter(string: String): Boolean {
        return enumValues<Transporter>().any { it.name == string }
    }
}