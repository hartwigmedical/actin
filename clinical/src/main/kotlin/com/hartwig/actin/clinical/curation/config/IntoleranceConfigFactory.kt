package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.icd.IcdModel

class IntoleranceConfigFactory(private val  icdModel: IcdModel) : CurationConfigFactory<ComorbidityConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<ComorbidityConfig> {
        val input = parts[fields["input"]!!]
        val (icdCodes, icdValidationErrors) =
            validateIcd(CurationCategory.INTOLERANCE, input, "icd", fields, parts, icdModel)

        // TODO Should consider how to model "we know for certain this patient has no intolerances".
        return ValidatedCurationConfig(
            ComorbidityConfig(
                input = input,
                ignore = false,
                curated = Intolerance(
                    name = parts[fields["name"]!!].trim().ifEmpty { null },
                    icdCodes = icdCodes
                )
            ), icdValidationErrors
        )
    }
}