package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.tumor.TumorTestFactory
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabValue
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasLimitedAsatAndAlatDependingOnLiverMetastasesTest {

    private val limitWithoutLiverMetastases = 2.0
    private val limitWithLiverMetastases = 5.0
    private val refDate = LocalDate.of(2024, 7, 4)
    private val minValidDate = refDate.minusDays(90)
    private val minPassDate = refDate.minusDays(30)
    private val function = HasLimitedAsatAndAlatDependingOnLiverMetastases(
        limitWithoutLiverMetastases, limitWithLiverMetastases, minValidDate, minPassDate
    )
    private val recordWithLiverLesions = TumorTestFactory.withLiverLesions(true)
    private val recordWithoutLiverLesions = TumorTestFactory.withLiverLesions(false)
    private val recordWithUnknownLiverLesions = TumorTestFactory.withLiverLesions(null)
    private val ASAT = LabMeasurement.ASPARTATE_AMINOTRANSFERASE
    private val ALAT = LabMeasurement.ALANINE_AMINOTRANSFERASE
    private val ASAT_1_ULN = LabTestFactory.create(ASAT, value = 100.0, refDate, refLimitUp = 100.0)
    private val ALAT_1_ULN = LabTestFactory.create(ALAT, value = 100.0, refDate, refLimitUp = 100.0)
    private val ALAT_3_ULN = LabTestFactory.create(ALAT, value = 300.0, refDate, refLimitUp = 100.0)
    private val ASAT_6_ULN = LabTestFactory.create(ASAT, value = 600.0, refDate, refLimitUp = 100.0)
    private val ALAT_6_ULN = LabTestFactory.create(ALAT, value = 600.0, refDate, refLimitUp = 100.0)

    @Test
    fun `Should pass when both lab values are under requested fold of ULN`() {
        evaluateForAllLiverLesionStates(EvaluationResult.PASS, listOf(ASAT_1_ULN, ALAT_1_ULN))
    }

    @Test
    fun `Should be undetermined when both lab measures are missing`() {
        evaluateForAllLiverLesionStates(EvaluationResult.UNDETERMINED, emptyList())
    }

    @Test
    fun `Should be undetermined when one lab measure is missing and the other one is within margin`() {
        evaluateForAllLiverLesionStates(EvaluationResult.UNDETERMINED, listOf(ALAT_1_ULN))
    }

    @Test
    fun `Should fail when one lab measure is missing but the other one is outside margin`() {
        evaluateForAllLiverLesionStates(EvaluationResult.FAIL, listOf(ASAT_6_ULN))
    }

    @Test
    fun `Should fail when both lab values are above requested fold of ULN for both with or without liver metastases`() {
        evaluateForAllLiverLesionStates(EvaluationResult.FAIL, listOf(ASAT_6_ULN, ALAT_6_ULN))
    }

    @Test
    fun `Should fail with message when ASAT is above requested fold of ULN for both with or without liver metastases`() {
        val labValues = listOf(ASAT_6_ULN, ALAT_1_ULN)
        evaluateForAllLiverLesionStates(EvaluationResult.FAIL, labValues)
        assertThat(function.evaluate(recordWithLiverLesions.copy(labValues = labValues)).failMessages)
            .containsExactly("ASAT 600.0 exceeds maximum of 5.0*ULN (5.0*100.0)")
    }

    @Test
    fun `Should fail with message when ALAT is above requested fold of ULN for both with or without liver metastases`() {
        val labValues = listOf(ASAT_1_ULN, ALAT_6_ULN)
        evaluateForAllLiverLesionStates(EvaluationResult.FAIL, labValues)
        assertThat(function.evaluate(recordWithLiverLesions.copy(labValues = labValues)).failMessages)
            .containsExactly("ALAT 600.0 exceeds maximum of 5.0*ULN (5.0*100.0)")
    }

    @Test
    fun `Should evaluate to undetermined if outside non-metastasis margin but inside metastasis-margin and liver lesion state unknown`() {
        val labValues = listOf(ASAT_1_ULN, ALAT_3_ULN)
        val evaluation = function.evaluate(recordWithUnknownLiverLesions.copy(labValues = labValues))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessages)
            .containsExactly("ALAT 300.0 exceeds maximum of 2.0*ULN (2.0*100.0) if no liver metastases present (liver lesion data missing)")
    }

    @Test
    fun `Should evaluate to undetermined if ALAT is above requested fold of ULN but within margin of error`() {
        val labValues = listOf(LabTestFactory.create(ALAT, value = 210.0, refDate, refLimitUp = 100.0), ASAT_1_ULN)
        val evaluation = function.evaluate(recordWithUnknownLiverLesions.copy(labValues = labValues))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessages)
            .containsExactly("ALAT 210.0 exceeds maximum of 2.0*ULN (2.0*100.0) but within margin of error")
    }

    @Test
    fun `Should evaluate to undetermined if ASAT is above requested fold of ULN but within margin of error`() {
        val labValues = listOf(LabTestFactory.create(ASAT, value = 210.0, refDate, refLimitUp = 100.0), ALAT_1_ULN)
        val evaluation = function.evaluate(recordWithUnknownLiverLesions.copy(labValues = labValues))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessages)
            .containsExactly("ASAT 210.0 exceeds maximum of 2.0*ULN (2.0*100.0) but within margin of error")
    }

    @Test
    fun `Should evaluate to undetermined if comparison to ULN cannot be made due to missing reference limit`() {
        val labValues = listOf(
            LabTestFactory.create(ASAT, value = 100.0, refDate, refLimitUp = null),
            LabTestFactory.create(ALAT, value = 100.0, refDate, refLimitUp = 100.0)
        )
        evaluateForAllLiverLesionStates(EvaluationResult.UNDETERMINED, labValues)
    }

    private fun evaluateForAllLiverLesionStates(expected: EvaluationResult, labValues: List<LabValue>) {
        assertEvaluation(
            expected, function.evaluate(recordWithLiverLesions.copy(labValues = labValues))
        )
        assertEvaluation(
            expected, function.evaluate(recordWithoutLiverLesions.copy(labValues = labValues))
        )
        assertEvaluation(
            expected, function.evaluate(recordWithUnknownLiverLesions.copy(labValues = labValues))
        )
    }
}