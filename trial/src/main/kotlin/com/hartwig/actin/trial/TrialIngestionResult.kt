package com.hartwig.actin.trial

import com.hartwig.actin.trial.config.TrialConfig
import com.hartwig.actin.trial.config.TrialDatabaseValidation
import com.hartwig.actin.trial.datamodel.Trial
import com.hartwig.actin.trial.status.TrialStatusDatabaseValidation

enum class TrialIngestionStatus {
    PASS,
    FAIL;

    companion object {
        fun from(
            trialStatusDatabaseValidation: TrialStatusDatabaseValidation,
            trialValidationResult: TrialDatabaseValidation,
        ): TrialIngestionStatus {
            return if (trialStatusDatabaseValidation.hasErrors() || trialValidationResult.hasErrors()) FAIL else PASS
        }
    }
}

interface ValidationError<T> : Comparable<ValidationError<T>> {
    val config: T
    val message: String

    fun configFormat(config: T): String
    fun warningMessage(): String {
        return "${this::class.java.simpleName} ${configFormat(config)}: $message"
    }

    override fun compareTo(other: ValidationError<T>): Int {
        return this.message.compareTo(other.message)
    }
}

interface TrialValidationError<T : TrialConfig> : ValidationError<T>


data class TrialIngestionResult(
    val ingestionStatus: TrialIngestionStatus,
    val trialStatusDatabaseValidation: TrialStatusDatabaseValidation,
    val trialValidationResult: TrialDatabaseValidation,
    @Transient val trials: List<Trial>
)