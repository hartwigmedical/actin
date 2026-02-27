package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.tumor.TumorTestFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.BodyLocationCategory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.Radiotherapy
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test

class HasHadBrainRadiationTherapyTest {

    private val radiotherapy = setOf(Radiotherapy("Radiotherapy"))

    @Test
    fun `Should pass if radiotherapy with body location brain in oncological history`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(treatments = radiotherapy, bodyLocationCategory = null),
            TreatmentTestFactory.treatmentHistoryEntry(treatments = radiotherapy, bodyLocationCategory = setOf(BodyLocationCategory.BRAIN))
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            HasHadBrainRadiationTherapy().evaluate(withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should pass if radiotherapy with body location CNS (non spinal cord) in oncological history`() {
        val history = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = radiotherapy, bodyLocationCategory = setOf(BodyLocationCategory.CNS), bodyLocations = setOf("Cerebellum")
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            HasHadBrainRadiationTherapy().evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(history))
        )
    }

    @Test
    fun `Should fail if radiotherapy with body location CNS spinal cord in oncological history`() {
        val history = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = radiotherapy, bodyLocationCategory = setOf(BodyLocationCategory.CNS), bodyLocations = setOf("Spinal Cord")
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            HasHadBrainRadiationTherapy().evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(history))
        )
    }

    @Test
    fun `Should evaluate to undetermined if radiotherapy and brain metastases in history but radiotherapy location unknown`() {
        val history =
            TumorTestFactory.withCnsOrBrainLesionsAndOncologicalHistory(
                hasCnsLesions = true, hasBrainLesions = true,
                TreatmentTestFactory.treatmentHistoryEntry(treatments = radiotherapy, bodyLocationCategory = null)
            )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HasHadBrainRadiationTherapy().evaluate(history)
        )
    }

    @Test
    fun `Should evaluate to undetermined if radiotherapy and suspected brain metastases in history but radiotherapy location unknown`() {
        val history =
            TumorTestFactory.withSuspectedCnsOrBrainLesionsAndOncologicalHistory(
                hasSuspectedCnsLesions = null, hasSuspectedBrainLesions = true,
                TreatmentTestFactory.treatmentHistoryEntry(treatments = radiotherapy, bodyLocationCategory = null)
            )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HasHadBrainRadiationTherapy().evaluate(history)
        )
    }

    @Test
    fun `Should fail if no radiotherapy in history`() {
        val treatment = setOf(treatment("Chemotherapy", isSystemic = true, categories = setOf(TreatmentCategory.CHEMOTHERAPY)))
        val history =
            TumorTestFactory.withCnsOrBrainLesionsAndOncologicalHistory(
                hasCnsLesions = true, hasBrainLesions = true,
                TreatmentTestFactory.treatmentHistoryEntry(treatments = treatment, bodyLocationCategory = null)
            )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            HasHadBrainRadiationTherapy().evaluate(history)
        )
    }

    @Test
    fun `Should fail if radiotherapy in history but no brain or CNS metastases`() {
        val history =
            TumorTestFactory.withCnsOrBrainLesionsAndOncologicalHistory(
                hasCnsLesions = false, hasBrainLesions = false,
                TreatmentTestFactory.treatmentHistoryEntry(treatments = radiotherapy, bodyLocationCategory = null)
            )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            HasHadBrainRadiationTherapy().evaluate(history)
        )
    }

    @Test
    fun `Should fail if radiotherapy in history but with body location other than brain or CNS`() {
        val history =
            TumorTestFactory.withCnsOrBrainLesionsAndOncologicalHistory(
                hasCnsLesions = true, hasBrainLesions = true,
                TreatmentTestFactory.treatmentHistoryEntry(
                    treatments = radiotherapy,
                    bodyLocationCategory = setOf(BodyLocationCategory.BONE)
                )
            )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            HasHadBrainRadiationTherapy().evaluate(history)
        )
    }

    @Test
    fun `Should fail if oncological history is empty`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            HasHadBrainRadiationTherapy().evaluate(withTreatmentHistory(emptyList()))
        )
    }
}