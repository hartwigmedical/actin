package com.hartwig.actin.molecular.util

import com.hartwig.actin.datamodel.molecular.MolecularHistory
import org.apache.logging.log4j.LogManager

object MolecularHistoryPrinter {

    private val logger = LogManager.getLogger(MolecularHistoryPrinter::class.java)

    fun print(molecularHistory: MolecularHistory) {
        logger.info("Printing molecular history")

        molecularHistory.allOrangeMolecularRecords().map(MolecularTestPrinter.Companion::printOrangeRecord)
        molecularHistory.allPanels().map(MolecularTestPrinter.Companion::printPanelRecord)
    }
}