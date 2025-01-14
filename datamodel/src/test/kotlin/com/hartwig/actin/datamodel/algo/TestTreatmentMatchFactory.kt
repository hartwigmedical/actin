package com.hartwig.actin.datamodel.algo

import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.efficacy.TestExtendedEvidenceEntryFactory
import com.hartwig.actin.datamodel.personalization.Measurement
import com.hartwig.actin.datamodel.personalization.MeasurementType
import com.hartwig.actin.datamodel.personalization.PersonalizedDataAnalysis
import com.hartwig.actin.datamodel.personalization.Population
import com.hartwig.actin.datamodel.personalization.TreatmentAnalysis
import com.hartwig.actin.datamodel.personalization.TreatmentGroup
import com.hartwig.actin.datamodel.trial.CohortMetadata
import com.hartwig.actin.datamodel.trial.CriterionReference
import com.hartwig.actin.datamodel.trial.Eligibility
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.datamodel.trial.TrialIdentification
import com.hartwig.actin.datamodel.trial.TrialPhase
import com.hartwig.actin.datamodel.trial.TrialSource
import java.time.LocalDate

object TestTreatmentMatchFactory {

    fun createMinimalTreatmentMatch(): TreatmentMatch {
        return TreatmentMatch(
            patientId = TestPatientFactory.TEST_PATIENT,
            sampleId = TestPatientFactory.TEST_SAMPLE,
            referenceDate = LocalDate.of(2021, 8, 2),
            referenceDateIsLive = true,
            trialMatches = emptyList()
        )
    }

    fun createProperTreatmentMatch(): TreatmentMatch {
        return createMinimalTreatmentMatch().copy(
            trialMatches = createTestTrialMatches(),
            standardOfCareMatches = createSocMatches(),
            personalizedDataAnalysis = createPersonalizedDataAnalysis()
        )
    }

    private fun createTestTrialMatches(): List<TrialMatch> {
        return listOf(
            TrialMatch(
                identification = TrialIdentification(
                    trialId = "Test Trial 1",
                    open = true,
                    acronym = "TEST-1",
                    title = "Example test trial 1",
                    nctId = "NCT00000010",
                    phase = TrialPhase.PHASE_1,
                    source = TrialSource.NKI,
                    locations = listOf("Antoni van Leeuwenhoek")
                ),
                isPotentiallyEligible = true,
                evaluations = createTestGeneralEvaluationsTrial1(),
                cohorts = createTestCohortsTrial1(),
                nonEvaluableCohorts = emptyList()
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
                cohorts = createTestCohortsTrial2(),
                nonEvaluableCohorts = createNonEvaluableTestCohortsTrial2()
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
                        passMessages = setOf("Has active CNS metastases")
                    )
                ),
                annotations = TestExtendedEvidenceEntryFactory.createProperTestExtendedEvidenceEntries(),
                generalPfs = Measurement(136.5, 98, 74, 281, 46.0),
                generalOs = Measurement(215.0, 90, 121, 470, 110.1),
                resistanceEvidence = listOf(
                    ResistanceEvidence(
                        event = "BRAF amp",
                        treatmentName = "Pembrolizumab",
                        resistanceLevel = "A",
                        isTested = null,
                        isFound = false,
                        evidenceUrls = setOf("website")
                    )
                )
            )
        )
    }

    private fun createTestGeneralEvaluationsTrial1(): Map<Eligibility, Evaluation> {
        return mapOf(
            Eligibility(
                function = EligibilityFunction(rule = EligibilityRule.IS_AT_LEAST_X_YEARS_OLD, parameters = emptyList()),
                references = setOf(
                    CriterionReference(
                        id = "I-01",
                        text = "Patient must be an adult"
                    )
                )
            ) to unrecoverable(EvaluationResult.PASS, "Patient is at least 18 years old", null),
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
            ) to unrecoverable(EvaluationResult.PASS, "No known brain metastases present"),
            Eligibility(
                function = EligibilityFunction(rule = EligibilityRule.HAS_EXHAUSTED_SOC_TREATMENTS, parameters = emptyList()),
                references = setOf(
                    CriterionReference(
                        id = "I-02",
                        text = "This rule has 2 conditions:\n 1. Patient has no active brain metastases.\n 2. Patient has exhausted SOC."
                    )
                )
            ) to unrecoverable(EvaluationResult.FAIL, "Has not exhausted SOC (remaining options capecitabine)")
        )
    }

    fun createTestMetadata(
        cohortId: String,
        evaluable: Boolean,
        open: Boolean,
        slotsAvailable: Boolean,
        ignore: Boolean
    ): CohortMetadata {
        return CohortMetadata(
            cohortId = cohortId,
            evaluable = evaluable,
            open = open,
            slotsAvailable = slotsAvailable,
            ignore = ignore,
            description = "Cohort $cohortId"
        )
    }

    private fun createTestCohortsTrial1(): List<CohortMatch> {
        return listOf(
            CohortMatch(
                metadata = createTestMetadata("A", true, true, false, false),
                isPotentiallyEligible = true,
                evaluations = createTestCohortEvaluationsTrial1CohortA()
            ),
            CohortMatch(
                metadata = createTestMetadata("B", true, true, true, false),
                isPotentiallyEligible = true,
                evaluations = emptyMap()
            ),
            CohortMatch(
                metadata = createTestMetadata("C", true, false, false, false),
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
            ) to unrecoverable(EvaluationResult.PASS, "MSI", "MSI")
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
            ) to unrecoverable(EvaluationResult.FAIL, "Has active CNS metastases", null)
        )
    }

    private fun createTestGeneralEvaluationsTrial2(): Map<Eligibility, Evaluation> {
        return mapOf(
            Eligibility(
                function = EligibilityFunction(rule = EligibilityRule.HAS_MEASURABLE_DISEASE),
                references = setOf(CriterionReference(id = "I-01", text = "Patient should have measurable disease")),
            ) to unrecoverable(EvaluationResult.PASS, "Has measurable disease"),
            Eligibility(
                function = EligibilityFunction(rule = EligibilityRule.CAN_GIVE_ADEQUATE_INFORMED_CONSENT),
                references = setOf(CriterionReference(id = "I-02", text = "Patient should be able to give adequate informed consent"))
            ) to unrecoverable(EvaluationResult.NOT_EVALUATED, "Assumed that patient can give adequate informed consent")
        )
    }

    private fun createTestCohortsTrial2(): List<CohortMatch> {
        return listOf(
            CohortMatch(
                metadata = createTestMetadata("A", true, true, false, false),
                isPotentiallyEligible = true,
                evaluations = createTestCohortEvaluationsTrial2CohortA(),
            )
        )
    }

    private fun createNonEvaluableTestCohortsTrial2(): List<CohortMetadata> {
        return listOf(createTestMetadata("B", false, true, true, false))
    }

    private fun createTestCohortEvaluationsTrial2CohortA(): Map<Eligibility, Evaluation> {
        return mapOf(
            Eligibility(
                function = EligibilityFunction(rule = EligibilityRule.MSI_SIGNATURE, parameters = emptyList()),
                references = setOf(CriterionReference(id = "I-01", text = "MSI")),
            ) to unrecoverable(EvaluationResult.PASS, "Tumor is MSI with biallelic drivers in MMR genes", "MSI")
        )
    }

    private fun unrecoverable(
        result: EvaluationResult,
        message: String? = null,
        inclusionMolecularEvent: String? = null
    ): Evaluation {
        val base = Evaluation(result = result, recoverable = false, inclusionMolecularEvents = setOfNotNull(inclusionMolecularEvent))
        return when (result) {
            EvaluationResult.PASS -> {
                base.copy(passMessages = setOfNotNull(message))
            }

            EvaluationResult.NOT_EVALUATED -> {
                base.copy(passMessages = setOfNotNull(message))
            }

            EvaluationResult.WARN -> {
                base.copy(warnMessages = setOfNotNull(message))
            }

            EvaluationResult.UNDETERMINED -> {
                base.copy(
                    undeterminedMessages = setOfNotNull(message)
                )
            }

            EvaluationResult.FAIL -> {
                base.copy(failMessages = setOfNotNull(message))
            }
        }
    }

    private fun createPersonalizedDataAnalysis(): PersonalizedDataAnalysis {
        val pembrolizumab = TreatmentTestFactory.drugTreatment("PEMBROLIZUMAB", TreatmentCategory.IMMUNOTHERAPY)
        val populationPfsAndDecision = listOf(
            Triple("All", 236.5, 0.3),
            Triple("Age 45-55", 356.5, 0.4),
            Triple("WHO 1", 321.0, 0.25)
        )
        val populationOsAndDecision = listOf(
            Triple("All", 236.5, 0.3),
            Triple("Age 45-55", 356.5, 0.4),
            Triple("WHO 1", 321.0, 0.25)
        )

        val pfsMap = populationPfsAndDecision.map { (name, pfs, _) ->
            name to Measurement(pfs, 100, (pfs / 2).toInt(), (pfs * 2).toInt(), pfs * 0.4)
        }.toMap()

        val osMap = populationOsAndDecision.map { (name, os, _) ->
            name to Measurement(os, 100, (os / 2).toInt(), (os * 2).toInt(), os * 0.4)
        }.toMap()

        val decisionMap = populationPfsAndDecision.map { (name, _, decision) -> name to Measurement(decision, 100) }.toMap()

        val treatmentAnalyses = listOf(
            TreatmentAnalysis(
                TreatmentGroup.fromTreatmentName(pembrolizumab.name)!!,
                mapOf(
                    MeasurementType.PROGRESSION_FREE_SURVIVAL to pfsMap,
                    MeasurementType.OVERALL_SURVIVAL to osMap,
                    MeasurementType.TREATMENT_DECISION to decisionMap
                )
            )
        )

        val populations = populationPfsAndDecision.map { (name, _, _) ->
            Population(name, MeasurementType.entries.associateWith { 1000 })
        }

        return PersonalizedDataAnalysis(treatmentAnalyses, populations)
    }
}
