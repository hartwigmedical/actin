package com.hartwig.pipeline.trial

import com.hartwig.actin.algo.Either
import com.hartwig.actin.algo.EligibilityFactory
import com.hartwig.actin.algo.left
import com.hartwig.actin.algo.partitionAndJoin
import com.hartwig.actin.algo.right
import com.hartwig.actin.datamodel.trial.Cohort
import com.hartwig.actin.datamodel.trial.CohortAvailability
import com.hartwig.actin.datamodel.trial.CohortMetadata
import com.hartwig.actin.datamodel.trial.Eligibility
import com.hartwig.actin.datamodel.trial.InclusionCriterionConfig
import com.hartwig.actin.datamodel.trial.Trial
import com.hartwig.actin.datamodel.trial.TrialConfig
import com.hartwig.actin.datamodel.trial.TrialIdentification
import com.hartwig.actin.datamodel.trial.TrialSource

data class UnmappableTrial(
    val trialId: String,
    val mappingErrors: List<EligibilityMappingError>,
    val unmappableCohorts: List<UnmappableCohort>
)

data class UnmappableCohort(val cohortId: String, val mappingErrors: List<EligibilityMappingError>)

data class EligibilityMappingError(val inclusionRule: String, val error: String)

data class TrialAndEligibilityRules(val trial: Trial, val rules: List<String>)

class TrialIngestion(private val eligibilityFactory: EligibilityFactory) {

    fun ingest(config: List<TrialConfig>): Either<List<UnmappableTrial>, List<Trial>> {
        val trialsAndUnmappableTrials = config.map { trialState ->
            val (trialErrors, criteria) = trialState.inclusionCriterion.map {
                toEligibility(
                    inclusionCriterion = it
                )
            }.partitionAndJoin()
            val (unmappableCohorts, mappedCohorts) = trialState.cohorts.map { cohortConfig ->
                val (cohortMappingErrors, cohortCriteria) = cohortConfig.inclusionCriterion.map {
                    toEligibility(
                        inclusionCriterion = it
                    )
                }.partitionAndJoin()
                if (cohortMappingErrors.isEmpty()) Cohort(
                    metadata = CohortMetadata(
                        cohortId = cohortConfig.cohortId,
                        cohortAvailability = CohortAvailability(
                            open = cohortConfig.open,
                            slotsAvailable = cohortConfig.slotsAvailable,
                        ),
                        availabilityByLocation = cohortConfig.cohortAvailabilityConfig.mapValues {
                            CohortAvailability(
                                it.value?.open ?: false,
                                it.value?.slotsAvailable ?: false
                            )
                        },
                        description = cohortConfig.description,
                        evaluable = cohortConfig.evaluable,
                        ignore = cohortConfig.ignore
                    ),
                    eligibility = cohortCriteria
                ).right() else UnmappableCohort(cohortId = cohortConfig.cohortId, cohortMappingErrors).left()
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

        return with(trialWrappers) { map { it.trial } to flatMap { it.rules }.distinct() }.right()
    }

    private fun toEligibility(inclusionCriterion: InclusionCriterionConfig) =
        try {
            Eligibility(
                inclusionCriterion.references?.toSet() ?: emptySet(),
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