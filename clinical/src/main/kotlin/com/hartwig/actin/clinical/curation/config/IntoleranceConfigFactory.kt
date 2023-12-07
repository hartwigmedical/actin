package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.curation.CurationUtil

class IntoleranceConfigFactory(private val curationDoidValidator: CurationDoidValidator) : CurationConfigFactory<IntoleranceConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<IntoleranceConfig> {
        val input = parts[fields["input"]!!]
        val doids = CurationUtil.toDOIDs(parts[fields["doids"]!!])
        // TODO Should consider how to model "we know for certain this patient has no intolerances".
        return ValidatedCurationConfig(
            IntoleranceConfig(input = input, name = parts[fields["name"]!!], doids = doids),
            if (!input.equals(INTOLERANCE_INPUT_TO_IGNORE_FOR_DOID_CURATION, ignoreCase = true)
                && !curationDoidValidator.isValidDiseaseDoidSet(doids)
            ) {
                listOf(
                    CurationConfigValidationError(
                        "Intolerance config with input '$input' contains at least one invalid doid: '$doids'"
                    )
                )
            } else emptyList()
        )
    }

    companion object {
        private const val INTOLERANCE_INPUT_TO_IGNORE_FOR_DOID_CURATION = "Geen"
    }

}