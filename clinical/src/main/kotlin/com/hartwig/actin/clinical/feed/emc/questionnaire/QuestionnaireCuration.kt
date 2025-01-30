package com.hartwig.actin.clinical.feed.emc.questionnaire

import com.hartwig.actin.clinical.curation.extraction.BooleanValueParser
import com.hartwig.actin.datamodel.clinical.Ecg
import com.hartwig.actin.datamodel.clinical.InfectionStatus
import com.hartwig.actin.datamodel.clinical.TumorStage
import com.hartwig.actin.util.Either

internal object QuestionnaireCuration {

    private val STAGE_MAPPING = mapOf(
        "I" to TumorStage.I,
        "1" to TumorStage.I,
        "II" to TumorStage.II,
        "2" to TumorStage.II,
        "IIa" to TumorStage.IIA,
        "IIA" to TumorStage.IIA,
        "IIb" to TumorStage.IIB,
        "IIB" to TumorStage.IIB,
        "III" to TumorStage.III,
        "3" to TumorStage.III,
        "IIIa" to TumorStage.IIIA,
        "IIIA" to TumorStage.IIIA,
        "IIIb" to TumorStage.IIIB,
        "IIIB" to TumorStage.IIIB,
        "IIIc" to TumorStage.IIIC,
        "IIIC" to TumorStage.IIIC,
        "IV" to TumorStage.IV,
        "IIII" to TumorStage.IV,
        "4" to TumorStage.IV,
        "cT3N2M1" to TumorStage.IV,
        "unknown" to null,
        "onknown" to null,
        "na" to null
    )

    fun toBoolean(subject: String, option: String?): ValidatedQuestionnaireCuration<Boolean> {
        return when (val parsed = BooleanValueParser.parseBoolean(option)) {
            is Either.Right -> ValidatedQuestionnaireCuration(parsed.value)
            else -> ValidatedQuestionnaireCuration(
                null, listOf(QuestionnaireCurationError(subject, "Unrecognized questionnaire option: '$option'"))
            )
        }
    }

    fun toStage(subject: String, stage: String?): ValidatedQuestionnaireCuration<TumorStage> {
        if (stage.isNullOrEmpty()) {
            return ValidatedQuestionnaireCuration(null)
        }
        if (!STAGE_MAPPING.containsKey(stage)) {
            return ValidatedQuestionnaireCuration(
                null,
                listOf(QuestionnaireCurationError(subject, "Unrecognized questionnaire tumor stage: '$stage'"))
            )
        }
        return ValidatedQuestionnaireCuration(STAGE_MAPPING[stage])
    }

    fun toWHO(subject: String, integer: String?): ValidatedQuestionnaireCuration<Int> {
        if (integer.isNullOrEmpty()) {
            return ValidatedQuestionnaireCuration(null)
        }
        return when (val value = integer.toIntOrNull()) {
            null -> {
                return ValidatedQuestionnaireCuration(
                    null,
                    listOf(QuestionnaireCurationError(subject, "WHO status not an integer: '$integer'"))
                )
            }

            in 0..5 -> {
                ValidatedQuestionnaireCuration(value)
            }

            else -> {
                ValidatedQuestionnaireCuration(
                    null,
                    listOf(QuestionnaireCurationError(subject, "WHO status not between 0 and 5: '$value'"))
                )
            }
        }
    }

    fun toSecondaryPrimaries(secondaryPrimary: String, lastTreatmentInfo: String): List<String> {
        return listOf(secondaryPrimary + if (lastTreatmentInfo.isEmpty()) "" else " | last treatment date: $lastTreatmentInfo")
    }

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

    private fun buildEcg(hasSignificantAberrationLatestEcg: Boolean, description: String?): ValidatedQuestionnaireCuration<Ecg> {
        return ValidatedQuestionnaireCuration(
            Ecg(
                hasSigAberrationLatestEcg = hasSignificantAberrationLatestEcg,
                name = description,
                jtcMeasure = null,
                qtcfMeasure = null
            )
        )
    }

    private fun <T> buildFromDescription(
        description: String?,
        buildFunction: (Boolean, String?) -> ValidatedQuestionnaireCuration<T>
    ): ValidatedQuestionnaireCuration<T> {
        val present = when (val parsed = BooleanValueParser.parseBoolean(description)) {
            is Either.Right -> parsed.value
            else -> true
        }
        return present?.let { buildFunction(it, description) } ?: ValidatedQuestionnaireCuration(null)
    }
}