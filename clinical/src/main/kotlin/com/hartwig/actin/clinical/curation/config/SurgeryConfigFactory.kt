package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.datamodel.clinical.SurgeryType
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory

class SurgeryConfigFactory : CurationConfigFactory<SurgeryConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<SurgeryConfig> {
        val input = parts[fields["input"]!!]
        val name = parts[fields["name"]!!]
        val (type, validationErrors) = validateOptionalEnum(CurationCategory.SURGERY, input, "type", fields, parts) {
            SurgeryType.valueOf(it)
        }
        return ValidatedCurationConfig(
            SurgeryConfig(
                input = input,
                ignore = CurationUtil.isIgnoreString(name),
                name = name,
                type = type ?: SurgeryType.UNKNOWN,
            ), validationErrors
        )
    }
}


