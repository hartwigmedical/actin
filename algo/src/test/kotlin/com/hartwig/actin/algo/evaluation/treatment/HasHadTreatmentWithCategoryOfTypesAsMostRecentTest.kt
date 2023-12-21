package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test

class HasHadTreatmentWithCategoryOfTypesAsMostRecentTest {

    @Test
    fun canEvaluate() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(
                TreatmentTestFactory.drugTreatment(
                    "Osimertinib",
                    TreatmentCategory.TARGETED_THERAPY,
                    setOf(DrugType.EGFR_INHIBITOR_GEN_3)
                )
            ), 2020, 5
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            HasHadTreatmentWithCategoryOfTypesAsMostRecent(
                TreatmentCategory.TARGETED_THERAPY,
                setOf(DrugType.EGFR_INHIBITOR_GEN_3)
            ).evaluate(
                withTreatmentHistory(listOf(treatmentHistoryEntry))
            )
        )
    }

}