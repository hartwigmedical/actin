package com.hartwig.actin.clinical.curation.config

class SequencingTestConfig(override val input: String, override val ignore: Boolean, val curatedName: String) : CurationConfig 