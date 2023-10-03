package com.hartwig.actin.trial.trial.config

import com.hartwig.actin.trial.trial.TrialConfigDatabaseUtil

class InclusionCriteriaConfigFactory : TrialConfigFactory<InclusionCriteriaConfig> {
    override fun create(fields: Map<String, Int>, parts: List<String>): InclusionCriteriaConfig {
        return InclusionCriteriaConfig(
            trialId = parts[fields["trialId"]!!],
            referenceIds = TrialConfigDatabaseUtil.toReferenceIds(parts[fields["referenceIds"]!!]),
            appliesToCohorts = TrialConfigDatabaseUtil.toCohorts(parts[fields["appliesToCohorts"]!!]),
            inclusionRule = parts[fields["inclusionRule"]!!]
        )
    }
}