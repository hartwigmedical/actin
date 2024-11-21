package com.hartwig.actin.algo.soc

import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.datamodel.algo.AnnotatedTreatmentMatch
import com.hartwig.actin.datamodel.algo.EvaluatedTreatment
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.TreatmentCandidate
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.efficacy.TestExtendedEvidenceEntryFactory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EvaluatedTreatmentAnnotatorTest {

    private val efficacyEntries = TestExtendedEvidenceEntryFactory.createProperTestExtendedEvidenceEntries()
    private val actionableEvents: List<EfficacyEvidence> = emptyList()
    private val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
    private val treatmentDatabase = TestTreatmentDatabaseFactory.createProper()
    private val resistanceEvidenceMatcher = ResistanceEvidenceMatcher(
        doidModel,
        emptySet(),
        actionableEvents,
        treatmentDatabase,
        TestMolecularFactory.createMinimalTestMolecularHistory()
    )
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