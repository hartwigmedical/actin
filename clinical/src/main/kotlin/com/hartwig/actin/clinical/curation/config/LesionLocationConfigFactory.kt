package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory
import java.util.*

class LesionLocationConfigFactory : CurationConfigFactory<LesionLocationConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<LesionLocationConfig> {
        val categoryInput = parts[fields["category"]!!]
        val (category, validationErrors) = if (categoryInput.isEmpty()) {
            null to emptyList()
        } else {
            val categoryEnumName = categoryInput.replace(" ".toRegex(), "_").uppercase(Locale.getDefault())
            if (enumContains<LesionLocationCategory>(categoryEnumName)) {
                LesionLocationCategory.valueOf(categoryEnumName) to emptyList()
            } else {
                null to listOf(enumInvalid<LesionLocationCategory>(categoryEnumName))
            }
        }
        return ValidatedCurationConfig(
            LesionLocationConfig(
                input = parts[fields["input"]!!],
                location = parts[fields["location"]!!],
                category = category
            ), validationErrors
        )
    }
}