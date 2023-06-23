package com.hartwig.actin.treatment.trial.config

class InclusionCriteriaReferenceConfigFactory : TrialConfigFactory<InclusionCriteriaReferenceConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): InclusionCriteriaReferenceConfig {
        require(parts.size == 3) {
            "Invalid criteria reference config provided. Possibly due to hard line breaks? ('" + parts.joinToString(" ") + "')"
        }
        return InclusionCriteriaReferenceConfig(
            trialId = parts[fields["trialId"]!!],
            referenceId = parts[fields["referenceId"]!!],
            referenceText = parts[fields["referenceText"]!!]
        )
    }
}