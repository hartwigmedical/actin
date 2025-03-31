package com.hartwig.actin.clinical.correction

import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireEntry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object QuestionnaireCorrection {

    val LOGGER: Logger = LogManager.getLogger(QuestionnaireCorrection::class.java)

    fun correctQuestionnaires(
        questionnaireEntries: List<QuestionnaireEntry>,
        rawEntryMapper: QuestionnaireRawEntryMapper
    ): List<QuestionnaireEntry> {
        val (correctedQuestionnaireEntries, foundKeys) = questionnaireEntries.map {
            val correctionResult = rawEntryMapper.correctQuestionnaireEntry(it.text)
            QuestionnairesAndCorrections(listOf(it.copy(text = correctionResult.correctedText)), correctionResult.foundKeys)
        }.reduce(QuestionnairesAndCorrections::plus)

        LOGGER.info("Evaluating questionnaire correction mapping")
        rawEntryMapper.evaluate(foundKeys)

        return correctedQuestionnaireEntries
    }

    private data class QuestionnairesAndCorrections(val questionnaireEntries: List<QuestionnaireEntry>, val foundKeys: Set<String>) {
        operator fun plus(other: QuestionnairesAndCorrections): QuestionnairesAndCorrections {
            return QuestionnairesAndCorrections(
                questionnaireEntries + other.questionnaireEntries,
                foundKeys + other.foundKeys
            )
        }
    }
}