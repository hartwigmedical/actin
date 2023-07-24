package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.CypInteraction
import com.hartwig.actin.clinical.datamodel.ImmutableCypInteraction
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class CurrentlyGetsCYPXInhibitingOrInducingMedicationTest {
    @Test
    fun shouldPassWhenCYPInhibitingOrInducingMedication() {
        val function = CurrentlyGetsCYPXInhibitingOrInducingMedication(MedicationTestFactory.alwaysActive(), "9A9")
        val medications: MutableList<Medication> = mutableListOf()
        medications.add(TestMedicationFactory.builder().addCypInteractions(ImmutableCypInteraction.builder().cyp("9A9").type(CypInteraction.Type.INDUCER).strength(CypInteraction.Strength.STRONG).build()).build())
        //assertEvaluation(EvaluationResult.PASS, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
    }

    @Test
    fun shouldFailWhenNoCYPInhibitingOrInducingMedication() {
        val function = CurrentlyGetsCYPXInhibitingOrInducingMedication(MedicationTestFactory.alwaysActive(), "9A9")
        val medications: MutableList<Medication> = mutableListOf()
        medications.add(TestMedicationFactory.builder().addCypInteractions(ImmutableCypInteraction.builder().cyp("9A9").type(CypInteraction.Type.SUBSTRATE).strength(CypInteraction.Strength.STRONG).build()).build())
        //assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
    }
}