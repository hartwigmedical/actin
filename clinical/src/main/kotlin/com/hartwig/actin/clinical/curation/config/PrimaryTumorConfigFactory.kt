package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDoidValidator

class PrimaryTumorConfigFactory(private val curationDoidValidator: CurationDoidValidator) : CurationConfigFactory<PrimaryTumorConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<PrimaryTumorConfig> {
        val input = parts[fields["input"]!!]
        val (doids, doidValidationErrors) = validateDoids(
            CurationCategory.PRIMARY_TUMOR,
            input,
            "doids",
            fields,
            parts
        ) { curationDoidValidator.isValidCancerDoidSet(it) }

        return ValidatedCurationConfig(
            PrimaryTumorConfig(input = input, name = parts[fields["name"]!!], doids = doids ?: emptySet()),
            doidValidationErrors
        )
    }
}