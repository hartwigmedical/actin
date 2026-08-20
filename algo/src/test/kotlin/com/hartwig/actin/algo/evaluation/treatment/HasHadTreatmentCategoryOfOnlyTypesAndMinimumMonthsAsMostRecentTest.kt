package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.jupiter.api.Test

class HasHadTreatmentCategoryOfOnlyTypesAndMinimumMonthsAsMostRecentTest {

    @Test
    fun `Should evaluate to undetermined`() {
        val function = HasHadTreatmentCategoryOfOnlyTypesAndMinimumMonthsAsMostRecent(
            TreatmentCategory.CHEMOTHERAPY,
            setOf(DrugType.ALKYLATING_AGENT),
            2
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()),
            "Undetermined if received chemotherapy of only type(s)alkylating agent for at least 2 months as most recent line"
        )
    }
}