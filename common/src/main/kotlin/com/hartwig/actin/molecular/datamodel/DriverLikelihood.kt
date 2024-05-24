package com.hartwig.actin.molecular.datamodel

enum class DriverLikelihood {
    HIGH,
    MEDIUM,
    LOW;

    override fun toString(): String {
        return name.substring(0, 1).uppercase() + name.substring(1).lowercase()
    }
}
