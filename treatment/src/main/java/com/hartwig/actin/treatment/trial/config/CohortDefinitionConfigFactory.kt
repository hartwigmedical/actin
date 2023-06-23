package com.hartwig.actin.treatment.trial.config

import com.hartwig.actin.util.ResourceFile

class CohortDefinitionConfigFactory : TrialConfigFactory<CohortDefinitionConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): CohortDefinitionConfig {
        return CohortDefinitionConfig(
            trialId = parts[fields["trialId"]!!],
            cohortId = parts[fields["cohortId"]!!],
            evaluable = ResourceFile.bool(parts[fields["evaluable"]!!]),
            open = ResourceFile.bool(parts[fields["open"]!!]),
            slotsAvailable = ResourceFile.bool(parts[fields["slotsAvailable"]!!]),
            blacklist = ResourceFile.bool(parts[fields["blacklist"]!!]),
            description = parts[fields["description"]!!]
        )
    }
}