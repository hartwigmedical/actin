package com.hartwig.actin.datamodel.molecular.driver

enum class DriverLikelihood {
    HIGH,
    MEDIUM,
    LOW;

    override fun toString(): String {
        return name.substring(0, 1).uppercase() + name.substring(1).lowercase()
    }

    companion object {
        fun from(value: Double?): DriverLikelihood? {
            return when {
                value == null -> null
                value >= 0.8 -> HIGH
                value >= 0.2 -> MEDIUM
                else -> LOW
            }
        }
    }
}
