package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.PriorSecondPrimary
import org.junit.Test
import java.time.LocalDate

class HasHistoryOfSecondMalignancyWithinYearsTest {

    @Test
    fun canEvaluate() {
        val minDate = LocalDate.of(2019, 6, 20)
        val function = HasHistoryOfSecondMalignancyWithinYears(minDate)

        // No history in case of no second primary
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(emptyList())))

        // One second primary but more than 3 years ago.
        val tooOld: PriorSecondPrimary = PriorTumorTestFactory.priorSecondPrimary(lastTreatmentYear = 2018)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimary(tooOld)))

        // One Second primary same year, no month known.
        val aroundCutoff: PriorSecondPrimary = PriorTumorTestFactory.priorSecondPrimary(lastTreatmentYear = 2019)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimary(aroundCutoff)))

        // One second primary but less than 3 years ago.
        val notTooLongAgo: PriorSecondPrimary = PriorTumorTestFactory.priorSecondPrimary(lastTreatmentYear = 2019, lastTreatmentMonth = 9)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimary(notTooLongAgo)))

        // One second primary but less than 4 years ago diagnosed
        val diagnosedNotTooLongAgo: PriorSecondPrimary = PriorTumorTestFactory.priorSecondPrimary(diagnosedYear = 2019)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimary(diagnosedNotTooLongAgo)))

        // One second primary no dates available
        val noDates: PriorSecondPrimary = PriorTumorTestFactory.priorSecondPrimary()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimary(noDates)))
    }
}