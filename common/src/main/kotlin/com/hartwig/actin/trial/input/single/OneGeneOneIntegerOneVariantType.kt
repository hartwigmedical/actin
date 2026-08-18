package com.hartwig.actin.trial.input.single

import com.hartwig.actin.datamodel.trial.VariantType

data class OneGeneOneIntegerOneVariantType(
    val geneName: String,
    val integer: Int,
    val variantType: VariantType
)
