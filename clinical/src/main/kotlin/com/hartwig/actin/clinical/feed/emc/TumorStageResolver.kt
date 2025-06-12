package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.datamodel.clinical.TumorStage

object TumorStageResolver {

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

    fun resolve(stage: String?): TumorStage? {
        return stage?.let { STAGE_MAPPING[stage] }
    }
}