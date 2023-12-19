package com.hartwig.actin.molecular.datamodel.driver

import java.util.*

enum class DriverLikelihood {
    HIGH,
    MEDIUM,
    LOW;

    override fun toString(): String {
        return name.substring(0, 1).uppercase(Locale.getDefault()) + name.substring(1).lowercase(Locale.getDefault())
    }
}
