package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test

class HasHadTreatmentCategoryOfOnlyTypesAndMinimumMonthsAsMostRecentTest {

    @Test
    fun `Should evaluate to undetermined`() {
        val function = HasHadTreatmentCategoryOfOnlyTypesAndMinimumMonthsAsMostRecent(
            TreatmentCategory.CHEMOTHERAPY,
            setOf(DrugType.ALKYLATING_AGENT),
            2
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }
}