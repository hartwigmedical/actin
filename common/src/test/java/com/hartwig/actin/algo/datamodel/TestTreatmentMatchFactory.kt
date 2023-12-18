package com.hartwig.actin.algo.datamodel

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.treatment.datamodel.CohortMetadata
import com.hartwig.actin.treatment.datamodel.Eligibility
import com.hartwig.actin.treatment.datamodel.EligibilityRule
import com.hartwig.actin.treatment.datamodel.ImmutableCohortMetadata
import com.hartwig.actin.treatment.datamodel.ImmutableCriterionReference
import com.hartwig.actin.treatment.datamodel.ImmutableEligibility
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction
import com.hartwig.actin.treatment.datamodel.ImmutableTrialIdentification
import com.hartwig.actin.treatment.sort.EligibilityComparator
import java.time.LocalDate

object TestTreatmentMatchFactory {

    fun createMinimalTreatmentMatch(): TreatmentMatch {
        return TreatmentMatch(
            patientId = TestDataFactory.TEST_PATIENT,
            sampleId = TestDataFactory.TEST_SAMPLE,
            referenceDate = LocalDate.of(2021, 8, 2),
            referenceDateIsLive = true,
            trialMatches = emptyList()
        )
    }

    fun createProperTreatmentMatch(): TreatmentMatch {
        return createMinimalTreatmentMatch().copy(trialMatches = createTestTrialMatches())
    }

    private fun createTestTrialMatches(): List<TrialMatch> {
        return listOf(
            TrialMatch(
                identification = ImmutableTrialIdentification.builder()
                        .trialId("Test Trial 1")
                        .open(true)
                        .acronym("TEST-1")
                        .title("Example test trial 1")
                    .build(),
                isPotentiallyEligible = true,
                evaluations = createTestGeneralEvaluationsTrial1(),
                cohorts = createTestCohortsTrial1()
            ),
            TrialMatch(
                identification = ImmutableTrialIdentification.builder()
                        .trialId("Test Trial 2")
                        .open(true)
                        .acronym("TEST-2")
                        .title("Example test trial 2")
                    .build(),
                isPotentiallyEligible = true,
                evaluations = createTestGeneralEvaluationsTrial2(),
                cohorts = createTestCohortsTrial2()
            )
        )
    }

    private fun createTestGeneralEvaluationsTrial1(): Map<Eligibility, Evaluation> {
        return sortedMapOf<Eligibility, Evaluation>(
            EligibilityComparator(),
            ImmutableEligibility.builder()
                .function(ImmutableEligibilityFunction.builder().rule(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD).build())
                .addReferences(ImmutableCriterionReference.builder().id("I-01").text("Patient must be an adult").build())
                .build() to unrecoverable(EvaluationResult.PASS, "Patient is at least 18 years old", "Patient is adult", null),
            ImmutableEligibility.builder()
                .function(
                    ImmutableEligibilityFunction.builder()
                        .rule(EligibilityRule.NOT)
                        .addParameters(
                            ImmutableEligibilityFunction.builder()
                                .rule(EligibilityRule.HAS_KNOWN_ACTIVE_BRAIN_METASTASES)
                                .build()
                        )
                        .build()
                )
                .addReferences(
                    ImmutableCriterionReference.builder()
                        .id("I-02")
                        .text("This rule has 2 conditions:\n 1. Patient has no active brain metastases\n 2. Patient has exhausted SOC")
                        .build()
                )
                .build() to unrecoverable(EvaluationResult.PASS, "Patient has no known brain metastases", "No known brain metastases"),
            ImmutableEligibility.builder()
                .function(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS).build())
                .addReferences(
                    ImmutableCriterionReference.builder()
                        .id("I-02")
                        .text("This rule has 2 conditions:\n 1. Patient has no active brain metastases.\n 2. Patient has exhausted SOC.")
                        .build()
                )
                .build() to unrecoverable(
                EvaluationResult.UNDETERMINED, "Could not be determined if patient has exhausted SOC", "Undetermined SOC exhaustion"
            )
        )
    }

    private fun createTestMetadata(cohortId: String, open: Boolean, slotsAvailable: Boolean, blacklist: Boolean): CohortMetadata {
        return ImmutableCohortMetadata.builder()
            .cohortId(cohortId)
            .evaluable(true)
            .open(open)
            .slotsAvailable(slotsAvailable)
            .blacklist(blacklist)
            .description("Cohort $cohortId")
            .build()
    }

    private fun createTestCohortsTrial1(): List<CohortMatch> {
        return listOf(
            ImmutableCohortMatch.builder()
                .metadata(createTestMetadata("A", true, false, false))
                .isPotentiallyEligible(true)
                .evaluations(createTestCohortEvaluationsTrial1CohortA())
                .build(),
            ImmutableCohortMatch.builder()
                .metadata(createTestMetadata("B", true, true, false))
                .isPotentiallyEligible(true)
                .build(),
            ImmutableCohortMatch.builder()
                .metadata(createTestMetadata("C", false, false, false))
                .isPotentiallyEligible(false)
                .evaluations(createTestCohortEvaluationsTrial1CohortC())
                .build()
        )
    }

    private fun createTestCohortEvaluationsTrial1CohortA(): Map<Eligibility, Evaluation> {
        return mapOf(
            ImmutableEligibility.builder()
                .function(
                    ImmutableEligibilityFunction.builder()
                        .rule(EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X)
                        .addParameters("BRAF")
                        .build()
                )
                .addReferences(ImmutableCriterionReference.builder().id("I-01").text("BRAF Activation").build())
                .build() to unrecoverable(EvaluationResult.PASS, "Patient has BRAF activation", "BRAF Activation", "BRAF V600E")
        )
    }

    private fun createTestCohortEvaluationsTrial1CohortC(): Map<Eligibility, Evaluation> {
        return mapOf(
            ImmutableEligibility.builder()
                .function(
                    ImmutableEligibilityFunction.builder()
                        .rule(EligibilityRule.NOT)
                        .addParameters(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_KNOWN_ACTIVE_CNS_METASTASES).build())
                        .build()
                )
                .addReferences(ImmutableCriterionReference.builder().id("E-01").text("Active CNS metastases").build())
                .build() to unrecoverable(EvaluationResult.FAIL, "Patient has active CNS metastases", "Active CNS metastases", null)
        )
    }

    private fun createTestGeneralEvaluationsTrial2(): Map<Eligibility, Evaluation> {
        return sortedMapOf<Eligibility, Evaluation>(
            EligibilityComparator(),
            ImmutableEligibility.builder()
                .function(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_MEASURABLE_DISEASE).build())
                .addReferences(ImmutableCriterionReference.builder().id("I-01").text("Patient should have measurable disease").build())
                .build() to unrecoverable(EvaluationResult.PASS, "Patient has measurable disease"),
            ImmutableEligibility.builder()
                .function(ImmutableEligibilityFunction.builder().rule(EligibilityRule.CAN_GIVE_ADEQUATE_INFORMED_CONSENT).build())
                .addReferences(
                    ImmutableCriterionReference.builder()
                        .id("I-02")
                        .text("Patient should be able to give adequate informed consent")
                        .build()
                )
                .build() to unrecoverable(
                EvaluationResult.NOT_EVALUATED,
                "It is assumed that patient can provide adequate informed consent"
            )
        )
    }

    private fun createTestCohortsTrial2(): List<CohortMatch> {
        return listOf(
            ImmutableCohortMatch.builder()
                .metadata(createTestMetadata("A", true, false, false))
                .isPotentiallyEligible(true)
                .evaluations(createTestCohortEvaluationsTrial2CohortA())
                .build(),
            ImmutableCohortMatch.builder()
                .metadata(createTestMetadata("B", true, true, false))
                .isPotentiallyEligible(false)
                .evaluations(createTestCohortEvaluationsTrial2CohortB())
                .build()
        )
    }

    private fun createTestCohortEvaluationsTrial2CohortA(): Map<Eligibility, Evaluation> {
        return mapOf(
            ImmutableEligibility.builder()
                .function(
                    ImmutableEligibilityFunction.builder()
                        .rule(EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X)
                        .addParameters("BRAF")
                        .build()
                )
                .addReferences(ImmutableCriterionReference.builder().id("I-01").text("BRAF Activation").build())
                .build() to unrecoverable(EvaluationResult.PASS, "Patient has BRAF activation", "BRAF Activation", "BRAF V600E")
        )
    }

    private fun createTestCohortEvaluationsTrial2CohortB(): Map<Eligibility, Evaluation> {
        return mapOf(
            ImmutableEligibility.builder()
                .function(
                    ImmutableEligibilityFunction.builder()
                        .rule(EligibilityRule.NOT)
                        .addParameters(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_KNOWN_ACTIVE_CNS_METASTASES).build())
                        .build()
                )
                .addReferences(
                    ImmutableCriterionReference.builder()
                        .id("I-03")
                        .text("Patient should not have had Vemurafenib treatment")
                        .build()
                )
                .build() to unrecoverable(EvaluationResult.FAIL, "Patient has had Vemurafenib treatment", "Vemurafenib treatment", null)
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
