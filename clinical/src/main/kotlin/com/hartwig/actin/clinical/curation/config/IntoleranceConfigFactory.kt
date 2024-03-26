package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver
import com.hartwig.actin.util.ResourceFile

class IntoleranceConfigFactory(private val curationDoidValidator: CurationDoidValidator) : CurationConfigFactory<IntoleranceConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<IntoleranceConfig> {
        val input = parts[fields["input"]!!]
        val (doids, doidValidationErrors) = if (!input.equals(INTOLERANCE_INPUT_TO_IGNORE_FOR_DOID_CURATION, ignoreCase = true)) {
            validateDoids(CurationCategory.INTOLERANCE, input, "doids", fields, parts) { curationDoidValidator.isValidDiseaseDoidSet(it) }
        } else {
            null to emptyList()
        }
        val treatmentCategories = ResourceFile.optionalString(parts[fields["treatmentCategory"]!!])
            ?.let { TreatmentCategoryResolver.fromStringList(it) }
        // TODO Should consider how to model "we know for certain this patient has no intolerances".
        return ValidatedCurationConfig(
            IntoleranceConfig(input = input, name = parts[fields["name"]!!], doids = doids ?: emptySet(), treatmentCategories = treatmentCategories), doidValidationErrors
        )
    }

    companion object {
        private const val INTOLERANCE_INPUT_TO_IGNORE_FOR_DOID_CURATION = "Geen"
    }

}