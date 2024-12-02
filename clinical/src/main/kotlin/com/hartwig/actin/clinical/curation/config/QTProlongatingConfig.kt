package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.QTProlongatingRisk

data class QTProlongatingConfig(override val input: String, override val ignore: Boolean, val status: QTProlongatingRisk) : CurationConfig
