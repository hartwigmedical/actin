package com.hartwig.actin.trial.config

import com.hartwig.actin.util.ResourceFile

class TrialDefinitionConfigFactory : TrialConfigFactory<TrialDefinitionConfig> {

    override fun create(fields: Map<String, Int>, parts: List<String>): TrialDefinitionConfig {
        return TrialDefinitionConfig(
            nctId = parts[fields["nctId"]!!],
            open = ResourceFile.optionalBool(parts[fields["open"]!!]),
            acronym = parts[fields["acronym"]!!],
            title = parts[fields["title"]!!],
            phase = ResourceFile.optionalString(parts[fields["phase"]!!])
        )
    }
}