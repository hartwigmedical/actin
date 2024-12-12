package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class ToxicityConfigFactory(private val  icdModel: IcdModel) : CurationConfigFactory<ToxicityConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<ToxicityConfig> {
        val input = parts[fields["input"]!!]
        val (icdCode, icdValidationErrors) =
            validateIcd(CurationCategory.TOXICITY, input, "icd", fields, parts, icdModel)
        val (grade, gradeValidationError) = validateInteger(CurationCategory.TOXICITY, input, "grade", fields, parts)
        return ValidatedCurationConfig(
            ToxicityConfig(
                input,
                ignore = CurationUtil.isIgnoreString(parts[fields["name"]!!]),
                name = parts[fields["name"]!!],
                categories = CurationUtil.toCategories(parts[fields["categories"]!!]),
                grade = grade,
                icdCode = icdCode ?: IcdCode("", null)
            ), gradeValidationError + icdValidationErrors
        )
    }
}