package com.hartwig.actin.trial

import com.hartwig.actin.treatment.datamodel.Trial
import com.hartwig.actin.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.config.InclusionCriteriaConfig
import com.hartwig.actin.trial.config.InclusionCriteriaReferenceConfig
import com.hartwig.actin.trial.config.TrialConfig
import com.hartwig.actin.trial.config.TrialDefinitionConfig

enum class IngestionStatus {
    PASS,
    WARN
}

interface TrialWarning {
    val config: TrialConfig
    val message: String
}

data class CTCTrialMappingWarning(val trialId: String, val message: String)
data class CTCCohortMappingWarning(val studyMETC: String, val cohortName: String?, val cohortId: Int?, val message: String)
data class TrialInclusionCriteriaValidationWarning(
    override val config: InclusionCriteriaConfig,
    override val message: String
) : TrialWarning

data class TrialInclusionReferenceValidationWarning(
    override val config: InclusionCriteriaReferenceConfig,
    override val message: String
) : TrialWarning

data class TrialCohortValidationWarning(
    override val config: CohortDefinitionConfig,
    override val message: String
) : TrialWarning

data class TrialDefinitionValidationWarning(
    override val config: TrialDefinitionConfig,
    override val message: String
) : TrialWarning


data class TrialIngestionResult(
    val ingestionStatus: IngestionStatus,
    val trialMappingWarnings: List<CTCTrialMappingWarning>,
    val cohortMappingWarnings: List<CTCCohortMappingWarning>,
    val trialDefinitionWarnings: List<TrialWarning>,
    @Transient val trials: List<Trial>
)