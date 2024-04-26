package com.hartwig.actin.trial

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.hartwig.actin.trial.config.TrialConfig
import com.hartwig.actin.trial.config.TrialDatabaseValidation
import com.hartwig.actin.trial.datamodel.Trial
import com.hartwig.actin.trial.status.TrialStatusDatabaseValidation
import java.lang.reflect.Type

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
    val unusedRules: Set<String>,
    @Transient val trials: List<Trial>
) {
    fun serialize(): String {
        return GsonBuilder().registerTypeHierarchyAdapter(ValidationError::class.java, ValidationErrorSerializer())
            .create()
            .toJson(this)
    }
}

class ValidationErrorSerializer : JsonSerializer<ValidationError<*>> {
    override fun serialize(src: ValidationError<*>, p1: Type, context: JsonSerializationContext): JsonElement {
        val serialized = Gson().toJsonTree(src)
        serialized.asJsonObject.addProperty("warningMessage", src.warningMessage())
        return serialized
    }
}