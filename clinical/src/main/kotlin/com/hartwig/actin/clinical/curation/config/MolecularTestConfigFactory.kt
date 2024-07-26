package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.datamodel.PriorIHCTest
import com.hartwig.actin.util.ResourceFile

class MolecularTestConfigFactory(private val curationCategory: CurationCategory) : CurationConfigFactory<MolecularTestConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<MolecularTestConfig> {
        val ignore = CurationUtil.isIgnoreString(parts[fields["test"]!!])
        val input = parts[fields["input"]!!]
        val (impliesPotentialIndeterminateStatus, impliesPotentialIndeterminateStatusValidationErrors)
                = validateBoolean(curationCategory, input, "impliesPotentialIndeterminateStatus", fields, parts)
        val priorMolecularTest = impliesPotentialIndeterminateStatus?.let { curateObject(it, fields, parts) }
        return ValidatedCurationConfig(
            MolecularTestConfig(
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
        fields: Map<String, Int>,
        parts: Array<String>
    ): PriorIHCTest {
        return PriorIHCTest(
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