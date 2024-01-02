package com.hartwig.actin.clinical.feed.questionnaire

import com.google.common.collect.Maps
import com.hartwig.actin.clinical.datamodel.ECG
import com.hartwig.actin.clinical.datamodel.ImmutableECG
import com.hartwig.actin.clinical.datamodel.ImmutableInfectionStatus
import com.hartwig.actin.clinical.datamodel.InfectionStatus
import com.hartwig.actin.clinical.datamodel.TumorStage

internal object QuestionnaireCuration {
    private val OPTION_MAPPING: MutableMap<String, Boolean?> = Maps.newHashMap()
    private val STAGE_MAPPING: MutableMap<String, TumorStage?> = Maps.newHashMap()

    init {
        OPTION_MAPPING["no"] = false
        OPTION_MAPPING["No"] = false
        OPTION_MAPPING["NO"] = false
        OPTION_MAPPING["non"] = false
        OPTION_MAPPING["none"] = false
        OPTION_MAPPING["no indien ja welke"] = false
        OPTION_MAPPING["nee"] = false
        OPTION_MAPPING["neee"] = false
        OPTION_MAPPING["o"] = false
        OPTION_MAPPING["n.v.t."] = null
        OPTION_MAPPING["n.v.t"] = null
        OPTION_MAPPING["nvt"] = null
        OPTION_MAPPING["nvt."] = null
        OPTION_MAPPING["NA"] = null
        OPTION_MAPPING["na"] = null
        OPTION_MAPPING["yes"] = true
        OPTION_MAPPING["tes"] = true
        OPTION_MAPPING["Yes"] = true
        OPTION_MAPPING["YES"] = true
        OPTION_MAPPING["JA"] = true
        OPTION_MAPPING["Ja"] = true
        OPTION_MAPPING["ja"] = true
        OPTION_MAPPING["es"] = true
        OPTION_MAPPING["YES related to prostatecarcinoma"] = true
        OPTION_MAPPING["yes bone lesion L1 L2 with epidural extension"] = true
        OPTION_MAPPING["yes manubrium sterni"] = true
        OPTION_MAPPING["yes vertebra L2"] = true
        OPTION_MAPPING["yes wherefore surgery jun 2023"] = true
        OPTION_MAPPING["unknown"] = null
        OPTION_MAPPING["Unknown"] = null
        OPTION_MAPPING["UNKNOWN"] = null
        OPTION_MAPPING["uknown"] = null
        OPTION_MAPPING["unknonw"] = null
        OPTION_MAPPING["onknown"] = null
        OPTION_MAPPING["UNKOWN"] = null
        OPTION_MAPPING["suspect lesion"] = null
        OPTION_MAPPING["unknown after surgery"] = null
        OPTION_MAPPING["-"] = null
        OPTION_MAPPING["yes/no"] = null
        OPTION_MAPPING["yes/no/unknown"] = null
        OPTION_MAPPING["(yes/no)"] = null
        OPTION_MAPPING["botaantasting bij weke delen massa"] = false
        OPTION_MAPPING["no total resection"] = false
        OPTION_MAPPING["probably"] = null

        STAGE_MAPPING["I"] = TumorStage.I
        STAGE_MAPPING["1"] = TumorStage.I
        STAGE_MAPPING["II"] = TumorStage.II
        STAGE_MAPPING["2"] = TumorStage.II
        STAGE_MAPPING["IIa"] = TumorStage.IIA
        STAGE_MAPPING["IIA"] = TumorStage.IIA
        STAGE_MAPPING["IIb"] = TumorStage.IIB
        STAGE_MAPPING["IIB"] = TumorStage.IIB
        STAGE_MAPPING["III"] = TumorStage.III
        STAGE_MAPPING["3"] = TumorStage.III
        STAGE_MAPPING["IIIa"] = TumorStage.IIIA
        STAGE_MAPPING["IIIA"] = TumorStage.IIIA
        STAGE_MAPPING["IIIb"] = TumorStage.IIIB
        STAGE_MAPPING["IIIB"] = TumorStage.IIIB
        STAGE_MAPPING["IIIc"] = TumorStage.IIIC
        STAGE_MAPPING["IIIC"] = TumorStage.IIIC
        STAGE_MAPPING["IV"] = TumorStage.IV
        STAGE_MAPPING["IIII"] = TumorStage.IV
        STAGE_MAPPING["4"] = TumorStage.IV
        STAGE_MAPPING["cT3N2M1"] = TumorStage.IV
        STAGE_MAPPING["unknown"] = null
        STAGE_MAPPING["onknown"] = null
        STAGE_MAPPING["na"] = null
    }

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
        return buildFromDescription(subject, significantCurrentInfection, ::buildInfectionStatus)
    }

    fun toECG(subject: String, significantAberrationLatestECG: String?): ValidatedQuestionnaireCuration<ECG> {
        return buildFromDescription(subject, significantAberrationLatestECG, ::buildECG)
    }

    private fun buildInfectionStatus(
        hasActiveInfection: Boolean,
        description: String?
    ): ValidatedQuestionnaireCuration<InfectionStatus> {
        return ValidatedQuestionnaireCuration(
            ImmutableInfectionStatus.builder().hasActiveInfection(hasActiveInfection).description(description).build()
        )
    }

    private fun buildECG(hasSignificantAberrationLatestECG: Boolean, description: String?): ValidatedQuestionnaireCuration<ECG> {
        return ValidatedQuestionnaireCuration(
            ImmutableECG.builder()
                .hasSigAberrationLatestECG(hasSignificantAberrationLatestECG)
                .aberrationDescription(description)
                .build()
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