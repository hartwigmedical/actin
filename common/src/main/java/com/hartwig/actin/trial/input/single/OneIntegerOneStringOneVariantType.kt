package com.hartwig.actin.trial.input.single

import com.hartwig.actin.trial.input.datamodel.VariantTypeInput

data class OneIntegerOneStringOneVariantType(
    val integer: Int,
    val string: String,
    val variantType: VariantTypeInput
)
