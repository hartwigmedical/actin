package com.hartwig.actin.trial

import com.hartwig.actin.datamodel.trial.Cohort
import com.hartwig.actin.datamodel.trial.CohortMetadata
import com.hartwig.actin.datamodel.trial.CriterionReference
import com.hartwig.actin.datamodel.trial.Eligibility
import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.datamodel.trial.EligibilityRuleState
import com.hartwig.actin.datamodel.trial.EligibilityRuleUsedStatus
import com.hartwig.actin.datamodel.trial.Trial
import com.hartwig.actin.datamodel.trial.TrialIdentification
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.util.Either
import com.hartwig.actin.util.left
import com.hartwig.actin.util.partitionAndJoin
import com.hartwig.actin.util.right

data class TrialIngestSuccessResult(
    val trials: List<Trial>,
    val eligibilityRulesState: List<EligibilityRuleState>
)

data class UnmappableTrial(
    val trialId: String,
    val mappingErrors: List<EligibilityMappingError>,
    val unmappableCohorts: List<UnmappableCohort>
)

data class UnmappableCohort(val cohortId: String, val mappingErrors: List<EligibilityMappingError>)

data class EligibilityMappingError(val inclusionRule: String, val error: String)

data class TrialAndEligibilityRules(val trial: Trial, val rules: List<EligibilityRule>)

class TrialIngestion(private val eligibilityFactory: EligibilityFactory) {

    fun ingest(config: List<TrialConfig>): Either<List<UnmappableTrial>, TrialIngestSuccessResult> {
        val trialsAndUnmappableTrials = config.map { trialState ->
            val (trialErrors, criteria) = trialState.inclusionCriterion.map {
                toEligibility(
                    inclusionCriterion = it
                )
            }.partitionAndJoin()
            val (unmappableCohorts, mappedCohorts) = trialState.cohorts.map { cohortState ->
                val (cohortMappingErrors, cohortCriteria) = cohortState.inclusionCriterion.map {
                    toEligibility(
                        inclusionCriterion = it
                    )
                }.partitionAndJoin()
                if (cohortMappingErrors.isEmpty()) Cohort(
                    metadata = CohortMetadata(
                        cohortId = cohortState.cohortId,
                        open = cohortState.open,
                        slotsAvailable = cohortState.slotsAvailable,
                        description = cohortState.description,
                        evaluable = cohortState.evaluable,
                        ignore = cohortState.ignore
                    ),
                    eligibility = cohortCriteria
                ).right() else UnmappableCohort(cohortId = cohortState.cohortId, cohortMappingErrors).left()
            }.partitionAndJoin()
            if (unmappableCohorts.isEmpty() && trialErrors.isEmpty()) {
                val trial = Trial(
                    identification = TrialIdentification(
                        trialId = trialState.trialId,
                        open = trialState.open,
                        acronym = trialState.acronym,
                        title = trialState.title,
                        nctId = trialState.nctId,
                        phase = trialState.phase,
                        source = trialState.source,
                        sourceId = trialState.sourceId,
                        locations = trialState.locations.toSet(),
                        url = createTrialUrl(trialState)
                    ),
                    generalEligibility = criteria,
                    cohorts = mappedCohorts
                )
                TrialAndEligibilityRules(
                    trial = trial,
                    rules = criteria.map { it.function.rule } +
                            mappedCohorts.flatMap { cohort -> cohort.eligibility.map { it.function.rule } }
                ).right()
            } else {
                UnmappableTrial(trialId = trialState.trialId, trialErrors, unmappableCohorts).left()
            }
        }
        val (errors, trialWrappers) = trialsAndUnmappableTrials.partitionAndJoin()
        if (errors.isNotEmpty()) {
            return errors.left()
        }

        val (trials, usedRules) = with(trialWrappers) { map { it.trial } to flatMap { it.rules }.distinct() }
        val eligibilityRulesState = EligibilityRule.entries.map { rule ->
            EligibilityRuleState(rule, if (rule in usedRules) EligibilityRuleUsedStatus.USED else EligibilityRuleUsedStatus.UNUSED)
        }
        return TrialIngestSuccessResult(
            trials = trials,
            eligibilityRulesState = eligibilityRulesState
        ).right()
    }

    private fun toEligibility(inclusionCriterion: InclusionCriterionConfig) =
        try {
            Eligibility(
                inclusionCriterion.references?.map { CriterionReference(it.id, it.text) }?.toSet() ?: emptySet(),
                eligibilityFactory.generateEligibilityFunction(inclusionCriterion.inclusionRule)
            ).right()
        } catch (e: Exception) {
            EligibilityMappingError(inclusionCriterion.inclusionRule, e.message ?: "Unknown").left()
        }

    private fun createTrialUrl(trialConfig: TrialConfig): String? {
        return if (trialConfig.source == TrialSource.LKO) {
            trialConfig.sourceId?.let { "https://longkankeronderzoek.nl/studies/$it" }
        } else {
            trialConfig.nctId?.let { "https://clinicaltrials.gov/study/$it" }
        }
    }
}