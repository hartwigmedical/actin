package com.hartwig.actin.treatment.input.single

import com.hartwig.actin.treatment.input.datamodel.VariantTypeInput

data class OneGeneOneIntegerOneVariantType(
    val geneName: String,
    val integer: Int,
    val variantType: VariantTypeInput
)
