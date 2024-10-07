package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil

class SurgeryNameConfigFactory : CurationConfigFactory<SurgeryNameConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<SurgeryNameConfig> {
        val name = parts[fields["name"]!!]
        return ValidatedCurationConfig(
            SurgeryNameConfig(
                input = parts[fields["input"]!!],
                ignore = CurationUtil.isIgnoreString(name),
                name = name
            )
        )
    }
}


