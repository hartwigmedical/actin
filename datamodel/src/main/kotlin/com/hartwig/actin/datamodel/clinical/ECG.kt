package com.hartwig.actin.datamodel.clinical

data class ECG(
    val hasSigAberrationLatestECG: Boolean,
    val aberrationDescription: String?,
    val qtcfMeasure: ECGMeasure?,
    val jtcMeasure: ECGMeasure?
)
