package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.WhoAtcModel

const val ANATOMICAL = "NERVOUS SYSTEM"
const val THERAPEUTIC = "ANALGESICS"
const val PHARMACOLOGICAL = "OTHER ANALGESICS AND ANTIPYRETICS"
const val CHEMICAL = "Anilides"
const val CHEMICAL_SUBSTANCE = "paracetamol"
const val FULL_ATC_CODE = "N02BE01"

object TestAtcFactory {

    fun createProperAtcModel(): WhoAtcModel {
        return WhoAtcModel(
            mapOf(
                "N" to ANATOMICAL,
                "N02" to THERAPEUTIC,
                "N02B" to PHARMACOLOGICAL,
                "N02BE" to CHEMICAL,
                FULL_ATC_CODE to CHEMICAL_SUBSTANCE
            ),
            mapOf(
                Pair("N03XZ91", CHEMICAL_SUBSTANCE) to FULL_ATC_CODE
            )
        )
    }
}