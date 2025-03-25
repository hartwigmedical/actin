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
    private val MIN_CYCLES = 5
    private val RADIOTHERAPY = Radiotherapy("Radiotherapy", radioType = RadiotherapyType.CYBERKNIFE)
    private val REFERENCE_YEAR = 2024

    @Test
    fun `Should fail if there are no treatments`() {
        val record = TreatmentTestFactory.withTreatmentHistory(emptyList())
        assertResultForPatient(EvaluationResult.FAIL, DrugType.ALK_INHIBITOR, record)
    }

    @Test
    fun `Should pass if there is a chemotherapy of matching type with sufficient cycles`() {
        val matchingTreatment = TreatmentHistoryEntry(
            treatments = setOf(
                TreatmentTestFactory.drugTreatment("Alk Inhibitor", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.ALK_INHIBITOR)),
                RADIOTHERAPY
            ),
            treatmentHistoryDetails = TreatmentHistoryDetails(stopYear = 2030, cycles = MIN_CYCLES)
        )
        val record = TreatmentTestFactory.withTreatmentHistory(listOf(matchingTreatment))
        assertResultForPatient(EvaluationResult.PASS, DrugType.ALK_INHIBITOR, record)
    }

    @Test
    fun `Should fail if there is only radiotherapy`() {
        val matchingTreatment = TreatmentHistoryEntry(
            treatments = setOf(
                RADIOTHERAPY
            ),
            treatmentHistoryDetails = TreatmentHistoryDetails(stopYear = 2030, cycles = MIN_CYCLES)
        )
        val record = TreatmentTestFactory.withTreatmentHistory(listOf(matchingTreatment))
        assertResultForPatient(EvaluationResult.FAIL, RadiotherapyType.CYBERKNIFE, record)
    }

    @Test
    fun `Should be undetermined if there are no explicit fails but the types is undetermined`() {
        val matchingTreatment = TreatmentHistoryEntry(
            treatments = setOf(
                TreatmentTestFactory.drugTreatment("Alk Inhibitor", TreatmentCategory.CHEMOTHERAPY, emptySet()),
                Radiotherapy("Radiotherapy", radioType = null)
            ),
            treatmentHistoryDetails = TreatmentHistoryDetails(stopYear = 2030, cycles = MIN_CYCLES)
        )
        val record = TreatmentTestFactory.withTreatmentHistory(listOf(matchingTreatment))
        assertResultForPatient(EvaluationResult.UNDETERMINED, DrugType.ALK_INHIBITOR, record)
    }

    @Test
    fun `Should fail for matching treatment with insufficient cycles`() {
        val matchingTreatment = TreatmentHistoryEntry(
            treatments = setOf(
                TreatmentTestFactory.drugTreatment("Alk Inhibitor", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.ALK_INHIBITOR)),
                RADIOTHERAPY
            ),
            treatmentHistoryDetails = TreatmentHistoryDetails(stopYear = 2030, cycles = MIN_CYCLES - 1)
        )
        val record = TreatmentTestFactory.withTreatmentHistory(listOf(matchingTreatment))
        assertResultForPatient(EvaluationResult.FAIL, DrugType.ALK_INHIBITOR, record)
    }

    @Test
    fun `Should fail if the end date is null but there is a newer treatment`() {
        val matchingTreatment = TreatmentHistoryEntry(
            treatments = setOf(
                TreatmentTestFactory.drugTreatment("Alk Inhibitor", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.ALK_INHIBITOR)),
                RADIOTHERAPY
            ),
            treatmentHistoryDetails = TreatmentHistoryDetails(cycles = 10),
            startYear = REFERENCE_YEAR - 1
        )
        val newerTreatment = TreatmentHistoryEntry(
            treatments = setOf(TreatmentTestFactory.drugTreatment("Ablation", TreatmentCategory.ABLATION)),
            treatmentHistoryDetails = TreatmentHistoryDetails(cycles = 10),
            startYear = REFERENCE_YEAR
        )
        val record = TreatmentTestFactory.withTreatmentHistory(listOf(matchingTreatment, newerTreatment))
        assertResultForPatient(EvaluationResult.FAIL, DrugType.ALK_INHIBITOR, record)
    }

    @Test
    fun `Should pass if the end date is null but there is another treatment with an unknown start date`() {
        val matchingTreatment = TreatmentHistoryEntry(
            treatments = setOf(
                TreatmentTestFactory.drugTreatment("Alk Inhibitor", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.ALK_INHIBITOR)),
                RADIOTHERAPY
            ),
            treatmentHistoryDetails = TreatmentHistoryDetails(cycles = MIN_CYCLES),
            startYear = REFERENCE_YEAR - 1
        )
        val treatmentUnknownStartDate = TreatmentHistoryEntry(
            treatments = setOf(TreatmentTestFactory.drugTreatment("Ablation", TreatmentCategory.ABLATION)),
            treatmentHistoryDetails = TreatmentHistoryDetails(cycles = MIN_CYCLES)
        )
        val record = TreatmentTestFactory.withTreatmentHistory(listOf(matchingTreatment, treatmentUnknownStartDate))
        assertResultForPatient(EvaluationResult.PASS, DrugType.ALK_INHIBITOR, record)
    }

    @Test
    fun `Should be undetermined if there is a radiotherapy of unknown type with sufficient cycles`() {
        val matchingTreatment = TreatmentHistoryEntry(
            treatments = setOf(
                TreatmentTestFactory.drugTreatment("Alk Inhibitor", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.ALK_INHIBITOR)),
                Radiotherapy("Radiotherapy", radioType = null)
            ),
            treatmentHistoryDetails = TreatmentHistoryDetails(stopYear = 2030, cycles = 10)
        )
        val record = TreatmentTestFactory.withTreatmentHistory(listOf(matchingTreatment))
        assertResultForPatient(EvaluationResult.UNDETERMINED, DrugType.ALK_INHIBITOR, record)
    }

    @Test
    fun `Should be undetermined if there is a matching treatment with unknown cycles`() {
        val matchingTreatmentNullCycles = TreatmentHistoryEntry(
            treatments = setOf(
                TreatmentTestFactory.drugTreatment("Alk Inhibitor", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.ALK_INHIBITOR)),
                RADIOTHERAPY
            ),
            treatmentHistoryDetails = TreatmentHistoryDetails(stopYear = 2030)
        )
        val record = TreatmentTestFactory.withTreatmentHistory(listOf(matchingTreatmentNullCycles))
        assertResultForPatient(EvaluationResult.UNDETERMINED, DrugType.ALK_INHIBITOR, record)
    }

    @Test
    fun `Should fail for matching treatment with an end date before the reference date`() {
        val matchingTreatment = TreatmentHistoryEntry(
            treatments = setOf(
                TreatmentTestFactory.drugTreatment("Alk Inhibitor", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.ALK_INHIBITOR)),
                RADIOTHERAPY
            ),
            treatmentHistoryDetails = TreatmentHistoryDetails(stopYear = 2022, cycles = MIN_CYCLES)
        )
        val record = TreatmentTestFactory.withTreatmentHistory(listOf(matchingTreatment))
        assertResultForPatient(EvaluationResult.FAIL, DrugType.ALK_INHIBITOR, record)
    }

    @Test
    fun `Should fail if the category matches but the type is wrong`() {
        val matchingTreatment = TreatmentHistoryEntry(
            treatments = setOf(
                TreatmentTestFactory.drugTreatment("Alk Inhibitor", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.ALK_INHIBITOR)),
                RADIOTHERAPY
            ),
            treatmentHistoryDetails = TreatmentHistoryDetails(stopYear = 2030, cycles = MIN_CYCLES)
        )
        val record = TreatmentTestFactory.withTreatmentHistory(listOf(matchingTreatment))
        assertResultForPatient(EvaluationResult.FAIL, DrugType.ABL_INHIBITOR, record)
    }

    @Test
    fun `Should pass if there is a matching treatment with a null end date and a newer treatment that hasn't started as of the reference date`() {
        val matchingTreatmentNullEndDate = TreatmentHistoryEntry(
            treatments = setOf(
                TreatmentTestFactory.drugTreatment("Alk Inhibitor", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.ALK_INHIBITOR)),
                RADIOTHERAPY
            ),
            treatmentHistoryDetails = TreatmentHistoryDetails(cycles = MIN_CYCLES),
            startYear = REFERENCE_YEAR - 1
        )
        val treatmentStartDateAfterReferenceDate = TreatmentHistoryEntry(
            treatments = setOf(TreatmentTestFactory.drugTreatment("Ablation", TreatmentCategory.ABLATION)),
            treatmentHistoryDetails = TreatmentHistoryDetails(cycles = MIN_CYCLES),
            startYear = REFERENCE_YEAR + 1
        )
        val record = TreatmentTestFactory.withTreatmentHistory(listOf(matchingTreatmentNullEndDate, treatmentStartDateAfterReferenceDate))
        assertResultForPatient(EvaluationResult.PASS, DrugType.ALK_INHIBITOR, record)
    }

    private fun assertResultForPatient(evaluationResult: EvaluationResult, type: TreatmentType, record: PatientRecord) {
        val evaluation = CurrentlyGetsChemoradiotherapyWithSpecificChemotherapyTypeAndMinimumCycles(
            type, MIN_CYCLES, LocalDate.of(REFERENCE_YEAR, 1, 1)
        ).evaluate(record)
        return EvaluationAssert.assertEvaluation(evaluationResult, evaluation)
    }
}