package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.util.ResourceFile

class MolecularTestConfigFactory : CurationConfigFactory<MolecularTestConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<MolecularTestConfig> {
        val ignore = CurationUtil.isIgnoreString(parts[fields["test"]!!])
        val (priorMolecularTest, validationErrors) = curateObject(fields, parts)
        return ValidatedCurationConfig(
            MolecularTestConfig(
                input = parts[fields["input"]!!],
                ignore = ignore,
                curated = if (!ignore) {
                    priorMolecularTest
                } else null
            ), validationErrors
        )
    }

    private fun curateObject(
        fields: Map<String, Int>,
        parts: Array<String>
    ): Pair<PriorMolecularTest?, List<CurationConfigValidationError>> {
        val impliesPotentialIndeterminateStatusInput = parts[fields["impliesPotentialIndeterminateStatus"]!!]
        val impliesPotentialIndeterminateStatus = impliesPotentialIndeterminateStatusInput.toValidatedBoolean()
        return impliesPotentialIndeterminateStatus?.let {
            return ImmutablePriorMolecularTest.builder()
                .test(parts[fields["test"]!!])
                .item(parts[fields["item"]!!])
                .measure(ResourceFile.optionalString(parts[fields["measure"]!!]))
                .scoreText(ResourceFile.optionalString(parts[fields["scoreText"]!!]))
                .scoreValuePrefix(ResourceFile.optionalString(parts[fields["scoreValuePrefix"]!!]))
                .scoreValue(ResourceFile.optionalNumber(parts[fields["scoreValue"]!!]))
                .scoreValueUnit(ResourceFile.optionalString(parts[fields["scoreValueUnit"]!!]))
                .impliesPotentialIndeterminateStatus(it).build() to emptyList()
        } ?: (null to listOf(
            CurationConfigValidationError(
                "impliesPotentialIndeterminateStatus was configured with an invalid value of '$impliesPotentialIndeterminateStatusInput' " +
                        "for input '${parts[fields["input"]!!]}'"
            )
        ))
    }
}