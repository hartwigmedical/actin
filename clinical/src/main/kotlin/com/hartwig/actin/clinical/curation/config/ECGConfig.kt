package com.hartwig.actin.clinical.curation.config

data class ECGConfig(
    override val input: String,
    override val ignore: Boolean,
    val interpretation: String,
    val isQTCF: Boolean,
    val qtcfValue: Int? = null,
    val qtcfUnit: String? = null,
    val isJTC: Boolean,
    val jtcValue: Int? = null,
    val jtcUnit: String? = null,
    val hasSigAberrationLatestECG: Boolean? = null
) : CurationConfig
