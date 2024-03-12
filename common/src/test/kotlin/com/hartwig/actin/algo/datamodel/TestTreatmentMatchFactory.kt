package com.hartwig.actin.algo.datamodel

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.efficacy.TestExtendedEvidenceEntryFactory
import com.hartwig.actin.trial.datamodel.CohortMetadata
import com.hartwig.actin.trial.datamodel.CriterionReference
import com.hartwig.actin.trial.datamodel.Eligibility
import com.hartwig.actin.trial.datamodel.EligibilityFunction
import com.hartwig.actin.trial.datamodel.EligibilityRule
import com.hartwig.actin.trial.datamodel.TrialIdentification
import com.hartwig.actin.trial.sort.EligibilityComparator
import java.time.LocalDate

object TestTreatmentMatchFactory {

    fun createMinimalTreatmentMatch(): TreatmentMatch {
        return TreatmentMatch(
            patientId = TestDataFactory.TEST_PATIENT,
            sampleId = TestDataFactory.TEST_SAMPLE,
            trialSource = "EMC",
            referenceDate = LocalDate.of(2021, 8, 2),
            referenceDateIsLive = true,
            trialMatches = emptyList()
        )
    }

    fun createProperTreatmentMatch(): TreatmentMatch {
        return createMinimalTreatmentMatch().copy(trialMatches = createTestTrialMatches(), standardOfCareMatches = createSocMatches())
    }

    private fun createTestTrialMatches(): List<TrialMatch> {
        return listOf(
            TrialMatch(
                identification = TrialIdentification(
                    trialId = "Test Trial 1",
                    open = true,
                    acronym = "TEST-1",
                    title = "Example test trial 1",
                    nctId = "NCT00000010"
                ),
                isPotentiallyEligible = true,
                evaluations = createTestGeneralEvaluationsTrial1(),
                cohorts = createTestCohortsTrial1()
            ),
            TrialMatch(
                identification = TrialIdentification(
                    trialId = "Test Trial 2",
                    open = true,
                    acronym = "TEST-2",
                    title = "Example test trial 2",
                    nctId = "NCT00000002"
                ),
                isPotentiallyEligible = true,
                evaluations = createTestGeneralEvaluationsTrial2(),
                cohorts = createTestCohortsTrial2()
            )
        )
    }

    private fun createSocMatches(): List<AnnotatedTreatmentMatch> {
        return listOf(
            AnnotatedTreatmentMatch(
                treatmentCandidate = TreatmentCandidate(
                    TreatmentTestFactory.treatment("Pembrolizumab", true),
                    true,
                    setOf(EligibilityFunction(rule = EligibilityRule.HAS_KNOWN_ACTIVE_CNS_METASTASES, parameters = emptyList()))
                ),
                evaluations = listOf(
                    Evaluation(
                        result = EvaluationResult.PASS,
                        recoverable = false,
                        passSpecificMessages = setOf("Patient has active CNS metastases"),
                        passGeneralMessages = setOf("Active CNS metastases")
                    )
                ),
                annotations = TestExtendedEvidenceEntryFactory.createProperTestExtendedEvidenceEntries()
            )
        )
    }

    private fun createTestGeneralEvaluationsTrial1(): Map<Eligibility, Evaluation> {
        return sortedMapOf(
            EligibilityComparator(),
            Eligibility(
                function = EligibilityFunction(rule = EligibilityRule.IS_AT_LEAST_X_YEARS_OLD, parameters = emptyList()),
                references = setOf(
                    CriterionReference(
                        id = "I-01",
                        text = "Patient must be an adult"
                    )
                )
            ) to unrecoverable(EvaluationResult.PASS, "Patient is at least 18 years old", "Patient is adult", null),
            Eligibility(
                function = EligibilityFunction(
                    rule = EligibilityRule.NOT, parameters = listOf(
                        EligibilityFunction(rule = EligibilityRule.HAS_KNOWN_ACTIVE_BRAIN_METASTASES, parameters = emptyList())
                    )
                ),
                references = setOf(
                    CriterionReference(
                        id = "I-02",
                        text = "This rule has 2 conditions:\n 1. Patient has no active brain metastases\n 2. Patient has exhausted SOC"
                    )
                )
            ) to unrecoverable(EvaluationResult.PASS, "Patient has no known brain metastases", "No known brain metastases"),
            Eligibility(
                function = EligibilityFunction(rule = EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS, parameters = emptyList()),
                references = setOf(
                    CriterionReference(
                        id = "I-02",
                        text = "This rule has 2 conditions:\n 1. Patient has no active brain metastases.\n 2. Patient has exhausted SOC."
                    )
                )
            ) to unrecoverable(
                EvaluationResult.UNDETERMINED, "Could not be determined if patient has exhausted SOC", "Undetermined SOC exhaustion"
            )
        )
    }

    private fun createTestMetadata(cohortId: String, open: Boolean, slotsAvailable: Boolean): CohortMetadata {
        return CohortMetadata(
            cohortId = cohortId,
            evaluable = true,
            open = open,
            slotsAvailable = slotsAvailable,
            blacklist = false,
            description = "Cohort $cohortId"
        )
    }

    private fun createTestCohortsTrial1(): List<CohortMatch> {
        return listOf(
            CohortMatch(
                metadata = createTestMetadata("A", true, false),
                isPotentiallyEligible = true,
                evaluations = createTestCohortEvaluationsTrial1CohortA()
            ),
            CohortMatch(
                metadata = createTestMetadata("B", true, true),
                isPotentiallyEligible = true,
                evaluations = emptyMap()
            ),
            CohortMatch(
                metadata = createTestMetadata("C", false, false),
                isPotentiallyEligible = false,
                evaluations = createTestCohortEvaluationsTrial1CohortC()
            )
        )
    }

    private fun createTestCohortEvaluationsTrial1CohortA(): Map<Eligibility, Evaluation> {
        return mapOf(
            Eligibility(
                function = EligibilityFunction(rule = EligibilityRule.MSI_SIGNATURE, parameters = emptyList()),
                references = setOf(CriterionReference(id = "I-01", text = "MSI")),
            ) to unrecoverable(EvaluationResult.PASS, "Tumor is MSI", "MSI", "MSI")
        )
    }

    private fun createTestCohortEvaluationsTrial1CohortC(): Map<Eligibility, Evaluation> {
        return mapOf(
            Eligibility(
                function = EligibilityFunction(
                    rule = EligibilityRule.NOT,
                    parameters = listOf(EligibilityFunction(rule = EligibilityRule.HAS_KNOWN_ACTIVE_CNS_METASTASES)),
                ),
                references = setOf(CriterionReference(id = "E-01", text = "Active CNS metastases"))
            ) to unrecoverable(EvaluationResult.FAIL, "Patient has active CNS metastases", "Active CNS metastases", null)
        )
    }

    private fun createTestGeneralEvaluationsTrial2(): Map<Eligibility, Evaluation> {
        return sortedMapOf(
            EligibilityComparator(),
            Eligibility(
                function = EligibilityFunction(rule = EligibilityRule.HAS_MEASURABLE_DISEASE),
                references = setOf(CriterionReference(id = "I-01", text = "Patient should have measurable disease")),
            ) to unrecoverable(EvaluationResult.PASS, "Patient has measurable disease"),
            Eligibility(
                function = EligibilityFunction(rule = EligibilityRule.CAN_GIVE_ADEQUATE_INFORMED_CONSENT),
                references = setOf(CriterionReference(id = "I-02", text = "Patient should be able to give adequate informed consent"))
            ) to unrecoverable(EvaluationResult.NOT_EVALUATED, "It is assumed that patient can provide adequate informed consent")
        )
    }

    private fun createTestCohortsTrial2(): List<CohortMatch> {
        return listOf(
            CohortMatch(
                metadata = createTestMetadata("A", true, false),
                isPotentiallyEligible = true,
                evaluations = createTestCohortEvaluationsTrial2CohortA(),
            ),
            CohortMatch(
                metadata = createTestMetadata("B", true, true),
                isPotentiallyEligible = false,
                evaluations = createTestCohortEvaluationsTrial2CohortB(),
            )
        )
    }

    private fun createTestCohortEvaluationsTrial2CohortA(): Map<Eligibility, Evaluation> {
        return mapOf(
            Eligibility(
                function = EligibilityFunction(rule = EligibilityRule.MSI_SIGNATURE, parameters = emptyList()),
                references = setOf(CriterionReference(id = "I-01", text = "MSI")),
            ) to unrecoverable(EvaluationResult.PASS, "Tumor is MSI", "MSI", "MSI")
        )
    }

    private fun createTestCohortEvaluationsTrial2CohortB(): Map<Eligibility, Evaluation> {
        return mapOf(
            Eligibility(
                function = EligibilityFunction(
                    rule = EligibilityRule.NOT, parameters = listOf(
                        EligibilityFunction(rule = EligibilityRule.HAS_KNOWN_ACTIVE_CNS_METASTASES)
                    )
                ),
                references = setOf(CriterionReference(id = "I-03", text = "Patient should not have had pembrolizumab treatment"))
            ) to unrecoverable(EvaluationResult.FAIL, "Patient has had pembrolizumab treatment", "Pembrolizumab treatment", null)
        )
    }

    private fun unrecoverable(
        result: EvaluationResult, specificMessage: String,
        generalMessage: String? = null, inclusionMolecularEvent: String? = null
    ): Evaluation {
        val base = Evaluation(result = result, recoverable = false, inclusionMolecularEvents = setOfNotNull(inclusionMolecularEvent))
        return when (result) {
            EvaluationResult.PASS -> {
                base.copy(passSpecificMessages = setOf(specificMessage), passGeneralMessages = setOfNotNull(generalMessage))
            }

            EvaluationResult.NOT_EVALUATED -> {
                base.copy(passSpecificMessages = setOf(specificMessage), passGeneralMessages = setOfNotNull(generalMessage))
            }

            EvaluationResult.WARN -> {
                base.copy(warnSpecificMessages = setOf(specificMessage), warnGeneralMessages = setOfNotNull(generalMessage))
            }

            EvaluationResult.UNDETERMINED -> {
                base.copy(
                    undeterminedSpecificMessages = setOf(specificMessage), undeterminedGeneralMessages = setOfNotNull(generalMessage)
                )
            }

            EvaluationResult.FAIL -> {
                base.copy(failSpecificMessages = setOf(specificMessage), failGeneralMessages = setOfNotNull(generalMessage))
            }

            else -> {
                base
            }
        }
    }
}
