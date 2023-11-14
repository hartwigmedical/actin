package com.hartwig.actin.trial

import com.hartwig.actin.treatment.datamodel.Trial
import com.hartwig.actin.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.config.InclusionCriteriaConfig
import com.hartwig.actin.trial.config.InclusionCriteriaReferenceConfig
import com.hartwig.actin.trial.config.TrialConfig
import com.hartwig.actin.trial.config.TrialDefinitionConfig

enum class TrialIngestionStatus {
    PASS,
    WARN
}

interface TrialDatabaseValidationWarning {
    val config: TrialConfig
    val message: String
}

data class CTCTrialMappingWarning(val trialId: String, val message: String)
data class CTCCohortMappingWarning(val studyMETC: String, val cohortName: String?, val cohortId: Int?, val message: String)
data class InclusionCriteriaValidationWarning(
    override val config: InclusionCriteriaConfig,
    override val message: String
) : TrialDatabaseValidationWarning

data class InclusionReferenceValidationWarning(
    override val config: InclusionCriteriaReferenceConfig,
    override val message: String
) : TrialDatabaseValidationWarning

data class CohortDefinitionValidationWarning(
    override val config: CohortDefinitionConfig,
    override val message: String
) : TrialDatabaseValidationWarning

data class TrialDefinitionValidationWarning(
    override val config: TrialDefinitionConfig,
    override val message: String
) : TrialDatabaseValidationWarning


data class TrialIngestionResult(
    val ingestionStatus: TrialIngestionStatus,
    val trialMappingWarnings: List<CTCTrialMappingWarning>,
    val cohortMappingWarnings: List<CTCCohortMappingWarning>,
    val trialDatabaseValidationWarnings: List<TrialDatabaseValidationWarning>,
    @Transient val trials: List<Trial>
)