package com.hartwig.actin.clinical.feed.questionnaire

import com.google.common.collect.Lists
import org.apache.logging.log4j.LogManager

class QuestionnaireExtraction(private val questionnaireRawEntryMapper: QuestionnaireRawEntryMapper) {
    fun extract(entry: QuestionnaireEntry?): Questionnaire? {
        if (entry == null || !isActualQuestionnaire(entry)) {
            return null
        }
        val mapping = QuestionnaireMapping.mapping(entry)
        val lines = QuestionnaireReader.read(
            questionnaireRawEntryMapper.correctQuestionnaireEntry(entry.text),
            QuestionnaireMapping.keyStrings(entry)
        )
        val brainLesionData = lesionData(lines, mapping[QuestionnaireKey.HAS_BRAIN_LESIONS]!!)
        val cnsLesionData = lesionData(lines, mapping[QuestionnaireKey.HAS_CNS_LESIONS]!!)
        return Questionnaire(
            date = entry.authored,
            tumorLocation = value(lines, mapping[QuestionnaireKey.PRIMARY_TUMOR_LOCATION]),
            tumorType = if (QuestionnaireVersion.version(entry) == QuestionnaireVersion.V0_1) "Unknown" else value(
                lines,
                mapping[QuestionnaireKey.PRIMARY_TUMOR_TYPE]
            ),
            biopsyLocation = value(lines, mapping[QuestionnaireKey.BIOPSY_LOCATION]),
            stage = QuestionnaireCuration.toStage(value(lines, mapping[QuestionnaireKey.STAGE])),
            treatmentHistoryCurrentTumor = toList(value(lines, mapping[QuestionnaireKey.TREATMENT_HISTORY_CURRENT_TUMOR])),
            otherOncologicalHistory = toList(value(lines, mapping[QuestionnaireKey.OTHER_ONCOLOGICAL_HISTORY])),
            secondaryPrimaries = secondaryPrimaries(lines, mapping[QuestionnaireKey.SECONDARY_PRIMARY]),
            nonOncologicalHistory = toList(value(lines, mapping[QuestionnaireKey.NON_ONCOLOGICAL_HISTORY])),
            ihcTestResults = toList(value(lines, mapping[QuestionnaireKey.IHC_TEST_RESULTS])),
            pdl1TestResults = toList(value(lines, mapping[QuestionnaireKey.PDL1_TEST_RESULTS])),
            hasMeasurableDisease = QuestionnaireCuration.toOption(value(lines, mapping[QuestionnaireKey.HAS_MEASURABLE_DISEASE])),
            hasBrainLesions = brainLesionData.present(),
            hasActiveBrainLesions = brainLesionData.active(),
            hasCnsLesions = cnsLesionData.present(),
            hasActiveCnsLesions = cnsLesionData.active(),
            hasBoneLesions = QuestionnaireCuration.toOption(value(lines, mapping[QuestionnaireKey.HAS_BONE_LESIONS])),
            hasLiverLesions = QuestionnaireCuration.toOption(value(lines, mapping[QuestionnaireKey.HAS_LIVER_LESIONS])),
            otherLesions = otherLesions(entry, lines, mapping),
            whoStatus = QuestionnaireCuration.toWHO(value(lines, mapping[QuestionnaireKey.WHO_STATUS])),
            unresolvedToxicities = toList(value(lines, mapping[QuestionnaireKey.UNRESOLVED_TOXICITIES])),
            infectionStatus =
            QuestionnaireCuration.toInfectionStatus(
                value(
                    lines,
                    mapping[QuestionnaireKey.SIGNIFICANT_CURRENT_INFECTION]
                )
            ),
            ecg = QuestionnaireCuration.toECG(value(lines, mapping[QuestionnaireKey.SIGNIFICANT_ABERRATION_LATEST_ECG])),
            complications = toList(value(lines, mapping[QuestionnaireKey.COMPLICATIONS])),
            genayaSubjectNumber = optionalValue(lines, mapping[QuestionnaireKey.GENAYA_SUBJECT_NUMBER])
        )
    }

    companion object {
        private val LOGGER = LogManager.getLogger(QuestionnaireExtraction::class.java)
        const val KEY_VALUE_SEPARATOR = ":"
        const val VALUE_LIST_SEPARATOR_1 = ","
        private const val VALUE_LIST_SEPARATOR_2 = ";"
        private const val ACTIN_QUESTIONNAIRE_KEYWORD = "ACTIN Questionnaire"
        private const val ACTIVE_LINE_OFFSET = 1

        @JvmStatic
        fun isActualQuestionnaire(entry: QuestionnaireEntry): Boolean {
            return entry.text.contains(ACTIN_QUESTIONNAIRE_KEYWORD)
        }

        private fun secondaryPrimaries(lines: Array<String>, secondaryPrimaryKey: String?): List<String>? {
            val extractedValues = if (secondaryPrimaryKey == null) null else values(lines, secondaryPrimaryKey, 1)
            return if (extractedValues == null || extractedValues[0].isEmpty()) {
                null
            } else QuestionnaireCuration.toSecondaryPrimaries(extractedValues[0], extractedValues[1])
        }

        private fun otherLesions(
            entry: QuestionnaireEntry, lines: Array<String>,
            mapping: Map<QuestionnaireKey, String?>
        ): List<String>? {
            val version: QuestionnaireVersion = QuestionnaireVersion.version(entry)
            return if (version == QuestionnaireVersion.V0_1) {
                //In v0.1, the format for primary tumor location is "$location ($otherLesions)"
                val input = value(lines, mapping[QuestionnaireKey.PRIMARY_TUMOR_LOCATION])
                if (input != null && input.contains("(") && input.contains(")")) {
                    val start = input.indexOf("(")
                    val end = input.indexOf(")")
                    toList(input.substring(start + 1, end))
                } else {
                    null
                }
            } else {
                toList(value(lines, mapping[QuestionnaireKey.OTHER_LESIONS]))
            }
        }

        private fun lesionData(lines: Array<String>, keyString: String): LesionData {
            val extractedValues = values(lines, keyString, ACTIVE_LINE_OFFSET)
            return if (extractedValues == null) LesionData(null, null) else LesionData.fromString(
                extractedValues[0],
                extractedValues[1]
            )
        }

        private fun toList(value: String?): List<String>? {
            if (value == null) {
                return null
            }
            val reformatted = value.replace(VALUE_LIST_SEPARATOR_2.toRegex(), VALUE_LIST_SEPARATOR_1)
            val split = reformatted.split(VALUE_LIST_SEPARATOR_1.toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            return cleanAndTrim(split)
        }

        private fun cleanAndTrim(values: Array<String>): List<String> {
            val trimmed: MutableList<String> = Lists.newArrayList()
            for (value in values) {
                val trim = value.trim { it <= ' ' }
                if (trim.isNotEmpty()) {
                    trimmed.add(trim)
                }
            }
            return trimmed
        }

        private fun optionalValue(lines: Array<String>, key: String?): String? {
            return value(lines, key, true)
        }

        private fun value(lines: Array<String>, key: String?, isOptional: Boolean = false): String? {
            val lineIndex = lookup(lines, key, isOptional) ?: return null
            val extracted = extractValue(lines[lineIndex])
            return extracted.ifEmpty { null }
        }

        private fun values(lines: Array<String>, key: String?, lineOffset: Int): List<String>? {
            val lineIndex = lookup(lines, key, false)
            if (lineIndex != null) {
                val extractedValues = lines.slice(lineIndex..lineIndex + lineOffset)
                    .map { line: String -> extractValue(line) }
                if (extractedValues.size < lineOffset + 1) {
                    throw RuntimeException(String.format("Failed to extract %d lines for key '%s'", lineOffset + 1, key))
                }
                return extractedValues
            }
            return null
        }

        private fun extractValue(line: String): String {
            return line.substring(line.indexOf(KEY_VALUE_SEPARATOR) + 1).trim { it <= ' ' }
        }

        private fun lookup(lines: Array<String>, key: String?, isOptional: Boolean): Int? {
            if (key == null) {
                return null
            }
            for (i in lines.indices) {
                if (lines[i].contains(key)) {
                    return i
                }
            }
            if (isOptional) {
                LOGGER.debug(
                    "Key '{}' not present but skipped since it is configured as optional in questionnaire '{}'", key,
                    java.lang.String.join("\n", *lines)
                )
                return null
            }
            throw IllegalStateException("Could not find key " + key + " in questionnaire " + java.lang.String.join("\n", *lines))
        }
    }
}