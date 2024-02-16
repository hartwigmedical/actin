package com.hartwig.actin.trial.config

interface CohortDefinitionConfig : TrialConfig {
    val cohortId: String
    val ctcCohortIds: Set<String>
    val evaluable: Boolean
    val open: Boolean?
    val slotsAvailable: Boolean?
    val blacklist: Boolean
    val description: String

    fun ctcCohortIds()

    fun open(): Boolean?
    fun slotsAvailable(): Boolean?
}

data class EmcCohortDefinitionConfig(
    override val trialId: String,
    override val cohortId: String,
    override val ctcCohortIds: Set<String>,
    override val evaluable: Boolean,
    override val open: Boolean?,
    override val slotsAvailable: Boolean?,
    override val blacklist: Boolean,
    override val description: String
) : CohortDefinitionConfig {
    override fun ctcCohortIds() {
        ctcCohortIds
    }

    override fun open(): Boolean? {
        return open
    }

    override fun slotsAvailable(): Boolean? {
        return slotsAvailable
    }
}

data class NkiCohortDefinitionConfig(
    override val cohortId: String,
    override val evaluable: Boolean,
    override val open: Boolean?,
    override val slotsAvailable: Boolean?,
    override val blacklist: Boolean,
    override val description: String,
    override val trialId: String
) : CohortDefinitionConfig {
    override val ctcCohortIds: Set<String>
        get() = throw UnsupportedOperationException("No CTC cohort ids expected for NKI")

    override fun ctcCohortIds() {
        throw UnsupportedOperationException("No CTC cohort ids expected for NKI")
    }

    override fun open(): Boolean? {
        return open
    }

    override fun slotsAvailable(): Boolean? {
        return slotsAvailable
    }
}
