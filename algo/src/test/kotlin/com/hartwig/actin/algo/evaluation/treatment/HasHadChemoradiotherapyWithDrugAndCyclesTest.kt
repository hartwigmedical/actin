package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.jupiter.api.Test

class HasHadChemoradiotherapyWithDrugAndCyclesTest {

    @Test
    fun `Should evaluate to undetermined`() {
        val function = HasHadChemoradiotherapyWithDrugAndCycles(setOf(Drug("name", emptySet(), TreatmentCategory.CHEMOTHERAPY)), 2)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }
}