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

    @Test
    fun `Should annotate SOC treatments with efficacy evidence`() {
        val efficacyEntries = TestExtendedEvidenceEntryFactory.createProperTestExtendedEvidenceEntries()
        val annotator = EvaluatedTreatmentAnnotator.create(efficacyEntries)
        val eligibilityFunction = EligibilityFunction(EligibilityRule.MSI_SIGNATURE, emptyList())
        val treatmentCandidate = TreatmentCandidate(
            TreatmentTestFactory.drugTreatment("pembrolizumab", TreatmentCategory.CHEMOTHERAPY), false, setOf(eligibilityFunction)
        )
        val evaluations = listOf(Evaluation(result = EvaluationResult.PASS, recoverable = true))
        val socTreatments = listOf(EvaluatedTreatment(treatmentCandidate, evaluations))

        val actualAnnotatedTreatmentMatches = annotator.annotate(socTreatments)
        val expectedAnnotatedTreatmentMatches = listOf(AnnotatedTreatmentMatch(treatmentCandidate, evaluations, efficacyEntries))

        assertThat(actualAnnotatedTreatmentMatches).isEqualTo(expectedAnnotatedTreatmentMatches)
    }
}