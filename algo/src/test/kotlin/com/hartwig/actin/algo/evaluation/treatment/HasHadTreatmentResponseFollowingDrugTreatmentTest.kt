package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.Radiotherapy
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


private const val MATCHING_DRUG_NAME = "match"
private val TREATMENT_CATEGORY = TreatmentCategory.TARGETED_THERAPY

class HasHadTreatmentResponseFollowingDrugTreatmentTest {

    private val functionWithDrug =
        HasHadTreatmentResponseFollowingDrugTreatment(
            Drug(
                name = MATCHING_DRUG_NAME,
                category = TREATMENT_CATEGORY,
                drugTypes = emptySet()
            )
        )


    @Test
    fun `Should fail for empty treatment history`() {
        assertEvaluation(EvaluationResult.FAIL, functionWithDrug.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail if no matching drugs found`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(
                treatments = setOf(
                    DrugTreatment(
                        "treatment",
                        setOf(Drug(name = "other_drug", category = TREATMENT_CATEGORY, drugTypes = emptySet()))
                    )
                ),
                bestResponse = TreatmentResponse.COMPLETE_RESPONSE
            )
        )
        assertEvaluation(EvaluationResult.FAIL, functionWithDrug.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should fail if matching drugs found and response is progressive disease`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(
                treatments = setOf(
                    DrugTreatment(
                        "treatment",
                        setOf(Drug(name = MATCHING_DRUG_NAME, category = TREATMENT_CATEGORY, drugTypes = emptySet()))
                    )
                ),
                bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE
            )
        )
        assertEvaluation(EvaluationResult.FAIL, functionWithDrug.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should fail if matching drugs found and response is stable disease`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(
                treatments = setOf(
                    DrugTreatment(
                        "treatment",
                        setOf(Drug(name = MATCHING_DRUG_NAME, category = TREATMENT_CATEGORY, drugTypes = emptySet()))
                    )
                ),
                bestResponse = TreatmentResponse.STABLE_DISEASE
            )
        )
        assertEvaluation(EvaluationResult.FAIL, functionWithDrug.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Undetermined if matching drugs found and response is mixed`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(
                treatments = setOf(
                    DrugTreatment(
                        "treatment",
                        setOf(Drug(name = MATCHING_DRUG_NAME, category = TREATMENT_CATEGORY, drugTypes = emptySet()))
                    )
                ),
                bestResponse = TreatmentResponse.MIXED
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, functionWithDrug.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Undetermined if matching drugs found and no response available`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(
                treatments = setOf(
                    DrugTreatment(
                        "treatment",
                        setOf(Drug(name = MATCHING_DRUG_NAME, category = TREATMENT_CATEGORY, drugTypes = emptySet()))
                    )
                )
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, functionWithDrug.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Undetermined if treatments of matching drugs found and both positive an negative response available`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(
                treatments = setOf(
                    DrugTreatment(
                        "treatment",
                        setOf(Drug(name = MATCHING_DRUG_NAME, category = TREATMENT_CATEGORY, drugTypes = emptySet()))
                    )
                ),
                bestResponse = TreatmentResponse.COMPLETE_RESPONSE
            ),
            treatmentHistoryEntry(
                treatments = setOf(
                    DrugTreatment(
                        "treatment",
                        setOf(Drug(name = MATCHING_DRUG_NAME, category = TREATMENT_CATEGORY, drugTypes = emptySet()))
                    )
                ),
                bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, functionWithDrug.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Pass if matching drugs found and response is partial`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(
                treatments = setOf(
                    DrugTreatment(
                        "treatment",
                        setOf(Drug(name = MATCHING_DRUG_NAME, category = TREATMENT_CATEGORY, drugTypes = emptySet()))
                    )
                ),
                bestResponse = TreatmentResponse.PARTIAL_RESPONSE

            )
        )
        assertEvaluation(EvaluationResult.PASS, functionWithDrug.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Pass if matching drugs found and response is near complete`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(
                treatments = setOf(
                    DrugTreatment(
                        "treatment",
                        setOf(Drug(name = MATCHING_DRUG_NAME, category = TREATMENT_CATEGORY, drugTypes = emptySet()))
                    )
                ),
                bestResponse = TreatmentResponse.NEAR_COMPLETE_RESPONSE

            )
        )
        assertEvaluation(EvaluationResult.PASS, functionWithDrug.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Pass if matching drugs found and response is complete`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(
                treatments = setOf(
                    DrugTreatment(
                        "treatment",
                        setOf(Drug(name = MATCHING_DRUG_NAME, category = TREATMENT_CATEGORY, drugTypes = emptySet()))
                    )
                ),
                bestResponse = TreatmentResponse.COMPLETE_RESPONSE

            )
        )
        assertEvaluation(EvaluationResult.PASS, functionWithDrug.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Pass if matching drugs found and response is remission`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(
                treatments = setOf(
                    DrugTreatment(
                        "treatment",
                        setOf(Drug(name = MATCHING_DRUG_NAME, category = TREATMENT_CATEGORY, drugTypes = emptySet()))
                    )
                ),
                bestResponse = TreatmentResponse.REMISSION
            )
        )
        assertEvaluation(EvaluationResult.PASS, functionWithDrug.evaluate(withTreatmentHistory(treatmentHistory)))
    }


}