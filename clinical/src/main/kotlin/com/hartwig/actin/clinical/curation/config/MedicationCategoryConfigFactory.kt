package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil

class MedicationCategoryConfigFactory : CurationConfigFactory<MedicationCategoryConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): MedicationCategoryConfig {
        return MedicationCategoryConfig(
            input = parts[fields["input"]!!],
            categories = CurationUtil.toCategories(parts[fields["categories"]!!])
        )
    }
}