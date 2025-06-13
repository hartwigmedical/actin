package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.datamodel.clinical.OtherCondition
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.icd.IcdModel

class InfectionConfigFactory(private val icdModel: IcdModel) : CurationConfigFactory<ComorbidityConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<ComorbidityConfig> {
        val interpretation = parts[fields["interpretation"]!!]
        val ignore = CurationUtil.isIgnoreString(interpretation)
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