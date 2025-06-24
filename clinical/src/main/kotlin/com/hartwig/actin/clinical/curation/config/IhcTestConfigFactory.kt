package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.util.ResourceFile

class IhcTestConfigFactory(private val curationCategory: CurationCategory) : CurationConfigFactory<IhcTestConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<IhcTestConfig> {
        val item = parts[fields["item"]!!]
        val ignore = CurationUtil.isIgnoreString(item)
        val input = parts[fields["input"]!!]
        val (impliesPotentialIndeterminateStatus, impliesPotentialIndeterminateStatusValidationErrors)
                = validateBoolean(curationCategory, input, "impliesPotentialIndeterminateStatus", fields, parts)
        val molecularTest = curateObject(impliesPotentialIndeterminateStatus ?: false, fields, parts)
        return ValidatedCurationConfig(
            IhcTestConfig(
                input = input,
                ignore = ignore,
                curated = molecularTest.takeIf { !ignore && impliesPotentialIndeterminateStatusValidationErrors.isEmpty() }
            ), impliesPotentialIndeterminateStatusValidationErrors
        )
    }

    private fun curateObject(
        impliesPotentialIndeterminateStatus: Boolean,
        fields: Map<String, Int>,
        parts: Array<String>
    ): IhcTest {
        return IhcTest(
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