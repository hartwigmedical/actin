package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.medication.AtcTestFactory
import com.hartwig.actin.algo.evaluation.medication.MedicationTestFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.ClinicalStatus
import com.hartwig.actin.datamodel.clinical.InfectionStatus
import org.junit.Test
import java.time.LocalDate

class HasActiveInfectionTest {

    private val referenceDate = LocalDate.of(2024, 6, 12)
    private val function = HasActiveInfection(
        AtcTestFactory.createProperAtcTree(),
        referenceDate
    )
    private val systemicAntimicrobialAtc = "J01"

    @Test
    fun `Should pass if patient has active infection`(){
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withInfectionStatus(true)))
    }

    @Test
    fun `Should fail if patient has known infection status and does not have an active infection`(){
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withInfectionStatus(false)))
    }

    @Test
    fun `Should warn if infection status unknown but patient uses antimicrobials`(){
        assertEvaluation(
            EvaluationResult.WARN, function.evaluate(withInfectionStatusAndAtc(null, systemicAntimicrobialAtc, referenceDate.minusDays(1)))
        )
    }

    @Test
    fun `Should warn if infection status set to false but patient uses antimicrobials`(){
        assertEvaluation(
            EvaluationResult.WARN, function.evaluate(withInfectionStatusAndAtc(false, systemicAntimicrobialAtc, referenceDate.minusDays(1)))
        )
    }

    @Test
    fun `Should evaluate to undetermined if infection status is unknown and patient does not use any antimicrobials`(){
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withInfectionStatus(null)))
    }

    companion object {
        private fun withInfectionStatus(hasActiveInfection: Boolean?): PatientRecord {
            val infectionStatus = hasActiveInfection?.let { InfectionStatus(hasActiveInfection = it, description = null) }
            return TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
                clinicalStatus = ClinicalStatus(infectionStatus = infectionStatus)
            )
        }

        private fun withInfectionStatusAndAtc(hasActiveInfection: Boolean?, atcCode: String, startDate: LocalDate): PatientRecord {
            return withInfectionStatus(hasActiveInfection).copy(
                medications = listOf(
                    MedicationTestFactory.medication(startDate = startDate, atc = AtcTestFactory.atcClassification(atcCode))
                )
            )
        }
    }
}