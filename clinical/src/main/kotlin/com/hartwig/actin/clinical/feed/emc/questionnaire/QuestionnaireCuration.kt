package com.hartwig.actin.clinical.feed.emc.questionnaire

import com.hartwig.actin.clinical.datamodel.ECG
import com.hartwig.actin.clinical.datamodel.InfectionStatus
import com.hartwig.actin.clinical.datamodel.TumorStage

internal object QuestionnaireCuration {
    private val OPTION_MAPPING = mapOf(
        "no" to false,
        "No" to false,
        "NO" to false,
        "non" to false,
        "none" to false,
        "no indien ja welke" to false,
        "nee" to false,
        "neee" to false,
        "o" to false,
        "n.v.t." to null,
        "n.v.t" to null,
        "nvt" to null,
        "nvt." to null,
        "NA" to null,
        "na" to null,
        "yes" to true,
        "tes" to true,
        "Yes" to true,
        "YES" to true,
        "JA" to true,
        "Ja" to true,
        "ja" to true,
        "es" to true,
        "YES related to prostatecarcinoma" to true,
        "yes bone lesion L1 L2 with epidural extension" to true,
        "yes manubrium sterni" to true,
        "yes vertebra L2" to true,
        "yes wherefore surgery jun 2023" to true,
        "unknown" to null,
        "Unknown" to null,
        "UNKNOWN" to null,
        "uknown" to null,
        "unknonw" to null,
        "onknown" to null,
        "UNKOWN" to null,
        "suspect lesion" to null,
        "unknown after surgery" to null,
        "-" to null,
        "yes/no" to null,
        "yes/no/unknown" to null,
        "(yes/no)" to null,
        "botaantasting bij weke delen massa" to false,
        "no total resection" to false,
        "probably" to null,
        "ye" to true,
        "possible" to null,
        "onbekend" to null,
    )

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

    fun toOption(subject: String, option: String?): ValidatedQuestionnaireCuration<Boolean> {
        if (option.isNullOrEmpty()) {
            return ValidatedQuestionnaireCuration(null)
        }
        if (!isConfiguredOption(option)) {
            return ValidatedQuestionnaireCuration(
                null,
                listOf(QuestionnaireCurationError(subject, "Unrecognized questionnaire option: '$option'"))
            )
        }
        return ValidatedQuestionnaireCuration(OPTION_MAPPING[option])
    }

    private fun isConfiguredOption(option: String?): Boolean {
        return OPTION_MAPPING.containsKey(option)
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
        return listOf(secondaryPrimary + if (lastTreatmentInfo.isEmpty()) "" else " | $lastTreatmentInfo")
    }

    fun toInfectionStatus(subject: String, significantCurrentInfection: String?): ValidatedQuestionnaireCuration<InfectionStatus> {
        return buildFromDescription(subject, significantCurrentInfection, QuestionnaireCuration::buildInfectionStatus)
    }

    fun toECG(subject: String, significantAberrationLatestECG: String?): ValidatedQuestionnaireCuration<ECG> {
        return buildFromDescription(subject, significantAberrationLatestECG, QuestionnaireCuration::buildECG)
    }

    private fun buildInfectionStatus(
        hasActiveInfection: Boolean,
        description: String?
    ): ValidatedQuestionnaireCuration<InfectionStatus> {
        return ValidatedQuestionnaireCuration(InfectionStatus(hasActiveInfection = hasActiveInfection, description = description))
    }

    private fun buildECG(hasSignificantAberrationLatestECG: Boolean, description: String?): ValidatedQuestionnaireCuration<ECG> {
        return ValidatedQuestionnaireCuration(
            ECG(
                hasSigAberrationLatestECG = hasSignificantAberrationLatestECG,
                aberrationDescription = description,
                jtcMeasure = null,
                qtcfMeasure = null
            )
        )
    }

    private fun <T> buildFromDescription(
        subject: String,
        description: String?,
        buildFunction: (Boolean, String?) -> ValidatedQuestionnaireCuration<T>
    ): ValidatedQuestionnaireCuration<T> {
        val present: ValidatedQuestionnaireCuration<Boolean> = if (isConfiguredOption(description)) {
            toOption(subject, description)
        } else if (!description.isNullOrEmpty()) {
            ValidatedQuestionnaireCuration(true)
        } else {
            ValidatedQuestionnaireCuration(null)
        }
        return if (present.curated == null) {
            ValidatedQuestionnaireCuration(null, present.errors)
        } else buildFunction(present.curated, description)
    }
}