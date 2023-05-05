package com.hartwig.actin.algo.evaluation.bloodtransfusion

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.BloodTransfusion
import com.hartwig.actin.clinical.datamodel.ImmutableBloodTransfusion
import com.hartwig.actin.clinical.datamodel.ImmutableMedication
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.apache.logging.log4j.util.Strings
import org.junit.Test
import java.time.LocalDate

class RequiresRegularHematopoieticSupportTest {
    @Test
    fun canEvaluateOnTransfusions() {
        val minDate = LocalDate.of(2020, 2, 1)
        val maxDate = minDate.plusMonths(2)
        val function = RequiresRegularHematopoieticSupport(minDate, maxDate)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(BloodTransfusionTestFactory.withBloodTransfusions(emptyList())))
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(BloodTransfusionTestFactory.withBloodTransfusion(create(minDate.minusWeeks(1))))
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(BloodTransfusionTestFactory.withBloodTransfusion(create(maxDate.plusWeeks(1))))
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(BloodTransfusionTestFactory.withBloodTransfusion(create(minDate.plusMonths(1))))
        )
    }

    @Test
    fun canEvaluateOnMedication() {
        val minDate = LocalDate.of(2020, 2, 1)
        val maxDate = minDate.plusMonths(2)
        val function = RequiresRegularHematopoieticSupport(minDate, maxDate)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(BloodTransfusionTestFactory.withMedications(emptyList())))
        val tooOld: Medication = support().startDate(minDate.minusWeeks(2)).stopDate(minDate.minusWeeks(1)).build()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(BloodTransfusionTestFactory.withMedication(tooOld)))
        val tooRecent: Medication = support().startDate(maxDate.plusWeeks(1)).stopDate(maxDate.plusWeeks(2)).build()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(BloodTransfusionTestFactory.withMedication(tooRecent)))
        val within: Medication = support().startDate(minDate.plusWeeks(1)).stopDate(maxDate.minusWeeks(1)).build()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(BloodTransfusionTestFactory.withMedication(within)))
        val stillRunning: Medication = support().startDate(minDate.minusWeeks(1)).stopDate(null).build()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(BloodTransfusionTestFactory.withMedication(stillRunning)))
        val wrongCategory: Medication = TestMedicationFactory.builder().from(stillRunning).categories(setOf("wrong")).build()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(BloodTransfusionTestFactory.withMedication(wrongCategory)))
    }

    companion object {
        private fun support(): ImmutableMedication.Builder {
            return TestMedicationFactory.builder()
                .addCategories(RequiresRegularHematopoieticSupport.HEMATOPOIETIC_MEDICATION_CATEGORIES.iterator().next())
        }

        private fun create(date: LocalDate): BloodTransfusion {
            return ImmutableBloodTransfusion.builder().product(Strings.EMPTY).date(date).build()
        }
    }
}