package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.extraction.BooleanValueParser
import com.hartwig.actin.datamodel.clinical.OtherCondition
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.util.Either

class InfectionConfigFactory(private val icdModel: IcdModel) : CurationConfigFactory<ComorbidityConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<ComorbidityConfig> {
        val interpretation = parts[fields["interpretation"]!!]
        val ignore = when (val parsed = BooleanValueParser.parseBoolean(interpretation)) {
            is Either.Right -> parsed.value != true
            else -> interpretation == "NULL"
        }
        val input = parts[fields["input"]!!]
        val (icdCodes, icdValidationErrors) = validateIcd(CurationCategory.INFECTION, input, "icd", fields, parts, icdModel)
        return ValidatedCurationConfig(
            ComorbidityConfig(
                input = input,
                ignore = ignore,
                curated = OtherCondition(name = interpretation, icdCodes = icdCodes).takeUnless { ignore }
            ),
            icdValidationErrors
        )
    }
}