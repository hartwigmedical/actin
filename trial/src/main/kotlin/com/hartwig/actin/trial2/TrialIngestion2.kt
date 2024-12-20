package com.hartwig.actin.trial2

import com.hartwig.actin.datamodel.trial.Cohort
import com.hartwig.actin.datamodel.trial.CohortMetadata
import com.hartwig.actin.datamodel.trial.CriterionReference
import com.hartwig.actin.datamodel.trial.Eligibility
import com.hartwig.actin.datamodel.trial.Trial
import com.hartwig.actin.datamodel.trial.TrialIdentification
import com.hartwig.actin.trial.interpretation.EligibilityFactory

class TrialIngestion2(private val eligibilityFactory: EligibilityFactory) {

    fun ingest(config: List<TrialState>): List<Trial> {
        return config.map { trialState ->
            Trial(
                identification = TrialIdentification(
                    trialId = trialState.trialId,
                    open = trialState.open,
                    acronym = trialState.acronym,
                    title = trialState.title,
                    nctId = trialState.nctId,
                    phase = trialState.phase,
                    source = trialState.source,
                    locations = trialState.locations
                ),
                generalEligibility = trialState.inclusionCriterion.map(::toEligibility),
                cohorts = trialState.cohorts.map { cohortState ->
                    Cohort(
                        metadata = CohortMetadata(
                            cohortId = cohortState.cohortId,
                            open = cohortState.open,
                            slotsAvailable = cohortState.slotsAvailable,
                            description = cohortState.description,
                            evaluable = true,
                            ignore = false
                        ),
                        eligibility = cohortState.inclusionCriterion.map(::toEligibility)
                    )
                }
            )
        }
    }

    private fun toEligibility(inclusionCriterion: InclusionCriterion) =
        Eligibility(
            inclusionCriterion.referenceIds?.map { CriterionReference(it.id, it.text) }?.toSet() ?: emptySet(),
            eligibilityFactory.generateEligibilityFunction(inclusionCriterion.inclusionRule)
        )
}