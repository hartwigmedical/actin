package com.hartwig.actin.clinical.feed.questionnaire

import com.google.common.collect.Maps
import com.hartwig.actin.clinical.datamodel.ECG
import com.hartwig.actin.clinical.datamodel.ImmutableECG
import com.hartwig.actin.clinical.datamodel.ImmutableInfectionStatus
import com.hartwig.actin.clinical.datamodel.InfectionStatus
import com.hartwig.actin.clinical.datamodel.TumorStage
import org.apache.logging.log4j.LogManager

internal object QuestionnaireCuration {
    private val LOGGER = LogManager.getLogger(QuestionnaireCuration::class.java)
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
        OPTION_MAPPING["unknown"] = null
        OPTION_MAPPING["Unknown"] = null
        OPTION_MAPPING["UNKNOWN"] = null
        OPTION_MAPPING["uknown"] = null
        OPTION_MAPPING["onknown"] = null
        OPTION_MAPPING["UNKOWN"] = null
        OPTION_MAPPING["suspect lesion"] = null
        OPTION_MAPPING["unknown after surgery"] = null
        OPTION_MAPPING["-"] = null
        OPTION_MAPPING["yes/no"] = null
        OPTION_MAPPING["yes/no/unknown"] = null
        OPTION_MAPPING["(yes/no)"] = null
        OPTION_MAPPING["botaantasting bij weke delen massa"] = false
        OPTION_MAPPING["YES related to prostatecarcinoma"] = true
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
    }

    fun toOption(option: String?): Boolean? {
        if (option.isNullOrEmpty()) {
            return null
        }
        if (!isConfiguredOption(option)) {
            LOGGER.warn("Unrecognized questionnaire option: '{}'", option)
            return null
        }
        return OPTION_MAPPING[option]
    }

    private fun isConfiguredOption(option: String?): Boolean {
        return OPTION_MAPPING.containsKey(option)
    }

    fun toStage(stage: String?): TumorStage? {
        if (stage.isNullOrEmpty()) {
            return null
        }
        if (!STAGE_MAPPING.containsKey(stage)) {
            LOGGER.warn("Unrecognized questionnaire tumor stage: '{}'", stage)
            return null
        }
        return STAGE_MAPPING[stage]
    }

    fun toWHO(integer: String?): Int? {
        if (integer.isNullOrEmpty()) {
            return null
        }
        val value = integer.toInt()
        return if (value in 0..5) {
            value
        } else {
            LOGGER.warn("WHO status not between 0 and 5: '{}'", value)
            null
        }
    }

    fun toSecondaryPrimaries(secondaryPrimary: String, lastTreatmentInfo: String): List<String> {
        return listOf(secondaryPrimary + if (lastTreatmentInfo.isEmpty()) "" else " | $lastTreatmentInfo")
    }

    fun toInfectionStatus(significantCurrentInfection: String?): InfectionStatus? {
        return buildFromDescription(significantCurrentInfection, ::buildInfectionStatus)
    }

    fun toECG(significantAberrationLatestECG: String?): ECG? {
        return buildFromDescription(significantAberrationLatestECG, ::buildECG)
    }

    private fun buildInfectionStatus(hasActiveInfection: Boolean, description: String?): InfectionStatus {
        return ImmutableInfectionStatus.builder().hasActiveInfection(hasActiveInfection).description(description).build()
    }

    private fun buildECG(hasSignificantAberrationLatestECG: Boolean, description: String?): ECG {
        return ImmutableECG.builder()
            .hasSigAberrationLatestECG(hasSignificantAberrationLatestECG)
            .aberrationDescription(description)
            .build()
    }

    private fun <T> buildFromDescription(description: String?, buildFunction: (Boolean, String?) -> T): T? {
        val present: Boolean? = if (isConfiguredOption(description)) {
            toOption(description)
        } else if (!description.isNullOrEmpty()) {
            true
        } else {
            null
        }
        return if (present == null) {
            null
        } else buildFunction(present, description)
    }
}