package com.hartwig.actin.trial

import com.hartwig.actin.datamodel.trial.Cohort
import com.hartwig.actin.datamodel.trial.CohortMetadata
import com.hartwig.actin.datamodel.trial.CriterionReference
import com.hartwig.actin.datamodel.trial.Eligibility
import com.hartwig.actin.datamodel.trial.Trial
import com.hartwig.actin.datamodel.trial.TrialIdentification
import com.hartwig.actin.util.Either
import com.hartwig.actin.util.left
import com.hartwig.actin.util.partitionAndJoin
import com.hartwig.actin.util.right

data class UnmappableTrial(
    val trialId: String,
    val mappingErrors: List<EligibilityMappingError>,
    val unmappableCohorts: List<UnmappableCohort>
)

data class UnmappableCohort(val cohortId: String, val mappingErrors: List<EligibilityMappingError>)

data class EligibilityMappingError(val inclusionRule: String, val error: String)

class TrialIngestion(private val eligibilityFactory: EligibilityFactory) {

    fun ingest(config: List<TrialConfig>): Either<List<UnmappableTrial>, List<Trial>> {
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
            if (unmappableCohorts.isEmpty() && trialErrors.isEmpty())
                Trial(
                    identification = TrialIdentification(
                        trialId = trialState.trialId,
                        open = trialState.open,
                        acronym = trialState.acronym,
                        title = trialState.title,
                        nctId = trialState.nctId,
                        phase = trialState.phase,
                        source = trialState.source,
                        sourceId = trialState.sourceId,
                        locations = trialState.locations
                    ),
                    generalEligibility = criteria,
                    cohorts = mappedCohorts
                ).right()
            else UnmappableTrial(trialId = trialState.trialId, trialErrors, unmappableCohorts).left()
        }
        val (errors, trials) = trialsAndUnmappableTrials.partitionAndJoin()
        return if (errors.isNotEmpty()) errors.left() else trials.right()
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
}