package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory
import com.hartwig.actin.util.ResourceFile

class LesionLocationConfigFactory : CurationConfigFactory<LesionLocationConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<LesionLocationConfig> {
        val input = parts[fields["input"]!!]
        val (category, validationErrors) = validateOptionalEnum(
            CurationCategory.LESION_LOCATION,
            input,
            "category",
            fields,
            parts
        ) { LesionLocationCategory.valueOf(it) }
        val location = parts[fields["location"]!!]
        return ValidatedCurationConfig(
            LesionLocationConfig(
                input = input,
                ignore = CurationUtil.isIgnoreString(location),
                location = location,
                category = category,
                active = ResourceFile.optionalBool(parts[fields["active"]!!]),
                suspected = ResourceFile.optionalBool(parts[fields["suspected"]!!])
            ), validationErrors
        )
    }
}