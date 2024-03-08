package com.hartwig.actin.trial.config

import com.hartwig.actin.util.ResourceFile

class TrialDefinitionConfigFactory : TrialConfigFactory<TrialDefinitionConfig> {

    override fun create(fields: Map<String, Int>, parts: List<String>): TrialDefinitionConfig {
        return TrialDefinitionConfig(
            trialId = parts[fields["trialId"]!!],
            open = ResourceFile.optionalBool(parts[fields["open"]!!]),
            acronym = parts[fields["acronym"]!!],
            title = parts[fields["title"]!!],
            nctId = parts[fields["nctId"]!!],
        )
    }
}