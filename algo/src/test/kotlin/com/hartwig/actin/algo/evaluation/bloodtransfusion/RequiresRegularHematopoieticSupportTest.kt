package com.hartwig.actin.algo.evaluation.bloodtransfusion

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.bloodtransfusion.RequiresRegularHematopoieticSupport.Companion.hematopoieticMedicationCategories
import com.hartwig.actin.algo.evaluation.medication.AtcTestFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.AtcClassification
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.BloodTransfusion
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.TestMedicationFactory
import com.hartwig.actin.datamodel.clinical.TransfusionProduct
import java.time.LocalDate
import org.junit.jupiter.api.Test

class RequiresRegularHematopoieticSupportTest {
    @Test
    fun shouldFailWhenNoBloodTransfusions() {
        assertEvaluation(
            EvaluationResult.FAIL,
            FUNCTION.evaluate(BloodTransfusionTestFactory.withBloodTransfusions(emptyList())),
            "Has not received recent hematopoietic support"
        )
    }

    @Test
    fun shouldFailWhenBloodTransfusionDateIsTooOld() {
        assertEvaluation(
            EvaluationResult.FAIL,
            FUNCTION.evaluate(BloodTransfusionTestFactory.withBloodTransfusion(create(MIN_DATE.minusWeeks(1)))),
            "Has not received recent hematopoietic support"
        )
    }

    @Test
    fun shouldFailWhenBloodTransfusionDateIsTooRecent() {
        assertEvaluation(
            EvaluationResult.FAIL,
            FUNCTION.evaluate(BloodTransfusionTestFactory.withBloodTransfusion(create(MAX_DATE.plusWeeks(1)))),
            "Has not received recent hematopoietic support"
        )
    }

    @Test
    fun shouldPassWhenBloodTransfusionHasCorrectDate() {
        assertEvaluation(
            EvaluationResult.PASS,
            FUNCTION.evaluate(BloodTransfusionTestFactory.withBloodTransfusion(create(MIN_DATE.plusMonths(1)))),
            "Has received recent hematopoietic support (Erythrocyte)"
        )
    }

    @Test
    fun shouldFailWhenNoMedication() {
        assertEvaluation(
            EvaluationResult.FAIL,
            FUNCTION.evaluate(BloodTransfusionTestFactory.withMedications(emptyList())),
            "Has not received recent hematopoietic support"
        )
    }

    @Test
    fun shouldFailWhenMedicationDateIsTooOld() {
        val tooOld = support(MIN_DATE.minusWeeks(2), MIN_DATE.minusWeeks(1))
        assertEvaluation(
            EvaluationResult.FAIL,
            FUNCTION.evaluate(BloodTransfusionTestFactory.withMedication(tooOld)),
            "Has not received recent hematopoietic support"
        )
    }

    @Test
    fun shouldFailWhenMedicationDateIsTooRecent() {
        val tooRecent = support(MAX_DATE.plusWeeks(1), MAX_DATE.plusWeeks(2))
        assertEvaluation(
            EvaluationResult.FAIL,
            FUNCTION.evaluate(BloodTransfusionTestFactory.withMedication(tooRecent)),
            "Has not received recent hematopoietic support"
        )
    }

    @Test
    fun shouldPassWhenMedicationHasCorrectDate() {
        val within = support(MIN_DATE.plusWeeks(1), MAX_DATE.minusWeeks(1))
        assertEvaluation(
            EvaluationResult.PASS,
            FUNCTION.evaluate(BloodTransfusionTestFactory.withMedication(within)),
            "Has received recent hematopoietic support (medication)"
        )
    }

    @Test
    fun shouldPassWhenMedicationIsStillRunning() {
        val stillRunning = support(MIN_DATE.minusWeeks(1), null)
        assertEvaluation(
            EvaluationResult.PASS,
            FUNCTION.evaluate(BloodTransfusionTestFactory.withMedication(stillRunning)),
            "Has received recent hematopoietic support (medication)"
        )
    }

    @Test
    fun shouldFailWhenMedicationIsStillRunningButHasWrongCategory() {
        val atc = AtcTestFactory.atcClassification().copy(
            chemicalSubGroup = AtcLevel(name = "", code = "wrong category")
        )
        val wrongCategory = support(MIN_DATE.minusWeeks(1), null, atc)
        assertEvaluation(
            EvaluationResult.FAIL,
            FUNCTION.evaluate(BloodTransfusionTestFactory.withMedication(wrongCategory)),
            "Has not received recent hematopoietic support"
        )
    }

    companion object {
        private val MIN_DATE: LocalDate = LocalDate.of(2020, 2, 1)
        private val MAX_DATE = MIN_DATE.plusMonths(2)
        private val FUNCTION = RequiresRegularHematopoieticSupport(AtcTestFactory.createProperAtcTree(), MIN_DATE, MAX_DATE)

        private fun support(startDate: LocalDate, stopDate: LocalDate?, atc: AtcClassification? = null): Medication {
            return TestMedicationFactory.createMinimal().copy(
                name = "medication",
                startDate = startDate,
                stopDate = stopDate,
                atc = atc ?: AtcTestFactory.atcClassification().copy(
                    chemicalSubGroup = hematopoieticMedicationCategories(AtcTestFactory.createProperAtcTree()).first()
                )
            )
        }

        private fun create(date: LocalDate): BloodTransfusion {
            return BloodTransfusion(product = TransfusionProduct.ERYTHROCYTE, date = date)
        }
    }
}