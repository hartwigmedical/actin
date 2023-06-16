package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil

class MedicationNameConfigFactory : CurationConfigFactory<MedicationNameConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): MedicationNameConfig {
        val name = parts[fields["name"]!!]
        return MedicationNameConfig(
            input = parts[fields["input"]!!],
            ignore = CurationUtil.isIgnoreString(name),
            name = name
        )
    }
}