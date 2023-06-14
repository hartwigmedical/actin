package com.hartwig.actin.clinical.curation.config

data class ECGConfig(
    override val input: String,
    override val ignore: Boolean,
    val interpretation: String,
    val isQTCF: Boolean,
    val qtcfValue: Int?,
    val qtcfUnit: String?,
    val isJTC: Boolean,
    val jtcValue: Int?,
    val jtcUnit: String?
) : CurationConfig
