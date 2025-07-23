package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import org.junit.Test
import java.time.LocalDate

class IsPrimaryPlatinumRefractoryWithinMonthsTest {

    private val referenceDate = LocalDate.of(2025, 2, 5)
    private val recentDate = LocalDate.of(2025, 2, 5).minusMonths(1)
    private val nonRecentDate = LocalDate.of(2025, 2, 5).minusMonths(9)
    private val function = IsPrimaryPlatinumRefractoryWithinMonths(3, referenceDate)

    private val platinum = DrugTreatment(
        name = "Carboplatin",
        drugs = setOf(Drug(name = "Carboplatin", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = setOf(DrugType.PLATINUM_COMPOUND)))
    )

    @Test
    fun `Should fail if treatment history contains platinum but without progression`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(platinum),
                stopReason = StopReason.TOXICITY,
                stopYear = recentDate.year,
                stopMonth = recentDate.monthValue
            )
        )

        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }


    @Test
    fun `Should evaluate to undetermined if treatment history contains platinum but unknown if progression`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(platinum),
                stopYear = recentDate.year,
                stopMonth = recentDate.monthValue
            )
        )

        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should evaluate to undetermined if treatment history contains platinum with progression but long time ago`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(platinum),
                stopReason = StopReason.PROGRESSIVE_DISEASE,
                stopYear = nonRecentDate.year,
                stopMonth = nonRecentDate.monthValue
            )
        )

        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should evaluate to undetermined if treatment history does not contain platinum`() {
        val history = listOf(TreatmentTestFactory.treatmentHistoryEntry(treatments = emptySet()))

        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should pass if treatment history contains platinum with progression and within 3 months`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(platinum),
                stopReason = StopReason.PROGRESSIVE_DISEASE,
                stopYear = recentDate.year,
                stopMonth = recentDate.monthValue
            )
        )

        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }
}