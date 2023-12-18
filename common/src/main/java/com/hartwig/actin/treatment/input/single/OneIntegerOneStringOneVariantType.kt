package com.hartwig.actin.treatment.input.single

import com.hartwig.actin.treatment.input.datamodel.VariantTypeInput

data class OneIntegerOneStringOneVariantType(
    val integer: Int,
    val string: String,
    val variantType: VariantTypeInput
)
