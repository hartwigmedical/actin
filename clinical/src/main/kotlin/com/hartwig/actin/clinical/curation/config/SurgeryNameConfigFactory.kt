package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.datamodel.clinical.SurgeryType
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory

class SurgeryNameConfigFactory : CurationConfigFactory<SurgeryNameConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<SurgeryNameConfig> {
        val input = parts[fields["input"]!!]
        val name = parts[fields["name"]!!]
        val (type, validationErrors) = validateOptionalEnum(
            CurationCategory.SURGERY_NAME,
            input,
            "type",
            fields,
            parts
        ) { SurgeryType.valueOf(it) }
        return ValidatedCurationConfig(
            SurgeryNameConfig(
                input = input,
                ignore = CurationUtil.isIgnoreString(name),
                name = name,
                type = type
            ), validationErrors
        )
    }
}


