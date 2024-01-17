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
    fun `Should fail if treatment history empty`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            HasHadTreatmentWithCategoryOfTypesAsMostRecent(
                TreatmentCategory.TARGETED_THERAPY, DrugType.EGFR_INHIBITOR_GEN_3
            ).evaluate(
                withTreatmentHistory(emptyList())
            )
        )
    }

    @Test
    fun `Should fail if no anti cancer drugs in treatment history`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(
                TreatmentTestFactory.drugTreatment(
                    "Prednisone",
                    TreatmentCategory.SUPPORTIVE_TREATMENT,
                    setOf(DrugType.CORTICOSTEROID)
                )
            ), 2023, 5
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            HasHadTreatmentWithCategoryOfTypesAsMostRecent(
                TreatmentCategory.TARGETED_THERAPY, DrugType.EGFR_INHIBITOR_GEN_3
            ).evaluate(
                withTreatmentHistory(listOf(treatmentHistoryEntry))
            )
        )
    }

    @Test
    fun `Should evaluate specified category if type is null`() {
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
                TreatmentCategory.TARGETED_THERAPY, null
            ).evaluate(
                withTreatmentHistory(listOf(treatmentHistoryEntry))
            )
        )
    }

    @Test
    fun `Should pass if most recent drug is of right type`() {
        val treatmentHistoryEntry = listOf(
            treatmentHistoryEntry(
                setOf(
                    TreatmentTestFactory.drugTreatment(
                        "Osimertinib",
                        TreatmentCategory.TARGETED_THERAPY,
                        setOf(DrugType.EGFR_INHIBITOR_GEN_3)
                    )
                ), 2020, 5
            ),
            treatmentHistoryEntry(
                setOf(
                    TreatmentTestFactory.drugTreatment(
                        "Alectinib",
                        TreatmentCategory.TARGETED_THERAPY,
                        setOf(DrugType.ALK_INHIBITOR)
                    )
                ), 2019, 7
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            HasHadTreatmentWithCategoryOfTypesAsMostRecent(
                TreatmentCategory.TARGETED_THERAPY, DrugType.EGFR_INHIBITOR_GEN_3
            ).evaluate(
                withTreatmentHistory(treatmentHistoryEntry)
            )
        )
    }

    @Test
    fun `Should fail if right type but not most recent`() {
        val treatmentHistoryEntry = listOf(
            treatmentHistoryEntry(
                setOf(
                    TreatmentTestFactory.drugTreatment(
                        "Osimertinib",
                        TreatmentCategory.TARGETED_THERAPY,
                        setOf(DrugType.EGFR_INHIBITOR_GEN_3)
                    )
                ), 2020, 5
            ),
            treatmentHistoryEntry(
                setOf(
                    TreatmentTestFactory.drugTreatment(
                        "Alectinib",
                        TreatmentCategory.TARGETED_THERAPY,
                        setOf(DrugType.ALK_INHIBITOR)
                    )
                ), 2021, 5
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            HasHadTreatmentWithCategoryOfTypesAsMostRecent(
                TreatmentCategory.TARGETED_THERAPY, DrugType.EGFR_INHIBITOR_GEN_3
            ).evaluate(
                withTreatmentHistory(treatmentHistoryEntry)
            )
        )
    }

    @Test
    fun `Should fail if type null and specified category not most recent`() {
        val treatmentHistoryEntry = listOf(
            treatmentHistoryEntry(
                setOf(
                    TreatmentTestFactory.drugTreatment(
                        "Osimertinib",
                        TreatmentCategory.TARGETED_THERAPY,
                        setOf(DrugType.EGFR_INHIBITOR_GEN_3)
                    )
                ), 2020, 5
            ),
            treatmentHistoryEntry(
                setOf(
                    TreatmentTestFactory.drugTreatment(
                        "Carboplatin",
                        TreatmentCategory.CHEMOTHERAPY
                    )
                ), 2021, 5
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            HasHadTreatmentWithCategoryOfTypesAsMostRecent(
                TreatmentCategory.TARGETED_THERAPY, null
            ).evaluate(
                withTreatmentHistory(treatmentHistoryEntry)
            )
        )
    }

    @Test
    fun `Should fail if type not in treatment history`() {
        val treatmentHistoryEntry = listOf(
            treatmentHistoryEntry(
                setOf(
                    TreatmentTestFactory.drugTreatment(
                        "Alectinib",
                        TreatmentCategory.TARGETED_THERAPY,
                        setOf(DrugType.ALK_INHIBITOR)
                    )
                ), 2021, 5
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            HasHadTreatmentWithCategoryOfTypesAsMostRecent(
                TreatmentCategory.TARGETED_THERAPY, DrugType.EGFR_INHIBITOR_GEN_3
            ).evaluate(
                withTreatmentHistory(treatmentHistoryEntry)
            )
        )
    }

    @Test
    fun `Should evaluate to undetermined if start year missing in a prior treatment`() {
        val treatmentHistoryEntry = listOf(
            treatmentHistoryEntry(
                setOf(
                    TreatmentTestFactory.drugTreatment(
                        "Alectinib",
                        TreatmentCategory.TARGETED_THERAPY,
                        setOf(DrugType.ALK_INHIBITOR)
                    )
                ), 2021, 5
            ),
            treatmentHistoryEntry(
                setOf(
                    TreatmentTestFactory.drugTreatment(
                        "Osimertinib",
                        TreatmentCategory.TARGETED_THERAPY,
                        setOf(DrugType.EGFR_INHIBITOR_GEN_3)
                    )
                )
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HasHadTreatmentWithCategoryOfTypesAsMostRecent(
                TreatmentCategory.TARGETED_THERAPY, DrugType.EGFR_INHIBITOR_GEN_3
            ).evaluate(
                withTreatmentHistory(treatmentHistoryEntry)
            )
        )
    }
}