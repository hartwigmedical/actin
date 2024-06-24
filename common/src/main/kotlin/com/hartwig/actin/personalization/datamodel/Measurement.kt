package com.hartwig.actin.personalization.datamodel

data class Measurement(val value: Double, val numPatients: Int, val min: Int? = null, val max: Int? = null)
