package com.hartwig.actin.algo.soc

import com.hartwig.actin.algo.datamodel.AnnotatedTreatmentMatch
import com.hartwig.actin.algo.datamodel.EvaluatedTreatment
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.TreatmentCandidate
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.doid.datamodel.TestDoidEntryFactory
import com.hartwig.actin.efficacy.TestExtendedEvidenceEntryFactory
import com.hartwig.actin.trial.datamodel.EligibilityFunction
import com.hartwig.actin.trial.datamodel.EligibilityRule
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EvaluatedTreatmentAnnotatorTest {

    private val efficacyEntries = TestExtendedEvidenceEntryFactory.createProperTestExtendedEvidenceEntries()
    private val actionableEvents: ActionableEvents = ImmutableActionableEvents.builder().build()
    private val doidEntry = TestDoidEntryFactory.createMinimalTestDoidEntry()
    private val resistanceEvidenceMatcher = ResistanceEvidenceMatcher(doidEntry, emptySet(), actionableEvents)
    private val annotator = EvaluatedTreatmentAnnotator.create(efficacyEntries, resistanceEvidenceMatcher)
    private val evaluations = listOf(Evaluation(result = EvaluationResult.PASS, recoverable = true))

    @Test
    fun `Should annotate SOC treatments with efficacy evidence`() {
        val eligibilityFunction = EligibilityFunction(EligibilityRule.MSI_SIGNATURE, emptyList())
        val treatmentCandidate = TreatmentCandidate(
            TreatmentTestFactory.drugTreatment("pembrolizumab", TreatmentCategory.IMMUNOTHERAPY), false, setOf(eligibilityFunction)
        )
        val socTreatments = listOf(EvaluatedTreatment(treatmentCandidate, evaluations))

        val actualAnnotatedTreatmentMatches = annotator.annotate(socTreatments)
        val expectedAnnotatedTreatmentMatches =
            listOf(AnnotatedTreatmentMatch(treatmentCandidate, evaluations, efficacyEntries, null, emptyList()))

        assertThat(actualAnnotatedTreatmentMatches).isEqualTo(expectedAnnotatedTreatmentMatches)
    }

    @Test
    fun `Should return empty annotations list for SOC treatment without efficacy evidence`() {
        val eligibilityFunction = EligibilityFunction(EligibilityRule.MSI_SIGNATURE, emptyList())
        val treatmentCandidate = TreatmentCandidate(
            TreatmentTestFactory.drugTreatment("capecitabine+oxaliplatin", TreatmentCategory.CHEMOTHERAPY),
            false,
            setOf(eligibilityFunction)
        )
        val socTreatments = listOf(EvaluatedTreatment(treatmentCandidate, evaluations))

        val actualAnnotatedTreatmentMatches = annotator.annotate(socTreatments)
        val expectedAnnotatedTreatmentMatches =
            listOf(AnnotatedTreatmentMatch(treatmentCandidate, evaluations, emptyList(), null, emptyList()))

        assertThat(actualAnnotatedTreatmentMatches).isEqualTo(expectedAnnotatedTreatmentMatches)
    }
}