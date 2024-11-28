package com.hartwig.actin.medication

class MedicationInputChecker {

    companion object {
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
            return listOf("OATP1B1", "OATP1B3", "PGP", "OCT2", "MATE1", "BCRP").contains(string)
        }
    }
}