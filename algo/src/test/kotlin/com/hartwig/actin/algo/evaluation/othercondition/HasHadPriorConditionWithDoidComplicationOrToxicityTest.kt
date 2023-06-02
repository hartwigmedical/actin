package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableComplication
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition
import com.hartwig.actin.clinical.datamodel.ImmutableToxicity
import com.hartwig.actin.clinical.datamodel.ToxicitySource
import com.hartwig.actin.doid.TestDoidModelFactory
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class HasHadPriorConditionWithDoidComplicationOrToxicityTest {
    private var victim: EvaluationFunction? = null

    @Before
    fun setUp() {
        victim = HasHadPriorConditionWithDoidComplicationOrToxicity(
            TestDoidModelFactory.createWithOneDoidAndTerm(DOID, DOID_TERM),
            DOID,
            COMPLICATION_CATEGORY,
            TOXICITY_CATEGORY
        )
    }

    @Test
    fun shouldEvaluateFailWhenDoidNotPresentInModel() {
        victim = HasHadPriorConditionWithDoidComplicationOrToxicity(
            TestDoidModelFactory.createMinimalTestDoidModel(),
            DOID,
            COMPLICATION_NAME,
            TOXICITY_NAME
        )
        val evaluation = victim!!.evaluate(PATIENT_RECORD)
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        Assertions.assertThat(evaluation.failSpecificMessages())
            .containsOnly("Patient has no other condition belonging to category unknown doid")
        Assertions.assertThat(evaluation.failGeneralMessages()).containsOnly("No relevant non-oncological condition")
    }

    @Test
    fun shouldEvaluateFailWhenNoMatchingDoidComplicationOrToxicity() {
        val evaluation = victim!!.evaluate(PATIENT_RECORD)
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        Assertions.assertThat(evaluation.failSpecificMessages())
            .containsOnly("Patient has no other condition belonging to category some disease")
        Assertions.assertThat(evaluation.failGeneralMessages()).containsOnly("No relevant non-oncological condition")
    }

    @Test
    fun shouldEvaluatePassWhenDoidTermMatchesCategory() {
        assertPassEvaluationWithMessages(
            victim!!.evaluate(
                ImmutablePatientRecord.copyOf(PATIENT_RECORD).withClinical(
                    ImmutableClinicalRecord.copyOf(
                        PATIENT_RECORD.clinical()
                    )
                        .withPriorOtherConditions(priorOtherCondition())
                )
            ),
            "other condition",
            "Patient has condition(s) other condition, which is indicative of some disease"
        )
    }

    @Test
    fun shouldEvaluatePassWhenComplicationMatchesCategory() {
        assertPassEvaluationWithMessages(
            victim!!.evaluate(
                ImmutablePatientRecord.copyOf(PATIENT_RECORD).withClinical(
                    ImmutableClinicalRecord.copyOf(
                        PATIENT_RECORD.clinical()
                    )
                        .withComplications(complication())
                )

            ), "complication", "Patient has complication(s) complication, which is indicative of some disease"
        )
    }

    @Test
    fun shouldEvaluatePassWhenToxicityFromQuestionnaireMatchesCategory() {
        assertPassEvaluationWithMessages(
            victim!!.evaluate(
                ImmutablePatientRecord.copyOf(PATIENT_RECORD).withClinical(
                    ImmutableClinicalRecord.copyOf(
                        PATIENT_RECORD.clinical()
                    )
                        .withToxicities(toxicity(ToxicitySource.QUESTIONNAIRE, 1))
                )
            ),
            "toxicity",
            "Patient has toxicity(ies) toxicity, which is indicative of some disease"
        )
    }

    @Test
    fun shouldEvaluatePassWhenToxicityWithAtLeastGradeTwoMatchesCategory() {
        assertPassEvaluationWithMessages(
            victim!!.evaluate(
                ImmutablePatientRecord.copyOf(PATIENT_RECORD).withClinical(
                    ImmutableClinicalRecord.copyOf(
                        PATIENT_RECORD.clinical()
                    )
                        .withToxicities(toxicity(ToxicitySource.EHR, 2))
                )
            ),
            "toxicity",
            "Patient has toxicity(ies) toxicity, which is indicative of some disease"
        )
    }

    @Test
    fun shouldEvaluateFailWhenToxicityLessThanGradeTwoAndSourceNotQuestionnaire() {
        val evaluation = victim!!.evaluate(
            ImmutablePatientRecord.copyOf(PATIENT_RECORD).withClinical(
                ImmutableClinicalRecord.copyOf(
                    PATIENT_RECORD.clinical()
                )
                    .withToxicities(toxicity(ToxicitySource.EHR, 1))
            )
        )
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        Assertions.assertThat(evaluation.failSpecificMessages())
            .containsOnly("Patient has no other condition belonging to category some disease")
        Assertions.assertThat(evaluation.failGeneralMessages()).containsOnly("No relevant non-oncological condition")
    }

    @Test
    fun shouldIncludeMultipleMessages() {
        assertPassEvaluationWithMessages(
            victim!!.evaluate(
                ImmutablePatientRecord.copyOf(PATIENT_RECORD).withClinical(
                    ImmutableClinicalRecord.copyOf(
                        PATIENT_RECORD.clinical()
                    )
                        .withToxicities(toxicity(ToxicitySource.QUESTIONNAIRE, 2))
                        .withComplications(complication())
                        .withPriorOtherConditions(priorOtherCondition())
                )
            ),
            "complication and other condition and toxicity",
            "Patient has toxicity(ies) toxicity, which is indicative of some disease",
            "Patient has complication(s) complication, which is indicative of some disease",
            "Patient has condition(s) other condition, which is indicative of some disease"
        )
    }

    companion object {
        private const val OTHER_CONDITION_NAME: String = "other condition"
        private const val DOID: String = "1234"
        private val PATIENT_RECORD: PatientRecord = TestDataFactory.createMinimalTestPatientRecord()
        private const val TOXICITY_CATEGORY: String = "toxicity_category"
        private const val COMPLICATION_CATEGORY: String = "complication_category"
        private const val COMPLICATION_NAME: String = "complication"
        private const val TOXICITY_NAME: String = "toxicity"
        private const val DOID_TERM: String = "some disease"

        private fun complication(): ImmutableComplication {
            return ImmutableComplication.builder().addCategories(COMPLICATION_CATEGORY).name(COMPLICATION_NAME).build()
        }

        private fun toxicity(toxicitySource: ToxicitySource, grade: Int?): ImmutableToxicity {
            return ImmutableToxicity.builder()
                .addCategories(TOXICITY_CATEGORY)
                .name(TOXICITY_NAME)
                .evaluatedDate(LocalDate.now())
                .source(toxicitySource)
                .grade(grade)
                .build()
        }

        private fun priorOtherCondition(): ImmutablePriorOtherCondition {
            return ImmutablePriorOtherCondition.builder()
                .addDoids(DOID)
                .name(OTHER_CONDITION_NAME)
                .category(DOID_TERM)
                .isContraindicationForTherapy(true)
                .build()
        }

        private fun assertPassEvaluationWithMessages(evaluation: Evaluation, matchedNames: String, vararg passSpecificMessages: String) {
            assertEvaluation(EvaluationResult.PASS, evaluation)
            Assertions.assertThat(evaluation.passSpecificMessages()).containsOnly(*passSpecificMessages)
            Assertions.assertThat(evaluation.passGeneralMessages()).containsOnly("Patient has $matchedNames")
        }
    }
}