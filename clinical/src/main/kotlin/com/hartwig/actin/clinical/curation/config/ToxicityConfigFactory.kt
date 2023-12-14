package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil

class ToxicityConfigFactory : CurationConfigFactory<ToxicityConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<ToxicityConfig> {
        val input = parts[fields["input"]!!]
        val (grade, gradeValidationError) = validateInteger(input, "grade", fields, parts)
        return ValidatedCurationConfig(
            ToxicityConfig(
                input,
                ignore = CurationUtil.isIgnoreString(parts[fields["name"]!!]),
                name = parts[fields["name"]!!],
                categories = CurationUtil.toCategories(parts[fields["categories"]!!]),
                grade = grade
            ), gradeValidationError
        )
    }
}