package com.hartwig.actin.molecular.util

import com.hartwig.actin.molecular.datamodel.MolecularHistory

class MolecularHistoryPrinter() {

    fun print(molecularHistory: MolecularHistory) {
        // TODO (kz): print all records in history
        molecularHistory.latestOrangeMolecularRecord()?.let(MolecularRecordPrinter::printRecord)
    }

    companion object {
        fun printRecord(molecularHistory: MolecularHistory) {
            MolecularHistoryPrinter().print(molecularHistory)
        }
    }
}