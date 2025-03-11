package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.icd.IcdModel

class ToxicityConfigFactory(private val  icdModel: IcdModel) : CurationConfigFactory<ComorbidityConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<ComorbidityConfig> {
        val input = parts[fields["input"]!!]
        val (icdCodes, icdValidationErrors) =
            validateIcd(CurationCategory.TOXICITY, input, "icd", fields, parts, icdModel)
        val (grade, gradeValidationError) = validateInteger(CurationCategory.TOXICITY, input, "grade", fields, parts)
        return ValidatedCurationConfig(
            ComorbidityConfig(
                input,
                ignore = CurationUtil.isIgnoreString(parts[fields["name"]!!]),
                curated = ToxicityCuration(
                    name = parts[fields["name"]!!].trim().ifEmpty { null },
                    grade = grade,
                    icdCodes = icdCodes
                )
            ), gradeValidationError + icdValidationErrors
        )
    }
}