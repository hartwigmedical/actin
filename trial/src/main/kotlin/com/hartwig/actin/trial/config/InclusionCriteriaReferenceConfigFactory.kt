package com.hartwig.actin.trial.config

class InclusionCriteriaReferenceConfigFactory : TrialConfigFactory<InclusionCriteriaReferenceConfig> {

    override fun create(fields: Map<String, Int>, parts: List<String>): InclusionCriteriaReferenceConfig {
        require(parts.size == 3) {
            "Invalid criteria reference config provided. Possibly due to hard line breaks? ('" + parts.joinToString(" ") + "')"
        }
        return InclusionCriteriaReferenceConfig(
            nctId = parts[fields["trialId"]!!],
            referenceId = parts[fields["referenceId"]!!],
            referenceText = parts[fields["referenceText"]!!]
        )
    }
}