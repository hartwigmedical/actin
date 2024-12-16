package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.QtProlongatingDatabase
import com.hartwig.actin.datamodel.clinical.QTProlongatingRisk

object TestQtProlongatingFactory {

    fun createProper(
        medicationName: String = "",
        qtProlongatingRisk: QTProlongatingRisk = QTProlongatingRisk.UNKNOWN
    ): QtProlongatingDatabase {
        return QtProlongatingDatabase(mapOf(medicationName to qtProlongatingRisk))
    }
}