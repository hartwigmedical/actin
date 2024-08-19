package com.hartwig.actin.molecular.util

import com.hartwig.actin.molecular.datamodel.MolecularHistory
import org.apache.logging.log4j.LogManager

class MolecularHistoryPrinter() {

    fun print(molecularHistory: MolecularHistory) {
        // TODO (kz): print all records in history
        LOGGER.info("Printing molecular history")
       // molecularHistory.latestOrangeMolecularRecord()?.let(MolecularRecordPrinter::printRecord)
    }

    companion object {
        private val LOGGER = LogManager.getLogger(MolecularHistoryPrinter::class.java)

        fun printRecord(molecularHistory: MolecularHistory) {
            MolecularHistoryPrinter().print(molecularHistory)
        }
    }
}