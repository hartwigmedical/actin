package com.hartwig.actin.trial.input.single

import com.hartwig.actin.datamodel.clinical.BodyLocationCategory

data class OneIntegerOneBodyLocation(
    val integer: Int,
    val bodyLocation: BodyLocationCategory
)
