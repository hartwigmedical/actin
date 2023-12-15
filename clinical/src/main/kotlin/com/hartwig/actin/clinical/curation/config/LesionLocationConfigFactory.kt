package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory

class LesionLocationConfigFactory : CurationConfigFactory<LesionLocationConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<LesionLocationConfig> {
        val input = parts[fields["input"]!!]
        val categoryInput = parts[fields["category"]!!]
        val categoryEnumName = categoryInput.ifEmpty { null }?.replace(" ".toRegex(), "_")?.uppercase()
        val (category, validationErrors) = categoryEnumName?.let {
            validateEnum(
                categoryEnumName,
                input
            ) { LesionLocationCategory.valueOf(it) }
        }
            ?: (null to emptyList())
        return ValidatedCurationConfig(
            LesionLocationConfig(
                input = input,
                location = parts[fields["location"]!!],
                category = category
            ), validationErrors
        )
    }
}