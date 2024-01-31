package com.hartwig.actin.clinical.datamodel

data class ECG(
    val hasSigAberrationLatestECG: Boolean,
    val aberrationDescription: String?,
    val qtcfMeasure: ECGMeasure?,
    val jtcMeasure: ECGMeasure?
)
