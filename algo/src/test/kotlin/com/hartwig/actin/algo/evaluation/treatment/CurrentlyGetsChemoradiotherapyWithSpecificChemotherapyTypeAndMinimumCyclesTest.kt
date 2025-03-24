package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.Radiotherapy
import com.hartwig.actin.datamodel.clinical.treatment.RadiotherapyType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate
import org.junit.Test

class CurrentlyGetsChemoradiotherapyWithSpecificChemotherapyTypeAndMinimumCyclesTest {
    @Test
    fun `Should fail if there are no treatments`() {
        val record = TreatmentTestFactory.withTreatmentHistory(emptyList())
        assertResultForPatient(EvaluationResult.FAIL, DrugType.ALK_INHIBITOR, 10, record)
    }

    @Test
    fun `Should pass if there is a chemotherapy of matching type with sufficient cycles`() {
        val matchingTreatment = TreatmentHistoryEntry(
            treatments = setOf(
                TreatmentTestFactory.drugTreatment("Alk Inhibitor", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.ALK_INHIBITOR)),
                Radiotherapy("Radiotherapy", radioType = RadiotherapyType.CYBERKNIFE)
            ),
            treatmentHistoryDetails = TreatmentHistoryDetails(stopYear = 2030, cycles = 10)
        )
        val record = TreatmentTestFactory.withTreatmentHistory(listOf(matchingTreatment))
        assertResultForPatient(EvaluationResult.PASS, DrugType.ALK_INHIBITOR, 1, record)
    }

    @Test
    fun `Should fail if there is only radiotherapy`() {
        val matchingTreatment = TreatmentHistoryEntry(
            treatments = setOf(
                Radiotherapy("Radiotherapy", radioType = RadiotherapyType.CYBERKNIFE)
            ),
            treatmentHistoryDetails = TreatmentHistoryDetails(stopYear = 2030, cycles = 10)
        )
        val record = TreatmentTestFactory.withTreatmentHistory(listOf(matchingTreatment))
        assertResultForPatient(EvaluationResult.FAIL, RadiotherapyType.CYBERKNIFE, 1, record)
    }

    @Test
    fun `Should be undetermined if there are no explicit fails`() {
        val matchingTreatment = TreatmentHistoryEntry(
            treatments = setOf(
                TreatmentTestFactory.drugTreatment("Alk Inhibitor", TreatmentCategory.CHEMOTHERAPY, emptySet()),
                Radiotherapy("Radiotherapy", radioType = null)
            ),
            treatmentHistoryDetails = TreatmentHistoryDetails(stopYear = 2030, cycles = 10)
        )
        val record = TreatmentTestFactory.withTreatmentHistory(listOf(matchingTreatment))
        assertResultForPatient(EvaluationResult.UNDETERMINED, DrugType.ALK_INHIBITOR, 1, record)
    }

    @Test
    fun `Should FAIL for matching treatment with insufficient cycles`() {
        val matchingTreatment = TreatmentHistoryEntry(
            treatments = setOf(
                TreatmentTestFactory.drugTreatment("Alk Inhibitor", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.ALK_INHIBITOR)),
                Radiotherapy("Radiotherapy", radioType = RadiotherapyType.CYBERKNIFE)
            ),
            treatmentHistoryDetails = TreatmentHistoryDetails(stopYear = 2030, cycles = 10)
        )
        val record = TreatmentTestFactory.withTreatmentHistory(listOf(matchingTreatment))
        assertResultForPatient(EvaluationResult.FAIL, DrugType.ALK_INHIBITOR, 20, record)
    }

    @Test
    fun `Should FAIL if the end date is null but there is a newer treatment`() {
        val matchingTreatment = TreatmentHistoryEntry(
            treatments = setOf(
                TreatmentTestFactory.drugTreatment("Alk Inhibitor", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.ALK_INHIBITOR)),
                Radiotherapy("Radiotherapy", radioType = RadiotherapyType.CYBERKNIFE)
            ),
            treatmentHistoryDetails = TreatmentHistoryDetails(cycles = 10),
            startYear = 2020
        )
        val newerTreatment = TreatmentHistoryEntry(
            treatments = setOf(TreatmentTestFactory.drugTreatment("Alk Inhibitor", TreatmentCategory.ABLATION)),
            treatmentHistoryDetails = TreatmentHistoryDetails(cycles = 10),
            startYear = 2022
        )
        val record = TreatmentTestFactory.withTreatmentHistory(listOf(matchingTreatment, newerTreatment))
        assertResultForPatient(EvaluationResult.FAIL, DrugType.ALK_INHIBITOR, 1, record)
    }

    @Test
    fun `Should PASS if the end date is null but there is another treatment with an unknown start date`() {
        val matchingTreatment = TreatmentHistoryEntry(
            treatments = setOf(
                TreatmentTestFactory.drugTreatment("Alk Inhibitor", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.ALK_INHIBITOR)),
                Radiotherapy("Radiotherapy", radioType = RadiotherapyType.CYBERKNIFE)
            ),
            treatmentHistoryDetails = TreatmentHistoryDetails(cycles = 10),
            startYear = 2020
        )
        val newerTreatment = TreatmentHistoryEntry(
            treatments = setOf(TreatmentTestFactory.drugTreatment("Alk Inhibitor", TreatmentCategory.ABLATION)),
            treatmentHistoryDetails = TreatmentHistoryDetails(cycles = 10),
            startYear = null
        )
        val record = TreatmentTestFactory.withTreatmentHistory(listOf(matchingTreatment, newerTreatment))
        assertResultForPatient(EvaluationResult.PASS, DrugType.ALK_INHIBITOR, 1, record)
    }

    @Test
    fun `Should be UNDETERMINED if there is a chemotherapy of unknown type with sufficient cycles`() {
        val matchingTreatment = TreatmentHistoryEntry(
            treatments = setOf(
                TreatmentTestFactory.drugTreatment("Alk Inhibitor", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.ALK_INHIBITOR)),
                Radiotherapy("Radiotherapy", radioType = null)
            ),
            treatmentHistoryDetails = TreatmentHistoryDetails(stopYear = 2030, cycles = 10)
        )
        val record = TreatmentTestFactory.withTreatmentHistory(listOf(matchingTreatment))
        assertResultForPatient(EvaluationResult.UNDETERMINED, DrugType.ALK_INHIBITOR, 1, record)
    }

    private fun assertResultForPatient(evaluationResult: EvaluationResult, type: TreatmentType, minCycles: Int, record: PatientRecord) {
        val evaluation = CurrentlyGetsChemoradiotherapyWithSpecificChemotherapyTypeAndMinimumCycles(
            type, minCycles, LocalDate.of(1900, 1, 1)
        ).evaluate(record)
        return EvaluationAssert.assertEvaluation(evaluationResult, evaluation)
    }
}