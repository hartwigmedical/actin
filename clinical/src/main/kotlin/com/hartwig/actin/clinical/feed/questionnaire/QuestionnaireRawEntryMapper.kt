package com.hartwig.actin.clinical.feed.questionnaire

import com.hartwig.actin.util.Paths
import java.io.File
import java.io.IOException

class QuestionnaireRawEntryMapper(private val correctionMap: Map<String, String>) {
    fun correctQuestionnaireEntry(rawQuestionnaireText: String): String {
        var correctedQuestionnaireText = rawQuestionnaireText
        for ((key, value) in correctionMap) {
            correctedQuestionnaireText = correctedQuestionnaireText.replace(key, value)
        }
        return correctedQuestionnaireText
    }

    companion object {
        private const val QUESTIONNAIRE_MAPPING_TSV = "questionnaire_mapping.tsv"
        private const val DELIMITER = "\t"
        private const val HEADER_ORIGINAL = "original"
        private const val HEADER_CORRECTED = "corrected"

        @Throws(IOException::class)
        fun createFromCurationDirectory(curationDirectory: String): QuestionnaireRawEntryMapper {
            val filePath = Paths.forceTrailingFileSeparator(curationDirectory) + QUESTIONNAIRE_MAPPING_TSV
            val lines = File(filePath).readLines()
            if (lines.isEmpty() || lines[0].split(DELIMITER, limit = 2) != listOf(HEADER_ORIGINAL, HEADER_CORRECTED)) {
                throw IllegalStateException("File must start with tab-separated headers '$HEADER_ORIGINAL' and '$HEADER_CORRECTED'")
            }

            val correctionMap = lines.drop(1).map { splitAndParseLineBreaks(it) }.associate { it[0] to it[1] }
            return QuestionnaireRawEntryMapper(correctionMap)
        }

        private fun splitAndParseLineBreaks(line: String): List<String> {
            return line.split(DELIMITER, limit = 2).map { it.replace("\\n", "\n") }
        }
    }
}