package com.hartwig.actin.clinical.curation.config

data class PrimaryTumorConfig(
    override val input: String,
    override val ignore: Boolean = false,
    val name: String,
    val primaryTumorLocation: String,
    val primaryTumorSubLocation: String,
    val primaryTumorType: String,
    val primaryTumorSubType: String,
    val primaryTumorExtraDetails: String,
    val doids: Set<String>
) : CurationConfig