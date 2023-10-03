package com.hartwig.actin.trial.config

import com.hartwig.actin.util.ResourceFile

class CohortDefinitionConfigFactory : TrialConfigFactory<CohortDefinitionConfig> {

    override fun create(fields: Map<String, Int>, parts: List<String>): CohortDefinitionConfig {
        return CohortDefinitionConfig(
            trialId = parts[fields["trialId"]!!],
            cohortId = parts[fields["cohortId"]!!],
            ctcCohortIds = toSet(parts[fields["ctcCohortIds"]!!]),
            evaluable = ResourceFile.bool(parts[fields["evaluable"]!!]),
            open = ResourceFile.optionalBool(parts[fields["open"]!!]),
            slotsAvailable = ResourceFile.optionalBool(parts[fields["slotsAvailable"]!!]),
            blacklist = ResourceFile.bool(parts[fields["blacklist"]!!]),
            description = parts[fields["description"]!!]
        )
    }

    private fun toSet(ctcCohortIdString: String): Set<String> {
        return if (ctcCohortIdString.isEmpty()) emptySet() else {
            ctcCohortIdString.split(CTC_COHORT_DELIMITER).dropLastWhile { it.isEmpty() }.toSet()
        }
    }

    companion object {
        private const val CTC_COHORT_DELIMITER = ";"
    }
}