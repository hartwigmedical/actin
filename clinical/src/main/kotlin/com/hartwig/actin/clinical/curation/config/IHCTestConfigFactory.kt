package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.datamodel.clinical.IHCTest
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.util.ResourceFile

class IHCTestConfigFactory(private val curationCategory: CurationCategory) : CurationConfigFactory<IHCTestConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<IHCTestConfig> {
        val test = parts[fields["test"]!!]
        val ignore = CurationUtil.isIgnoreString(test)
        val input = parts[fields["input"]!!]
        val (impliesPotentialIndeterminateStatus, impliesPotentialIndeterminateStatusValidationErrors)
                = validateBoolean(curationCategory, input, "impliesPotentialIndeterminateStatus", fields, parts)
        val molecularTest = impliesPotentialIndeterminateStatus?.let { curateObject(it, fields, parts) }
        return ValidatedCurationConfig(
            IHCTestConfig(
                input = input,
                ignore = ignore,
                curated = if (!ignore) {
                    molecularTest
                } else null
            ), impliesPotentialIndeterminateStatusValidationErrors
        )
    }

    private fun curateObject(
        impliesPotentialIndeterminateStatus: Boolean,
        fields: Map<String, Int>,
        parts: Array<String>
    ): IHCTest {
        return IHCTest(
            item = parts[fields["item"]!!],
            measure = ResourceFile.optionalString(parts[fields["measure"]!!]),
            scoreText = ResourceFile.optionalString(parts[fields["scoreText"]!!]),
            scoreValuePrefix = ResourceFile.optionalString(parts[fields["scoreValuePrefix"]!!]),
            scoreValue = ResourceFile.optionalNumber(parts[fields["scoreValue"]!!]),
            scoreValueUnit = ResourceFile.optionalString(parts[fields["scoreValueUnit"]!!]),
            impliesPotentialIndeterminateStatus = impliesPotentialIndeterminateStatus
        )
    }
}