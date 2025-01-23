package com.hartwig.actin.datamodel.molecular.driver

import com.hartwig.actin.datamodel.Displayable

enum class ProteinEffect(val description: String) : Displayable {
    UNKNOWN("Unknown protein effect"),
    AMBIGUOUS("Ambiguous protein effect"),
    NO_EFFECT("No protein effect"),
    NO_EFFECT_PREDICTED("No protein effect"),
    LOSS_OF_FUNCTION("Loss of function"),
    LOSS_OF_FUNCTION_PREDICTED("Loss of function"),
    GAIN_OF_FUNCTION("Gain of function"),
    GAIN_OF_FUNCTION_PREDICTED("Gain of function");

    override fun display(): String = description
}