package com.hartwig.actin.algo.evaluation.bloodtransfusion

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.medication.AtcTestFactory
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
    fun shouldFailWhenNoBloodTransfusions() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(BloodTransfusionTestFactory.withBloodTransfusions(emptyList())))
    }

    @Test
    fun shouldFailWhenBloodTransfusionDateIsTooOld() {
        assertEvaluation(
            EvaluationResult.FAIL,
            FUNCTION.evaluate(BloodTransfusionTestFactory.withBloodTransfusion(create(MIN_DATE.minusWeeks(1))))
        )
    }

    @Test
    fun shouldFailWhenBloodTransfusionDateIsTooRecent() {
        assertEvaluation(
            EvaluationResult.FAIL,
            FUNCTION.evaluate(BloodTransfusionTestFactory.withBloodTransfusion(create(MAX_DATE.plusWeeks(1))))
        )
    }

    @Test
    fun shouldPassWhenBloodTransfusionHasCorrectDate() {
        assertEvaluation(
            EvaluationResult.PASS,
            FUNCTION.evaluate(BloodTransfusionTestFactory.withBloodTransfusion(create(MIN_DATE.plusMonths(1))))
        )
    }

    @Test
    fun shouldFailWhenNoMedication() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(BloodTransfusionTestFactory.withMedications(emptyList())))
    }

    @Test
    fun shouldFailWhenMedicationDateIsTooOld() {
        val tooOld: Medication = support().startDate(MIN_DATE.minusWeeks(2)).stopDate(MIN_DATE.minusWeeks(1)).build()
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(BloodTransfusionTestFactory.withMedication(tooOld)))
    }

    @Test
    fun shouldFailWhenMedicationDateIsTooRecent() {
        val tooRecent: Medication = support().startDate(MAX_DATE.plusWeeks(1)).stopDate(MAX_DATE.plusWeeks(2)).build()
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(BloodTransfusionTestFactory.withMedication(tooRecent)))
    }

    @Test
    fun shouldPassWhenMedicationHasCorrectDate() {
        val within: Medication = support().startDate(MIN_DATE.plusWeeks(1)).stopDate(MAX_DATE.minusWeeks(1)).build()
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(BloodTransfusionTestFactory.withMedication(within)))
    }

    @Test
    fun shouldPassWhenMedicationIsStillRunning() {
        val stillRunning: Medication = support().startDate(MIN_DATE.minusWeeks(1)).stopDate(null).build()
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(BloodTransfusionTestFactory.withMedication(stillRunning)))
    }

    @Test
    fun shouldFailWhenMedicationIsStillRunningButHasWrongCategory() {
        val stillRunning: Medication = support().startDate(MIN_DATE.minusWeeks(1)).stopDate(null).build()
        val atc = AtcTestFactory.atcClassificationBuilder().chemicalSubGroup(AtcTestFactory.atcLevelBuilder().name("wrong").build()).build()
        val wrongCategory: Medication = TestMedicationFactory.builder().from(stillRunning).atc(atc).build()
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(BloodTransfusionTestFactory.withMedication(wrongCategory)))
    }

    companion object {
        private val MIN_DATE: LocalDate = LocalDate.of(2020, 2, 1)
        private val MAX_DATE = MIN_DATE.plusMonths(2)
        private val FUNCTION = RequiresRegularHematopoieticSupport(MIN_DATE, MAX_DATE)

        private fun support(): ImmutableMedication.Builder {
            val atc = AtcTestFactory.atcClassificationBuilder().chemicalSubGroup(
                AtcTestFactory.atcLevelBuilder()
                    .name(RequiresRegularHematopoieticSupport.HEMATOPOIETIC_MEDICATION_CATEGORIES.iterator().next()).build()
            ).build()
            return TestMedicationFactory.builder().atc(atc)
        }

        private fun create(date: LocalDate): BloodTransfusion {
            return ImmutableBloodTransfusion.builder().product(Strings.EMPTY).date(date).build()
        }
    }
}