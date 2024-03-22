package com.hartwig.actin.molecular.util

import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.util.DatamodelPrinter

class MolecularHistoryPrinter(private val printer: DatamodelPrinter) {

    fun print(molecularHistory: MolecularHistory) {
        // TODO (kz): print all records in history
        molecularHistory.mostRecentWGS()?.let(MolecularRecordPrinter::printRecord)
    }

    companion object {
        fun printRecord(molecularHistory: MolecularHistory) {
            MolecularHistoryPrinter(DatamodelPrinter.withDefaultIndentation()).print(molecularHistory)
        }
    }
}