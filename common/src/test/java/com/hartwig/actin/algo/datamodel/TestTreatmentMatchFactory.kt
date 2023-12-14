package com.hartwig.actin.algo.datamodel

import com.google.common.collect.Lists
import com.google.common.collect.Maps
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
        return ImmutableTreatmentMatch.builder()
            .patientId(TestDataFactory.TEST_PATIENT)
            .sampleId(TestDataFactory.TEST_SAMPLE)
            .referenceDate(LocalDate.of(2021, 8, 2))
            .referenceDateIsLive(true)
            .build()
    }

    fun createProperTreatmentMatch(): TreatmentMatch {
        return ImmutableTreatmentMatch.builder().from(createMinimalTreatmentMatch()).trialMatches(createTestTrialMatches()).build()
    }

    private fun createTestTrialMatches(): List<TrialMatch> {
        val matches: MutableList<TrialMatch> = Lists.newArrayList()
        matches.add(
            ImmutableTrialMatch.builder()
                .identification(
                    ImmutableTrialIdentification.builder()
                        .trialId("Test Trial 1")
                        .open(true)
                        .acronym("TEST-1")
                        .title("Example test trial 1")
                        .build()
                )
                .isPotentiallyEligible(true)
                .evaluations(createTestGeneralEvaluationsTrial1())
                .cohorts(createTestCohortsTrial1())
                .build()
        )
        matches.add(
            ImmutableTrialMatch.builder()
                .identification(
                    ImmutableTrialIdentification.builder()
                        .trialId("Test Trial 2")
                        .open(true)
                        .acronym("TEST-2")
                        .title("Example test trial 2")
                        .build()
                )
                .isPotentiallyEligible(true)
                .evaluations(createTestGeneralEvaluationsTrial2())
                .cohorts(createTestCohortsTrial2())
                .build()
        )
        return matches
    }

    private fun createTestGeneralEvaluationsTrial1(): Map<Eligibility, Evaluation> {
        val map: MutableMap<Eligibility, Evaluation> = Maps.newTreeMap(EligibilityComparator())
        map[ImmutableEligibility.builder()
            .function(ImmutableEligibilityFunction.builder().rule(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD).build())
            .addReferences(ImmutableCriterionReference.builder().id("I-01").text("Patient must be an adult").build())
            .build()] =
            unrecoverable(EvaluationResult.PASS, "Patient is at least 18 years old", "Patient is adult", null)
        map[ImmutableEligibility.builder()
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
            .build()] = unrecoverable(
            EvaluationResult.PASS,
            "Patient has no known brain metastases",
            "No known brain metastases",
            null
        )
        map[ImmutableEligibility.builder()
            .function(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS).build())
            .addReferences(
                ImmutableCriterionReference.builder()
                    .id("I-02")
                    .text("This rule has 2 conditions:\n 1. Patient has no active brain metastases.\n 2. Patient has exhausted SOC.")
                    .build()
            )
            .build()] = unrecoverable(
            EvaluationResult.UNDETERMINED,
            "Could not be determined if patient has exhausted SOC",
            "Undetermined SOC exhaustion",
            null
        )
        return map
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
        val cohorts: MutableList<CohortMatch> = Lists.newArrayList()
        cohorts.add(
            ImmutableCohortMatch.builder()
                .metadata(createTestMetadata("A", true, false, false))
                .isPotentiallyEligible(true)
                .evaluations(createTestCohortEvaluationsTrial1CohortA())
                .build()
        )
        cohorts.add(
            ImmutableCohortMatch.builder()
                .metadata(createTestMetadata("B", true, true, false))
                .isPotentiallyEligible(true)
                .build()
        )
        cohorts.add(
            ImmutableCohortMatch.builder()
                .metadata(createTestMetadata("C", false, false, false))
                .isPotentiallyEligible(false)
                .evaluations(createTestCohortEvaluationsTrial1CohortC())
                .build()
        )
        return cohorts
    }

    private fun createTestCohortEvaluationsTrial1CohortA(): Map<Eligibility, Evaluation> {
        val map: MutableMap<Eligibility, Evaluation> = Maps.newTreeMap(EligibilityComparator())
        map[ImmutableEligibility.builder()
            .function(
                ImmutableEligibilityFunction.builder()
                    .rule(EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X)
                    .addParameters("BRAF")
                    .build()
            )
            .addReferences(ImmutableCriterionReference.builder().id("I-01").text("BRAF Activation").build())
            .build()] = unrecoverable(EvaluationResult.PASS, "Patient has BRAF activation", "BRAF Activation", "BRAF V600E")
        return map
    }

    private fun createTestCohortEvaluationsTrial1CohortC(): Map<Eligibility, Evaluation> {
        val map: MutableMap<Eligibility, Evaluation> = Maps.newTreeMap(EligibilityComparator())
        map[ImmutableEligibility.builder()
            .function(
                ImmutableEligibilityFunction.builder()
                    .rule(EligibilityRule.NOT)
                    .addParameters(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_KNOWN_ACTIVE_CNS_METASTASES).build())
                    .build()
            )
            .addReferences(ImmutableCriterionReference.builder().id("E-01").text("Active CNS metastases").build())
            .build()] =
            unrecoverable(EvaluationResult.FAIL, "Patient has active CNS metastases", "Active CNS metastases", null)
        return map
    }

    private fun createTestGeneralEvaluationsTrial2(): Map<Eligibility, Evaluation> {
        val map: MutableMap<Eligibility, Evaluation> = Maps.newTreeMap(EligibilityComparator())
        map[ImmutableEligibility.builder()
            .function(ImmutableEligibilityFunction.builder().rule(EligibilityRule.HAS_MEASURABLE_DISEASE).build())
            .addReferences(ImmutableCriterionReference.builder().id("I-01").text("Patient should have measurable disease").build())
            .build()] = unrecoverable(EvaluationResult.PASS, "Patient has measurable disease")
        map[ImmutableEligibility.builder()
            .function(ImmutableEligibilityFunction.builder().rule(EligibilityRule.CAN_GIVE_ADEQUATE_INFORMED_CONSENT).build())
            .addReferences(
                ImmutableCriterionReference.builder()
                    .id("I-02")
                    .text("Patient should be able to give adequate informed consent")
                    .build()
            )
            .build()] = unrecoverable(EvaluationResult.NOT_EVALUATED, "It is assumed that patient can provide adequate informed consent")
        return map
    }

    private fun createTestCohortsTrial2(): List<CohortMatch> {
        val cohorts: MutableList<CohortMatch> = Lists.newArrayList()
        cohorts.add(
            ImmutableCohortMatch.builder()
                .metadata(createTestMetadata("A", true, false, false))
                .isPotentiallyEligible(true)
                .evaluations(createTestCohortEvaluationsTrial2CohortA())
                .build()
        )
        cohorts.add(
            ImmutableCohortMatch.builder()
                .metadata(createTestMetadata("B", true, true, false))
                .isPotentiallyEligible(false)
                .evaluations(createTestCohortEvaluationsTrial2CohortB())
                .build()
        )
        return cohorts
    }

    private fun createTestCohortEvaluationsTrial2CohortA(): Map<Eligibility, Evaluation> {
        val map: MutableMap<Eligibility, Evaluation> = Maps.newTreeMap(EligibilityComparator())
        map[ImmutableEligibility.builder()
            .function(
                ImmutableEligibilityFunction.builder()
                    .rule(EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X)
                    .addParameters("BRAF")
                    .build()
            )
            .addReferences(ImmutableCriterionReference.builder().id("I-01").text("BRAF Activation").build())
            .build()] = unrecoverable(EvaluationResult.PASS, "Patient has BRAF activation", "BRAF Activation", "BRAF V600E")
        return map
    }

    private fun createTestCohortEvaluationsTrial2CohortB(): Map<Eligibility, Evaluation> {
        val map: MutableMap<Eligibility, Evaluation> = Maps.newTreeMap(EligibilityComparator())
        map[ImmutableEligibility.builder()
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
            .build()] =
            unrecoverable(EvaluationResult.FAIL, "Patient has had Vemurafenib treatment", "Vemurafenib treatment", null)
        return map
    }

    private fun unrecoverable(
        result: EvaluationResult, specificMessage: String,
        generalMessage: String? = null, inclusionMolecularEvent: String? = null
    ): Evaluation {
        val builder = ImmutableEvaluation.builder().result(result).recoverable(false)
        when (result) {
            EvaluationResult.PASS, EvaluationResult.NOT_EVALUATED -> {
                if (generalMessage != null) {
                    builder.addPassGeneralMessages(generalMessage)
                }
                builder.addPassSpecificMessages(specificMessage)
            }

            EvaluationResult.WARN -> {
                if (generalMessage != null) {
                    builder.addWarnGeneralMessages(generalMessage)
                }
                builder.addWarnSpecificMessages(specificMessage)
            }

            EvaluationResult.FAIL -> {
                if (generalMessage != null) {
                    builder.addFailGeneralMessages(generalMessage)
                }
                builder.addFailSpecificMessages(specificMessage)
            }

            EvaluationResult.UNDETERMINED -> {
                if (generalMessage != null) {
                    builder.addUndeterminedGeneralMessages(generalMessage)
                }
                builder.addUndeterminedSpecificMessages(specificMessage)
            }
        }
        if (inclusionMolecularEvent != null) {
            builder.addInclusionMolecularEvents(inclusionMolecularEvent)
        }
        return builder.build()
    }
}
