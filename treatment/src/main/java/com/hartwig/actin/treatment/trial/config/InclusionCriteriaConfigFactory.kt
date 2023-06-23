package com.hartwig.actin.treatment.trial.config

import com.hartwig.actin.treatment.trial.TrialConfigDatabaseUtil

class InclusionCriteriaConfigFactory : TrialConfigFactory<InclusionCriteriaConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): InclusionCriteriaConfig {
        return InclusionCriteriaConfig(
            trialId = parts[fields["trialId"]!!],
            referenceIds = TrialConfigDatabaseUtil.toReferenceIds(parts[fields["referenceIds"]!!]),
            appliesToCohorts = TrialConfigDatabaseUtil.toCohorts(parts[fields["appliesToCohorts"]!!]),
            inclusionRule = parts[fields["inclusionRule"]!!]
        )
    }
}