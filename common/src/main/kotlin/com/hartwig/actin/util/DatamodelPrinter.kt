package com.hartwig.actin.util

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class DatamodelPrinter(private val indentation: Int) {

    fun print(line: String) {
        LOGGER.info("{}{}", " ".repeat(indentation), line)
    }

    companion object {
        private val LOGGER: Logger = LogManager.getLogger(DatamodelPrinter::class.java)
        private const val DEFAULT_INDENTATION: Int = 1

        fun withDefaultIndentation(): DatamodelPrinter {
            return DatamodelPrinter(DEFAULT_INDENTATION)
        }
    }
}
