package com.hartwig.actin.trial

import com.hartwig.actin.datamodel.trial.Cohort
import com.hartwig.actin.datamodel.trial.CohortMetadata
import com.hartwig.actin.datamodel.trial.CriterionReference
import com.hartwig.actin.datamodel.trial.Eligibility
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.datamodel.trial.Trial
import com.hartwig.actin.datamodel.trial.TrialIdentification
import com.hartwig.actin.datamodel.trial.TrialPhase
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.trial.serialization.TrialJson
import com.hartwig.actin.util.Either
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val TRIAL_ID = "trialId"
private const val NCT_ID = "nctId"
private const val ACRONYM = "acronym"
private const val TITLE = "title"
private const val IS_MALE = "IS_MALE"
private const val IS_FEMALE = "IS_FEMALE"
private const val COHORT_ID = "cohortId"
private const val DESCRIPTION = "description"
private const val REFERENCE_ID = "id"
private const val REFERENCE_TEXT = "text"
private const val LOCATION = "location"

class TrialIngestionTest {

    @Test
    fun `Reorder`() {
        val new = TrialJson.readFromDir("/Users/pwolfe/Code/actin/new_trial")
        TrialJson.write(new, "/Users/pwolfe/Code/actin/new_trial")
    }

    @Test
    fun `Regress`() {
        val old = TrialJson.readFromDir("/Users/pwolfe/Code/actin/old_trial").associateBy { it.identification.trialId }
        val new = TrialJson.readFromDir("/Users/pwolfe/Code/actin/new_trial").associateBy { it.identification.trialId }
        for (entry in old) {
            val newTrial = new[entry.key]
            if (newTrial != null) {
                val oldTrial = entry.value
                if (newTrial.identification.trialId == "M22PDL") {
                    assertThat(
                        newTrial.copy(
                            identification = newTrial.identification.copy(
                                nctId = null,
                                locations = emptyList()
                            )
                        )
                    ).isEqualTo(
                        oldTrial.copy(identification = oldTrial.identification.copy(nctId = null, locations = emptyList()))
                    )
                }
            }
        }
    }

    @Test
    fun `Should map trial config to internal trial model and eligibility criteria`() {
        val ingestion = TrialIngestion(TestEligibilityFactoryFactory.createTestEligibilityFactory())
        val result = ingestion.ingest(
            listOf(
                TrialConfig(
                    trialId = TRIAL_ID,
                    source = TrialSource.NKI,
                    nctId = NCT_ID,
                    open = true,
                    acronym = ACRONYM,
                    title = TITLE,
                    phase = TrialPhase.PHASE_1,
                    inclusionCriterion = listOf(
                        InclusionCriterionConfig(IS_MALE, listOf(InclusionCriterionReferenceConfig(REFERENCE_ID, REFERENCE_TEXT)))
                    ),
                    cohorts = listOf(
                        CohortConfig(
                            cohortId = COHORT_ID,
                            open = true,
                            slotsAvailable = true,
                            description = DESCRIPTION,
                            ignore = false,
                            evaluable = true,
                            inclusionCriterion = listOf(
                                InclusionCriterionConfig(
                                    IS_FEMALE, listOf(
                                        InclusionCriterionReferenceConfig(
                                            REFERENCE_ID,
                                            REFERENCE_TEXT
                                        )
                                    )
                                )
                            )
                        )
                    ),
                    locations = listOf(LOCATION)
                )
            )
        ) as Either.Right
        assertThat(result.right).containsExactly(
            Trial(
                identification = TrialIdentification(
                    trialId = TRIAL_ID,
                    open = true,
                    acronym = ACRONYM,
                    title = TITLE,
                    nctId = NCT_ID,
                    phase = TrialPhase.PHASE_1,
                    source = TrialSource.NKI,
                    locations = listOf(LOCATION)
                ),
                generalEligibility = listOf(
                    Eligibility(
                        setOf(CriterionReference(REFERENCE_ID, REFERENCE_TEXT)),
                        EligibilityFunction(EligibilityRule.IS_MALE)
                    )
                ),
                cohorts = listOf(
                    Cohort(
                        metadata = CohortMetadata(
                            cohortId = COHORT_ID,
                            open = true,
                            slotsAvailable = true,
                            description = DESCRIPTION,
                            evaluable = true,
                            ignore = false
                        ),
                        eligibility = listOf(
                            Eligibility(
                                setOf(CriterionReference(REFERENCE_ID, REFERENCE_TEXT)),
                                EligibilityFunction(EligibilityRule.IS_FEMALE)
                            )
                        )
                    )
                )
            )
        )
    }

}