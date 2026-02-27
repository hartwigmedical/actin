package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test

class HasHadPDFollowingFirstLineTreatmentCategoryOfTypesTest {

    @Test
    fun `Should evaluate to undetermined`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HasHadPDFollowingFirstLineTreatmentCategoryOfTypes(TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.PLATINUM_COMPOUND)).evaluate(
                TestPatientFactory.createMinimalTestWGSPatientRecord()
            )
        )
    }
}