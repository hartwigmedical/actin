package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory

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
        return ValidatedCurationConfig(
            LesionLocationConfig(
                input = input,
                location = parts[fields["location"]!!],
                category = category
            ), validationErrors
        )
    }
}