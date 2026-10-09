package com.hartwig.actin.algo.evaluation.bloodtransfusion

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.BloodTransfusion
import com.hartwig.actin.datamodel.clinical.TransfusionProduct
import java.time.LocalDate
import org.junit.jupiter.api.Test

private val THROMBOCYTE_PRODUCT = TransfusionProduct.THROMBOCYTE
private val THROMBOCYTE_DISPLAY = THROMBOCYTE_PRODUCT.display().lowercase()

class HasHadRecentBloodTransfusionTest {
    private val minDate = LocalDate.of(2020, 3, 30)
    private val function = HasHadRecentBloodTransfusion(THROMBOCYTE_PRODUCT, minDate)

    @Test
    fun `Should fail when no blood transfusions`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(BloodTransfusionTestFactory.withBloodTransfusions(emptyList())),
            "Has not received recent $THROMBOCYTE_DISPLAY blood transfusion"
        )
    }

    @Test
    fun `Should fail when thrombocyte transfusion but too long ago`() {
        val tooOld = create(TransfusionProduct.THROMBOCYTE, minDate.minusWeeks(4))
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(BloodTransfusionTestFactory.withBloodTransfusion(tooOld)),
            "Has not received recent $THROMBOCYTE_DISPLAY blood transfusion"
        )
    }

    @Test
    fun `Should fail when erythrocyte transfusion`() {
        val wrongProduct = create(TransfusionProduct.ERYTHROCYTE, minDate.plusWeeks(2))
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(BloodTransfusionTestFactory.withBloodTransfusion(wrongProduct)),
            "Has not received recent $THROMBOCYTE_DISPLAY blood transfusion"
        )
    }

    @Test
    fun `Should pass for thrombocyte transfusion with correct date`() {
        val correct = create(TransfusionProduct.THROMBOCYTE, minDate.plusWeeks(2))
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(BloodTransfusionTestFactory.withBloodTransfusion(correct)),
            "Has received recent $THROMBOCYTE_DISPLAY blood transfusion"
        )
    }

    companion object {
        private fun create(product: TransfusionProduct, date: LocalDate): BloodTransfusion {
            return BloodTransfusion(product = product, date = date)
        }
    }
}