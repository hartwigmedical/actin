package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.LabMeasurement

data class LabMeasurementConfig (
    override val input: String,
    override val ignore: Boolean = false,
    val labMeasurement: LabMeasurement
) : CurationConfig