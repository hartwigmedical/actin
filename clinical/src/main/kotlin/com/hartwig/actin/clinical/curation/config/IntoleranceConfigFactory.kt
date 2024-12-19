package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.icd.IcdModel

class IntoleranceConfigFactory(private val  icdModel: IcdModel) : CurationConfigFactory<IntoleranceConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<IntoleranceConfig> {
        val input = parts[fields["input"]!!]
        val (icdCodes, icdValidationErrors) =
            validateIcd(CurationCategory.INTOLERANCE, input, "icd", fields, parts, icdModel)

        // TODO Should consider how to model "we know for certain this patient has no intolerances".
        return ValidatedCurationConfig(
            IntoleranceConfig(
                input = input,
                name = parts[fields["name"]!!],
                icd = icdCodes ?: emptySet()
            ), icdValidationErrors
        )
    }
}