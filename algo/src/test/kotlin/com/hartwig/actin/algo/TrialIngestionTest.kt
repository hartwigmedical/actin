package com.hartwig.actin.algo

import com.hartwig.actin.datamodel.trial.Cohort
import com.hartwig.actin.datamodel.trial.CohortAvailability
import com.hartwig.actin.datamodel.trial.CohortAvailabilityConfig
import com.hartwig.actin.datamodel.trial.CohortConfig
import com.hartwig.actin.datamodel.trial.CohortMetadata
import com.hartwig.actin.datamodel.trial.Eligibility
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.InclusionCriterionConfig
import com.hartwig.actin.datamodel.trial.Trial
import com.hartwig.actin.datamodel.trial.TrialConfig
import com.hartwig.actin.datamodel.trial.TrialIdentification
import com.hartwig.actin.datamodel.trial.TrialPhase
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.pipeline.trial.TrialIngestion
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

const val TRIAL_ID = "trialId"
const val NCT_ID = "nctId"
const val SOURCE_ID = "sourceId"
const val ACRONYM = "acronym"
const val TITLE = "title"
const val IS_MALE = "IS_MALE"
const val IS_FEMALE = "IS_FEMALE"
const val COHORT_ID = "cohortId"
const val DESCRIPTION = "description"
const val REFERENCE_ID = "id"
const val LOCATION = "location"
class TrialIngestionTest {

    @Test
    fun `Should map trial config to internal trial model and eligibility criteria`() {
        val ingestion = TrialIngestion(EligibilityFactory(mockk()))
        val result = ingestion.ingest(
            listOf(
                TrialConfig(
                    trialId = TRIAL_ID,
                    source = TrialSource.NKI,
                    sourceId = SOURCE_ID,
                    nctId = NCT_ID,
                    open = true,
                    acronym = ACRONYM,
                    title = TITLE,
                    phase = TrialPhase.PHASE_1,
                    inclusionCriterion = listOf(InclusionCriterionConfig(IS_MALE, listOf(REFERENCE_ID))),
                    cohorts = listOf(
                        CohortConfig(
                            cohortId = COHORT_ID,
                            open = true,
                            slotsAvailable = true,
                            cohortAvailabilityConfig = mapOf(
                                LOCATION to CohortAvailabilityConfig(
                                    open = true,
                                    slotsAvailable = true
                                )
                            ),
                            description = DESCRIPTION,
                            ignore = false,
                            evaluable = true,
                            inclusionCriterion = listOf(InclusionCriterionConfig(IS_FEMALE, listOf(REFERENCE_ID)))
                        )
                    ),
                    locations = listOf(LOCATION)
                )
            )
        ) as Either.Right
        assertThat(result.value).containsExactly(
            Trial(
                identification = TrialIdentification(
                    trialId = TRIAL_ID,
                    open = true,
                    acronym = ACRONYM,
                    title = TITLE,
                    nctId = NCT_ID,
                    phase = TrialPhase.PHASE_1,
                    source = TrialSource.NKI,
                    sourceId = SOURCE_ID,
                    locations = setOf(LOCATION),
                    url = "https://clinicaltrials.gov/study/$NCT_ID"
                ),
                generalEligibility = listOf(
                    Eligibility(
                        setOf(REFERENCE_ID),
                        EligibilityFunction("IS_MALE")
                    )
                ),
                cohorts = listOf(
                    Cohort(
                        metadata = CohortMetadata(
                            cohortId = COHORT_ID,
                            cohortAvailability = CohortAvailability(
                                open = true,
                                slotsAvailable = true
                            ),
                            availabilityByLocation = mapOf(LOCATION to CohortAvailability(true, true)),
                            description = DESCRIPTION,
                            evaluable = true,
                            ignore = false
                        ),
                        eligibility = listOf(
                            Eligibility(
                                setOf(REFERENCE_ID),
                                EligibilityFunction("IS_FEMALE")
                            )
                        )
                    )
                )
            )
        )
    }
}