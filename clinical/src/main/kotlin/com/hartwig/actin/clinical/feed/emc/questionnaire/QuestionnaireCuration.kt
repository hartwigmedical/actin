package com.hartwig.actin.clinical.feed.emc.questionnaire

import com.hartwig.actin.clinical.curation.extraction.BooleanValueParser
import com.hartwig.actin.datamodel.clinical.Ecg
import com.hartwig.actin.datamodel.clinical.InfectionStatus
import com.hartwig.actin.util.Either

internal object QuestionnaireCuration {

    fun toInfectionStatus(significantCurrentInfection: String?): ValidatedQuestionnaireCuration<InfectionStatus> {
        return buildFromDescription(significantCurrentInfection, QuestionnaireCuration::buildInfectionStatus)
    }

    fun toEcg(significantAberrationLatestEcg: String?): ValidatedQuestionnaireCuration<Ecg> {
        return buildFromDescription(significantAberrationLatestEcg, QuestionnaireCuration::buildEcg)
    }

    private fun buildInfectionStatus(
        hasActiveInfection: Boolean,
        description: String?
    ): ValidatedQuestionnaireCuration<InfectionStatus> {
        return ValidatedQuestionnaireCuration(InfectionStatus(hasActiveInfection = hasActiveInfection, description = description))
    }

    private fun buildEcg(isPresent: Boolean, description: String?): ValidatedQuestionnaireCuration<Ecg> {
        return ValidatedQuestionnaireCuration(
            Ecg(
                name = description,
                jtcMeasure = null,
                qtcfMeasure = null
            ).takeIf { isPresent }
        )
    }

    private fun <T> buildFromDescription(
        description: String?,
        buildFunction: (Boolean, String?) -> ValidatedQuestionnaireCuration<T>
    ): ValidatedQuestionnaireCuration<T> {
        val isPresent = when (val parsed = BooleanValueParser.parseBoolean(description)) {
            is Either.Right -> parsed.value
            else -> true
        }
        return isPresent?.let { buildFunction(it, description) } ?: ValidatedQuestionnaireCuration(null)
    }
}