package com.hartwig.actin.util

import com.google.common.base.Strings
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class DatamodelPrinter(private val indentation: Int) {

    fun print(line: String) {
        LOGGER.info("{}{}", Strings.repeat(" ", indentation), line)
    }

    companion object {
        private val LOGGER: Logger = LogManager.getLogger(DatamodelPrinter::class.java)
        private const val DEFAULT_INDENTATION: Int = 1

        @JvmStatic
        fun withDefaultIndentation(): DatamodelPrinter {
            return DatamodelPrinter(DEFAULT_INDENTATION)
        }
    }
}
