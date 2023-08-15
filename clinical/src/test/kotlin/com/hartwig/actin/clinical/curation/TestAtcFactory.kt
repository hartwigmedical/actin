package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.WhoAtcModel

const val ANATOMICAL = "NERVOUS SYSTEM"
const val THERAPEUTIC = "ANALGESICS"
const val PHARMACOLOGICAL = "OTHER ANALGESICS AND ANTIPYRETICS"
const val CHEMICAL = "Anilides"
const val CHEMICAL_SUBSTANCE = "paracetamol"
const val ATC_CODE = "N02BE01"

object TestAtcFactory {

    fun createMinimalAtcModel(): WhoAtcModel {
        return WhoAtcModel(emptyMap())
    }

    fun createProperAtcModel(): WhoAtcModel {
        return WhoAtcModel(
            mapOf(
                "N" to ANATOMICAL,
                "N02" to THERAPEUTIC,
                "N02B" to PHARMACOLOGICAL,
                "N02BE" to CHEMICAL,
                ATC_CODE to CHEMICAL_SUBSTANCE
            )
        )
    }
}