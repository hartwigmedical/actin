package com.hartwig.actin.algo.evaluation.bloodtransfusion

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.BloodTransfusion
import org.junit.Test
import java.time.LocalDate

class HasHadRecentBloodTransfusionTest {
    @Test
    fun canEvaluate() {
        val minDate = LocalDate.of(2020, 3, 30)
        val function = HasHadRecentBloodTransfusion(TransfusionProduct.THROMBOCYTE, minDate)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(BloodTransfusionTestFactory.withBloodTransfusions(emptyList())))
        val tooOld = create(TransfusionProduct.THROMBOCYTE, minDate.minusWeeks(4))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(BloodTransfusionTestFactory.withBloodTransfusion(tooOld)))
        val wrongProduct = create(TransfusionProduct.ERYTHROCYTE, minDate.plusWeeks(2))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(BloodTransfusionTestFactory.withBloodTransfusion(wrongProduct)))
        val correct = create(TransfusionProduct.THROMBOCYTE, minDate.plusWeeks(2))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(BloodTransfusionTestFactory.withBloodTransfusion(correct)))
    }

    companion object {
        private fun create(product: TransfusionProduct, date: LocalDate): BloodTransfusion {
            return BloodTransfusion(product = product.display(), date = date)
        }
    }
}