package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.datamodel.clinical.IHC_TEST_TYPE
import com.hartwig.actin.datamodel.clinical.PriorIHCTest
import com.hartwig.actin.util.ResourceFile

class IHCTestConfigFactory(private val curationCategory: CurationCategory) : CurationConfigFactory<IHCTestConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<IHCTestConfig> {
        val test = parts[fields["test"]!!]
        val ignore = CurationUtil.isIgnoreString(test)
        val input = parts[fields["input"]!!]
        val (impliesPotentialIndeterminateStatus, impliesPotentialIndeterminateStatusValidationErrors)
                = validateBoolean(curationCategory, input, "impliesPotentialIndeterminateStatus", fields, parts)
        val priorMolecularTest = impliesPotentialIndeterminateStatus?.let { curateObject(it, test, fields, parts) }
        return ValidatedCurationConfig(
            IHCTestConfig(
                input = input,
                ignore = ignore,
                curated = if (!ignore) {
                    priorMolecularTest
                } else null
            ), impliesPotentialIndeterminateStatusValidationErrors
        )
    }

    private fun curateObject(
        impliesPotentialIndeterminateStatus: Boolean,
        test: String,
        fields: Map<String, Int>,
        parts: Array<String>
    ): PriorIHCTest {
        return PriorIHCTest(
            item = parts[fields["item"]!!],
            test = test.ifEmpty { IHC_TEST_TYPE },
            measure = ResourceFile.optionalString(parts[fields["measure"]!!]),
            scoreText = ResourceFile.optionalString(parts[fields["scoreText"]!!]),
            scoreValuePrefix = ResourceFile.optionalString(parts[fields["scoreValuePrefix"]!!]),
            scoreValue = ResourceFile.optionalNumber(parts[fields["scoreValue"]!!]),
            scoreValueUnit = ResourceFile.optionalString(parts[fields["scoreValueUnit"]!!]),
            impliesPotentialIndeterminateStatus = impliesPotentialIndeterminateStatus
        )
    }
}