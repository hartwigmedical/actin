package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationIcdValidator
import com.hartwig.actin.clinical.curation.CurationUtil

class ToxicityConfigFactory(private val  curationIcdValidator: CurationIcdValidator) : CurationConfigFactory<ToxicityConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<ToxicityConfig> {
        val input = parts[fields["input"]!!]
        val (icdTitle, icdValidationErrors) =
            validateIcd(CurationCategory.TOXICITY, input, "icd", fields, parts) { curationIcdValidator.isValidIcdTitle(it) }
        val icdCode = icdTitle?.let { curationIcdValidator.getCodeFromTitle(icdTitle) } ?: ""
        val (grade, gradeValidationError) = validateInteger(CurationCategory.TOXICITY, input, "grade", fields, parts)
        return ValidatedCurationConfig(
            ToxicityConfig(
                input,
                ignore = CurationUtil.isIgnoreString(parts[fields["name"]!!]),
                name = parts[fields["name"]!!],
                categories = CurationUtil.toCategories(parts[fields["categories"]!!]),
                grade = grade,
                icdCode = icdCode
            ), gradeValidationError + icdValidationErrors
        )
    }
}