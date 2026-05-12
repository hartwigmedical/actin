package com.hartwig.actin.util

import io.github.oshai.kotlinlogging.KotlinLogging

class DatamodelPrinter(private val indentation: Int) {

    fun print(line: String) {
        logger.info { "${" ".repeat(indentation)}$line" }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        private const val DEFAULT_INDENTATION: Int = 1

        fun withDefaultIndentation(): DatamodelPrinter {
            return DatamodelPrinter(DEFAULT_INDENTATION)
        }
    }
}
