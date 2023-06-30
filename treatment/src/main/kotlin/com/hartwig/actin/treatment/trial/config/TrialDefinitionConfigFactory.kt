package com.hartwig.actin.treatment.trial.config

import com.hartwig.actin.util.ResourceFile

class TrialDefinitionConfigFactory : TrialConfigFactory<TrialDefinitionConfig> {
    override fun create(fields: Map<String, Int>, parts: List<String>): TrialDefinitionConfig {
        return TrialDefinitionConfig(
            trialId = parts[fields["trialId"]!!],
            open = ResourceFile.bool(parts[fields["open"]!!]),
            acronym = parts[fields["acronym"]!!],
            title = parts[fields["title"]!!]
        )
    }
}