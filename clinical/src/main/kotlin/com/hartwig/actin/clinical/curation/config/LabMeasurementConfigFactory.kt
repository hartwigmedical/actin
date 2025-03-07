package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.datamodel.clinical.LabMeasurement

class LabMeasurementConfigFactory : CurationConfigFactory<LabMeasurementConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<LabMeasurementConfig> {
        val input = parts[fields["input"]!!]
        val labMeasurement = parts[fields["labMeasurement"]!!]
        val (validatedLabMeasurement, labMeasurementValidationErrors) = validateMandatoryEnum<LabMeasurement>(
            CurationCategory.LAB_MEASUREMENT,
            input,
            "labMeasurement",
            fields,
            parts
        ) { LabMeasurement.valueOf(it) }
        return ValidatedCurationConfig(
            LabMeasurementConfig(
                input = input,
                ignore = CurationUtil.isIgnoreString(labMeasurement),
                labMeasurement = validatedLabMeasurement ?: LabMeasurement.UNKNOWN
            ),
            labMeasurementValidationErrors
        )
    }
}