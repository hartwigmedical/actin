package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.PriorPrimary
import org.junit.Test
import java.time.LocalDate

class HasHistoryOfSecondMalignancyWithinYearsTest {

    @Test
    fun canEvaluate() {
        val minDate = LocalDate.of(2019, 6, 20)
        val function = HasHistoryOfSecondMalignancyWithinYears(minDate)

        // No history in case of no second primary
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorPrimaries(emptyList())))

        // One second primary but more than 3 years ago.
        val tooOld: PriorPrimary = PriorTumorTestFactory.priorPrimary(lastTreatmentYear = 2018)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorPrimary(tooOld)))

        // One Second primary same year, no month known.
        val aroundCutoff: PriorPrimary = PriorTumorTestFactory.priorPrimary(lastTreatmentYear = 2019)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(PriorTumorTestFactory.withPriorPrimary(aroundCutoff)))

        // One second primary but less than 3 years ago.
        val notTooLongAgo: PriorPrimary = PriorTumorTestFactory.priorPrimary(lastTreatmentYear = 2019, lastTreatmentMonth = 9)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(PriorTumorTestFactory.withPriorPrimary(notTooLongAgo)))

        // One second primary but less than 4 years ago diagnosed
        val diagnosedNotTooLongAgo: PriorPrimary = PriorTumorTestFactory.priorPrimary(diagnosedYear = 2019)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(PriorTumorTestFactory.withPriorPrimary(diagnosedNotTooLongAgo)))

        // One second primary no dates available
        val noDates: PriorPrimary = PriorTumorTestFactory.priorPrimary()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(PriorTumorTestFactory.withPriorPrimary(noDates)))
    }
}