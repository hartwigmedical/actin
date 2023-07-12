package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.AtcModel

const val ALIMENTARY_TRACT_AND_METABOLISM = "Alimentary tract and metabolism"
const val DRUGS_USED_IN_DIABETES = "Drugs used in diabetes"
const val BLOOD_GLUCOSE_LOWERING_DRUGS_EXCL_INSULINS = "Blood glucose lowering drugs, excl. insulins"
const val BIGUANIDES = "Biguanides"
const val METFORMIN = "metformin"
const val METAFORMIN_ATC_CODE = "A10BA02"

object TestAtcFactory {

    fun createEmptyModel(): AtcModel {
        return AtcModel(emptyMap())
    }

    fun createMinimalModel(): AtcModel {
        return AtcModel(mapOf(
            "A" to ALIMENTARY_TRACT_AND_METABOLISM,
            "A10" to DRUGS_USED_IN_DIABETES,
            "A10B" to BLOOD_GLUCOSE_LOWERING_DRUGS_EXCL_INSULINS,
            "A10BA" to BIGUANIDES,
            METAFORMIN_ATC_CODE to METFORMIN
        ))
    }
}