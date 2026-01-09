package com.hartwig.actin.algo

import com.hartwig.actin.datamodel.trial.Cohort
import com.hartwig.actin.datamodel.trial.CohortAvailability
import com.hartwig.actin.datamodel.trial.CohortMetadata
import com.hartwig.actin.datamodel.trial.Eligibility
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.FunctionParameter
import com.hartwig.actin.datamodel.trial.IntegerParameter
import com.hartwig.actin.datamodel.trial.Trial
import com.hartwig.actin.datamodel.trial.TrialIdentification
import com.hartwig.actin.datamodel.trial.TrialSource

object TestTrialFactory {

    private const val TEST_TRIAL = "test trial"

    fun createMinimalTestTrial(): Trial {
        return Trial(
            identification = TrialIdentification(
                trialId = TEST_TRIAL,
                open = true,
                acronym = "",
                title = "",
                nctId = null,
                phase = null,
                source = TrialSource.EMC,
                sourceId = null,
                locations = emptySet(),
                url = null
            ),
            cohorts = emptyList(),
            generalEligibility = emptyList()
        )
    }

    fun createProperTestTrial(): Trial {
        val minimal = createMinimalTestTrial()
        return minimal.copy(
            identification = minimal.identification.copy(
                acronym = "TEST-TRIAL",
                title = "This is an ACTIN test trial",
                locations = setOf("Amsterdam UMC", "Antoni van Leeuwenhoek")
            ),
            generalEligibility = createGeneralEligibility(),
            cohorts = createTestCohorts(),
        )
    }

    private fun createGeneralEligibility(): List<Eligibility> {
        return listOf(
            Eligibility(
                references = setOf("I-01"),
                function = EligibilityFunction(rule = "IS_AT_LEAST_X_YEARS_OLD", parameters = listOf(IntegerParameter(18)))
            )
        )
    }

    private fun createTestCohorts(): List<Cohort> {
        return listOf(
            Cohort(
                metadata = createTestMetadata("A"),
                eligibility = listOf(
                    Eligibility(
                        references = setOf("E-01"),
                        function = EligibilityFunction(
                            rule = "NOT",
                            parameters = listOf(
                                FunctionParameter(EligibilityFunction(rule = "HAS_KNOWN_ACTIVE_CNS_METASTASES", parameters = emptyList()))
                            )
                        )
                    )
                )
            ),
            Cohort(
                metadata = createTestMetadata("B"),
                eligibility = listOf(
                    Eligibility(
                        references = setOf("E-01"),
                        function = EligibilityFunction(rule = "HAS_EXHAUSTED_SOC_TREATMENTS", parameters = emptyList())
                    )
                )
            ),
            Cohort(metadata = createTestMetadata("C"), eligibility = emptyList()),
            Cohort(metadata = createTestMetadata("D", false), eligibility = emptyList())
        )
    }

    private fun createTestMetadata(cohortId: String, evaluable: Boolean = true): CohortMetadata {
        return CohortMetadata(
            cohortId = cohortId,
            evaluable = evaluable,
            cohortAvailability = CohortAvailability(
                open = true,
                slotsAvailable = true
            ),
            ignore = false,
            description = "Cohort $cohortId"
        )
    }
}
