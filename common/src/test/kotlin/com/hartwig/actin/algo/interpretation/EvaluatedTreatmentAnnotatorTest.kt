package com.hartwig.actin.algo.interpretation

import com.hartwig.actin.algo.datamodel.AnnotatedTreatmentMatch
import com.hartwig.actin.algo.datamodel.EvaluatedTreatment
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.TreatmentCandidate
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.efficacy.TestExtendedEvidenceEntryFactory
import com.hartwig.actin.trial.datamodel.EligibilityFunction
import com.hartwig.actin.trial.datamodel.EligibilityRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EvaluatedTreatmentAnnotatorTest {

    private val efficacyEntries = TestExtendedEvidenceEntryFactory.createProperTestExtendedEvidenceEntries()
    private val annotator = EvaluatedTreatmentAnnotator.create(efficacyEntries)
    private val evaluations = listOf(Evaluation(result = EvaluationResult.PASS, recoverable = true))

    @Test
    fun `Should annotate SOC treatments with efficacy evidence`() {
        val eligibilityFunction = EligibilityFunction(EligibilityRule.MSI_SIGNATURE, emptyList())
        val treatmentCandidate = TreatmentCandidate(
            TreatmentTestFactory.drugTreatment("pembrolizumab", TreatmentCategory.IMMUNOTHERAPY), false, setOf(eligibilityFunction)
        )
        val socTreatments = listOf(EvaluatedTreatment(treatmentCandidate, evaluations))

        val actualAnnotatedTreatmentMatches = annotator.annotate(socTreatments)
        val expectedAnnotatedTreatmentMatches = listOf(AnnotatedTreatmentMatch(treatmentCandidate, evaluations, efficacyEntries))

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
        val expectedAnnotatedTreatmentMatches = listOf(AnnotatedTreatmentMatch(treatmentCandidate, evaluations, emptyList()))

        assertThat(actualAnnotatedTreatmentMatches).isEqualTo(expectedAnnotatedTreatmentMatches)
    }
}