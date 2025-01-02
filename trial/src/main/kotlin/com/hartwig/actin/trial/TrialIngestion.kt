package com.hartwig.actin.trial

import com.hartwig.actin.datamodel.trial.Cohort
import com.hartwig.actin.datamodel.trial.CohortMetadata
import com.hartwig.actin.datamodel.trial.CriterionReference
import com.hartwig.actin.datamodel.trial.Eligibility
import com.hartwig.actin.datamodel.trial.Trial
import com.hartwig.actin.datamodel.trial.TrialIdentification

class TrialIngestion(private val eligibilityFactory: EligibilityFactory) {

    fun ingest(config: List<TrialConfig>): List<Trial> {
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
                            evaluable = cohortState.evaluable,
                            ignore = cohortState.ignore
                        ),
                        eligibility = cohortState.inclusionCriterion.map(::toEligibility)
                    )
                }
            )
        }
    }

    private fun toEligibility(inclusionCriterion: InclusionCriterionConfig) =
        Eligibility(
            inclusionCriterion.references?.map { CriterionReference(it.id, it.text) }?.toSet() ?: emptySet(),
            eligibilityFactory.generateEligibilityFunction(inclusionCriterion.inclusionRule)
        )
}