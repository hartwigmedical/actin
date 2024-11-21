package com.hartwig.actin.clinical.correction

import com.hartwig.actin.util.Paths
import org.apache.logging.log4j.LogManager
import java.io.File

class QuestionnaireRawEntryMapper(private val correctionMap: Map<String, String>) {

    tailrec fun correctQuestionnaireEntry(
        questionnaireText: String,
        keyIterator: Iterator<String> = correctionMap.keys.iterator(),
        foundStrings: Set<String> = emptySet()
    ): QuestionnaireCorrectionResult {
        if (!keyIterator.hasNext()) {
            return QuestionnaireCorrectionResult(questionnaireText, foundStrings)
        }
        val key = keyIterator.next()
        val found = questionnaireText.contains(key)
        return correctQuestionnaireEntry(
            if (found) questionnaireText.replace(key, correctionMap[key]!!) else questionnaireText,
            keyIterator, if (found) foundStrings + key else foundStrings
        )
    }

    fun evaluate(usedCorrections: Set<String>) {
        (correctionMap.keys - usedCorrections).forEach { LOGGER.warn(" Questionnaire correction key '{}' not used", it) }
    }

    companion object {
        private val LOGGER = LogManager.getLogger(QuestionnaireRawEntryMapper::class.java)

        private const val QUESTIONNAIRE_MAPPING_TSV = "questionnaire_mapping.tsv"
        private const val DELIMITER = "\t"
        private const val HEADER_ORIGINAL = "original"
        private const val HEADER_CORRECTED = "corrected"

        fun createFromCurationDirectory(curationDirectory: String): QuestionnaireRawEntryMapper {
            val filePath = Paths.forceTrailingFileSeparator(curationDirectory) + QUESTIONNAIRE_MAPPING_TSV
            val lines = File(filePath).readLines()
            if (lines.isEmpty() || lines[0].split(DELIMITER, limit = 2) != listOf(HEADER_ORIGINAL, HEADER_CORRECTED)) {
                throw IllegalStateException("File must start with tab-separated headers '$HEADER_ORIGINAL' and '$HEADER_CORRECTED'")
            }

            val correctionMap = lines.drop(1).map { split(it) }.associate { it[0] to it[1] }
            return QuestionnaireRawEntryMapper(correctionMap)
        }

        private fun split(line: String): List<String> {
            return line.split(DELIMITER, limit = 2)
        }
    }
}