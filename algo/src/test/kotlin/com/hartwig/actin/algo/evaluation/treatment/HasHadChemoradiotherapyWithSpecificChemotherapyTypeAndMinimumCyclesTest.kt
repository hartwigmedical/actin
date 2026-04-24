package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.Radiotherapy
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.junit.jupiter.api.Test

class HasHadChemoradiotherapyWithSpecificChemotherapyTypeAndMinimumCyclesTest {

    private val minCycles = 5
    private val matchingType = DrugType.PLATINUM_COMPOUND
    private val radiotherapy = Radiotherapy("Radiotherapy")
    private val chemotherapy = TreatmentTestFactory.drugTreatment("Platinum", TreatmentCategory.CHEMOTHERAPY, setOf(matchingType))

    @Test
    fun `Should fail if there are no treatments`() {
        val record = TreatmentTestFactory.withTreatmentHistory(emptyList())
        assertResultForPatient(EvaluationResult.FAIL, matchingType, record)
    }

    @Test
    fun `Should fail if there is only radiotherapy`() {
        val matchingTreatment =
            TreatmentHistoryEntry(treatments = setOf(radiotherapy), treatmentHistoryDetails = TreatmentHistoryDetails(cycles = minCycles))
        val record = TreatmentTestFactory.withTreatmentHistory(listOf(matchingTreatment))
        assertResultForPatient(EvaluationResult.FAIL, matchingType, record)
    }

    @Test
    fun `Should warn for matching treatment with insufficient cycles`() {
        val matchingTreatment = TreatmentHistoryEntry(
            treatments = setOf(chemotherapy, radiotherapy),
            treatmentHistoryDetails = TreatmentHistoryDetails(cycles = minCycles - 1)
        )
        val record = TreatmentTestFactory.withTreatmentHistory(listOf(matchingTreatment))
        assertResultForPatient(EvaluationResult.WARN, matchingType, record)
    }

    @Test
    fun `Should fail if the chemo category matches but the type is wrong`() {
        val matchingTreatment = TreatmentHistoryEntry(
            treatments = setOf(
                TreatmentTestFactory.drugTreatment(
                    "Alkylating agent",
                    TreatmentCategory.CHEMOTHERAPY,
                    setOf(DrugType.ALKYLATING_AGENT)
                ), radiotherapy
            ), treatmentHistoryDetails = TreatmentHistoryDetails(cycles = minCycles)
        )
        val record = TreatmentTestFactory.withTreatmentHistory(listOf(matchingTreatment))
        assertResultForPatient(EvaluationResult.FAIL, matchingType, record)
    }

    @Test
    fun `Should be undetermined for chemoradiotherapy with matching cycles and unknown chemo type`() {
        val matchingTreatment = TreatmentHistoryEntry(
            treatments = setOf(
                TreatmentTestFactory.drugTreatment(
                    "Null type",
                    TreatmentCategory.CHEMOTHERAPY,
                    emptySet()
                ), radiotherapy
            ), treatmentHistoryDetails = TreatmentHistoryDetails(cycles = minCycles)
        )
        val record = TreatmentTestFactory.withTreatmentHistory(listOf(matchingTreatment))
        assertResultForPatient(EvaluationResult.UNDETERMINED, matchingType, record)
    }

    @Test
    fun `Should be undetermined if there is a matching treatment with unknown cycles`() {
        val matchingTreatmentNullCycles = TreatmentHistoryEntry(treatments = setOf(chemotherapy, radiotherapy))
        val record = TreatmentTestFactory.withTreatmentHistory(listOf(matchingTreatmentNullCycles))
        assertResultForPatient(EvaluationResult.UNDETERMINED, matchingType, record)
    }

    @Test
    fun `Should pass if there is a chemoradiotherapy of matching chemo type with sufficient cycles`() {
        val matchingTreatment = TreatmentHistoryEntry(
            treatments = setOf(chemotherapy, radiotherapy),
            treatmentHistoryDetails = TreatmentHistoryDetails(cycles = minCycles)
        )
        val record = TreatmentTestFactory.withTreatmentHistory(listOf(matchingTreatment))
        assertResultForPatient(EvaluationResult.PASS, matchingType, record)
    }

    private fun assertResultForPatient(evaluationResult: EvaluationResult, type: TreatmentType, record: PatientRecord) {
        val evaluation = HasHadChemoradiotherapyWithSpecificChemotherapyTypeAndMinimumCycles(type, minCycles).evaluate(record)
        return EvaluationAssert.assertEvaluation(evaluationResult, evaluation)
    }
}