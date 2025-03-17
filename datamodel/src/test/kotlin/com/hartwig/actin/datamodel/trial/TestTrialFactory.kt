package com.hartwig.actin.datamodel.trial

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
                source = TrialSource.EMC
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
                locations = listOf("Amsterdam UMC", "Antoni van Leeuwenhoek")
            ),
            generalEligibility = createGeneralEligibility(),
            cohorts = createTestCohorts(),
        )
    }

    fun createTrialWithIhcRules(): Trial {
        val minimal = createMinimalTestTrial()
        return createMinimalTestTrial().copy(
            identification = minimal.identification.copy(
                acronym = "TEST-TRIAL",
                title = "This is an ACTIN test trial",
                locations = listOf("Amsterdam UMC", "Antoni van Leeuwenhoek")
            ),
            cohorts = createTestCohorts(),
            generalEligibility = listOf(
                Eligibility(
                    function = EligibilityFunction(rule = EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC, parameters = listOf("ABC")),
                    references = setOf(CriterionReference(id = "I-01", text = "ref 01"))
                ),
                Eligibility(
                    function = EligibilityFunction(
                        rule = EligibilityRule.EXPRESSION_OF_PROTEIN_X_BY_IHC_OF_EXACTLY_Y,
                        parameters = listOf("DEF", "1")
                    ),
                    references = setOf(CriterionReference(id = "I-02", text = "ref 02"))
                ),
                Eligibility(
                    function = EligibilityFunction(rule = EligibilityRule.PD_L1_SCORE_CPS_OF_AT_LEAST_X, parameters = listOf("1")),
                    references = setOf(CriterionReference(id = "I-03", text = "ref 03"))
                )
            )
        )
    }

    private fun createGeneralEligibility(): List<Eligibility> {
        return listOf(
            Eligibility(
                function = EligibilityFunction(rule = EligibilityRule.IS_AT_LEAST_X_YEARS_OLD, parameters = listOf("18")),
                references = setOf(CriterionReference(id = "I-01", text = "Is adult"))
            )
        )
    }

    private fun createTestCohorts(): List<Cohort> {
        return listOf(
            Cohort(
                metadata = createTestMetadata("A"),
                eligibility = listOf(
                    Eligibility(
                        function = EligibilityFunction(
                            rule = EligibilityRule.NOT,
                            parameters = listOf(
                                EligibilityFunction(rule = EligibilityRule.HAS_KNOWN_ACTIVE_CNS_METASTASES, parameters = emptyList())
                            )
                        ),
                        references = setOf(
                            CriterionReference(id = "E-01", text = "Has no active CNS metastases and has exhausted SOC")
                        )
                    )
                )
            ),
            Cohort(
                metadata = createTestMetadata("B"),
                eligibility = listOf(
                    Eligibility(
                        function = EligibilityFunction(rule = EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS, parameters = emptyList()),
                        references = setOf(
                            CriterionReference(id = "E-01", text = "Has no active CNS metastases and has exhausted SOC")
                        )
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
            open = true,
            slotsAvailable = true,
            ignore = false,
            description = "Cohort $cohortId"
        )
    }
}
