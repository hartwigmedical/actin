package com.hartwig.actin.clinical.feed.questionnaire

import com.hartwig.actin.util.Paths
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.stream.Collectors

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

        @JvmStatic
        @Throws(IOException::class)
        fun createFromCurationDirectory(curationDirectory: String?): QuestionnaireRawEntryMapper {
            val filePath = Path.of(Paths.forceTrailingFileSeparator(curationDirectory!!) + QUESTIONNAIRE_MAPPING_TSV)
            Files.lines(filePath).use { fileStream ->
                val correctionMap: Map<String, String> = fileStream.map { line: String -> splitAndParseLineBreaks(line) }
                    .collect(
                        { HashMap() },
                        { m: HashMap<String, String>, cols: List<String> ->
                            m[cols[0]] = cols[1]
                        }) { obj: HashMap<String, String>, m: HashMap<String, String>? ->
                        obj.putAll(
                            m!!
                        )
                    }
                return QuestionnaireRawEntryMapper(correctionMap)
            }
        }

        private fun splitAndParseLineBreaks(line: String): List<String> {
            return Arrays.stream(line.split("\t".toRegex(), limit = 2).toTypedArray()).map { entry: String -> entry.replace("\\n", "\n") }
                .collect(Collectors.toList())
        }
    }
}