package com.hartwig.actin.trial.config

import com.hartwig.actin.util.ResourceFile

class CohortDefinitionConfigFactory : TrialConfigFactory<CohortDefinitionConfig> {

    override fun create(fields: Map<String, Int>, parts: List<String>): CohortDefinitionConfig {
        return CohortDefinitionConfig(
            trialId = parts[fields["trialId"]!!],
            cohortId = parts[fields["cohortId"]!!],
            externalCohortIds = toSet(parts[fields["externalCohortIds"]!!]),
            evaluable = ResourceFile.bool(parts[fields["evaluable"]!!]),
            open = ResourceFile.optionalBool(parts[fields["open"]!!]),
            slotsAvailable = ResourceFile.optionalBool(parts[fields["slotsAvailable"]!!]),
            ignore = ResourceFile.bool(parts[fields["ignore"]!!]),
            description = parts[fields["description"]!!]
        )
    }

    private fun toSet(rawString: String): Set<String> {
        return if (rawString.isEmpty()) emptySet() else {
            rawString.split(DELIMITER).dropLastWhile { it.isEmpty() }.toSet()
        }
    }

    companion object {
        private const val DELIMITER = ";"
    }
}