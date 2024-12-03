package com.hartwig.actin.medication

import com.hartwig.actin.datamodel.clinical.Cyp
import com.hartwig.actin.datamodel.clinical.Transporter

object MedicationInputChecker {

    fun isCyp(string: String): Boolean {
        return enumValues<Cyp>().any { it.name == string }
    }

    fun isTransporter(string: String): Boolean {
        return enumValues<Transporter>().any { it.name == string }
    }
}