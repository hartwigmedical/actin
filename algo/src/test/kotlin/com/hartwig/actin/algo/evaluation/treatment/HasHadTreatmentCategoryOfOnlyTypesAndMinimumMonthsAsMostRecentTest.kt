package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.configuration.ReportIntendedUse
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
            2,
            EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY).treatment
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }
}