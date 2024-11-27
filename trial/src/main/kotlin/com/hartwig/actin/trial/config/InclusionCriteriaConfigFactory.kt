package com.hartwig.actin.trial.config

class InclusionCriteriaConfigFactory : TrialConfigFactory<InclusionCriteriaConfig> {

    override fun create(fields: Map<String, Int>, parts: List<String>): InclusionCriteriaConfig {
        return InclusionCriteriaConfig(
            nctId = parts[fields["trialId"]!!],
            referenceIds = TrialConfigDatabaseUtil.toReferenceIds(parts[fields["referenceIds"]!!]),
            appliesToCohorts = TrialConfigDatabaseUtil.toCohorts(parts[fields["appliesToCohorts"]!!]),
            inclusionRule = parts[fields["inclusionRule"]!!]
        )
    }
}