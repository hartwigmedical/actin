package com.hartwig.actin.clinical.curation.config

data class PeriodBetweenUnitConfig(override val input: String, override val ignore: Boolean = false, val interpretation: String) :
    CurationConfig