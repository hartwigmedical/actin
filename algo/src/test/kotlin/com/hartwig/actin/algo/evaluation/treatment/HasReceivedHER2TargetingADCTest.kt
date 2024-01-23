package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test


class HasReceivedHER2TargetingADCTest {

    @Test
    fun `Should pass when treatment history contains HER2 ADC`() {
        val treatmentHistoryEntry = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(
                    TreatmentTestFactory.drugTreatment(
                        "Trastuzumab-Deruxtecan",
                        TreatmentCategory.TARGETED_THERAPY,
                        setOf(DrugType.HER2_ANTIBODY, DrugType.ANTIBODY_DRUG_CONJUGATE_TARGETED_THERAPY)
                    )
                ), 2022, 5
            ),
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(
                    TreatmentTestFactory.drugTreatment(
                        "Doxorubicin",
                        TreatmentCategory.CHEMOTHERAPY,
                        setOf(DrugType.ANTHRACYCLINE)
                    )
                ), 2023, 7
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, HasReceivedHER2TargetingADC().evaluate(
                (TreatmentTestFactory.withTreatmentHistory(treatmentHistoryEntry))
            )
        )
        println(
            HasReceivedHER2TargetingADC().evaluate(
                (TreatmentTestFactory.withTreatmentHistory(treatmentHistoryEntry))
            )
        )
    }

    @Test
    fun `Should fail when treatment history is empty`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, HasReceivedHER2TargetingADC().evaluate(
                (TreatmentTestFactory.withTreatmentHistory(emptyList()))
            )
        )
    }

    @Test
    fun `Should fail when treatment history only contains drugs of other type`() {
        val treatmentHistoryEntry = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(
                    TreatmentTestFactory.drugTreatment(
                        "Doxorubicin",
                        TreatmentCategory.CHEMOTHERAPY,
                        setOf(DrugType.ANTHRACYCLINE)
                    )
                ), 2023, 7
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, HasReceivedHER2TargetingADC().evaluate(
                (TreatmentTestFactory.withTreatmentHistory(treatmentHistoryEntry))
            )
        )
    }
}