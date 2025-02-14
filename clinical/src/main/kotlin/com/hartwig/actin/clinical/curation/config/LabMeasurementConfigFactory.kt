package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.datamodel.clinical.LabMeasurement

class LabMeasurementConfigFactory : CurationConfigFactory<LabMeasurementConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<LabMeasurementConfig> {
        val labMeasurement = parts[fields["labMeasurement"]!!]
        return ValidatedCurationConfig(
            LabMeasurementConfig(
                input = parts[fields["input"]!!],
                ignore = CurationUtil.isIgnoreString(labMeasurement),
                labMeasurement = LabMeasurement.valueOf(labMeasurement.uppercase())
            )
        )
    }
}