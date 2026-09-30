package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.tumor.TumorTestFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabValue
import java.time.LocalDate
import org.junit.jupiter.api.Test

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
    private val asat = LabMeasurement.ASPARTATE_AMINOTRANSFERASE
    private val alat = LabMeasurement.ALANINE_AMINOTRANSFERASE
    private val asat1Uln = LabTestFactory.create(asat, value = 100.0, refDate, refLimitUp = 100.0)
    private val alat1Uln = LabTestFactory.create(alat, value = 100.0, refDate, refLimitUp = 100.0)
    private val alat3Uln = LabTestFactory.create(alat, value = 300.0, refDate, refLimitUp = 100.0)
    private val asat6Uln = LabTestFactory.create(asat, value = 600.0, refDate, refLimitUp = 100.0)
    private val alat6Uln = LabTestFactory.create(alat, value = 600.0, refDate, refLimitUp = 100.0)

    @Test
    fun `Should pass when both lab values are under requested fold of ULN`() {
        evaluateForAllLiverLesionStates(
            EvaluationResult.PASS,
            listOf(asat1Uln, alat1Uln),
            "ASAT 100.0 and ALAT 100.0 below max of 5.0*ULN (5.0*100.0) and 5.0*ULN (5.0*100.0)",
            "ASAT 100.0 and ALAT 100.0 below max of 2.0*ULN (2.0*100.0) and 2.0*ULN (2.0*100.0)"
        )
    }

    @Test
    fun `Should be undetermined when both lab measures are missing`() {
        evaluateForAllLiverLesionStates(
            EvaluationResult.UNDETERMINED, emptyList(), "ASAT and ALAT are not present or cannot be evaluated"
        )
    }

    @Test
    fun `Should be undetermined when one lab measure is missing and the other one is within margin`() {
        evaluateForAllLiverLesionStates(EvaluationResult.UNDETERMINED, listOf(alat1Uln), "No measurement found for ASAT")
    }

    @Test
    fun `Should fail when one lab measure is missing but the other one is outside margin`() {
        evaluateForAllLiverLesionStates(
            EvaluationResult.FAIL,
            listOf(asat6Uln),
            "ASAT 600.0 exceeds maximum of 5.0*ULN (5.0*100.0)",
            "ASAT 600.0 exceeds maximum of 2.0*ULN (2.0*100.0)"
        )
    }

    @Test
    fun `Should fail when both lab values are above requested fold of ULN for both with or without liver metastases`() {
        evaluateForAllLiverLesionStates(
            EvaluationResult.FAIL, listOf(asat6Uln, alat6Uln), "ASAT 600.0 and ALAT 600.0 exceed maximum allowed value"
        )
    }

    @Test
    fun `Should fail with message when ASAT is above requested fold of ULN for both with or without liver metastases`() {
        evaluateForAllLiverLesionStates(
            EvaluationResult.FAIL,
            listOf(asat6Uln, alat1Uln),
            "ASAT 600.0 exceeds maximum of 5.0*ULN (5.0*100.0)",
            "ASAT 600.0 exceeds maximum of 2.0*ULN (2.0*100.0)"
        )
    }

    @Test
    fun `Should fail with message when ALAT is above requested fold of ULN for both with or without liver metastases`() {
        evaluateForAllLiverLesionStates(
            EvaluationResult.FAIL,
            listOf(asat1Uln, alat6Uln),
            "ALAT 600.0 exceeds maximum of 5.0*ULN (5.0*100.0)",
            "ALAT 600.0 exceeds maximum of 2.0*ULN (2.0*100.0)"
        )
    }

    @Test
    fun `Should evaluate to undetermined if outside non-metastasis margin but inside metastasis-margin and liver lesion state unknown`() {
        val labValues = listOf(asat1Uln, alat3Uln)
        val evaluation = function.evaluate(recordWithUnknownLiverLesions.copy(labValues = labValues))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluation,
            "ALAT 300.0 exceeds maximum of 2.0*ULN (2.0*100.0) if no liver metastases present (liver lesion data missing)"
        )
    }

    @Test
    fun `Should evaluate to undetermined if ALAT is above requested fold of ULN but within margin of error`() {
        val labValues = listOf(LabTestFactory.create(alat, value = 210.0, refDate, refLimitUp = 100.0), asat1Uln)
        val evaluation = function.evaluate(recordWithUnknownLiverLesions.copy(labValues = labValues))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluation,
            "ALAT 210.0 exceeds maximum of 2.0*ULN (2.0*100.0) but within margin of error"
        )
    }

    @Test
    fun `Should evaluate to undetermined if ASAT is above requested fold of ULN but within margin of error`() {
        val labValues = listOf(LabTestFactory.create(asat, value = 210.0, refDate, refLimitUp = 100.0), alat1Uln)
        val evaluation = function.evaluate(recordWithUnknownLiverLesions.copy(labValues = labValues))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluation,
            "ASAT 210.0 exceeds maximum of 2.0*ULN (2.0*100.0) but within margin of error"
        )
    }

    @Test
    fun `Should evaluate to undetermined if comparison to ULN cannot be made due to missing reference limit`() {
        val labValues = listOf(
            LabTestFactory.create(asat, value = 100.0, refDate, refLimitUp = null),
            LabTestFactory.create(alat, value = 100.0, refDate, refLimitUp = 100.0)
        )
        evaluateForAllLiverLesionStates(EvaluationResult.UNDETERMINED, labValues, "ASAT undetermined")
    }

    private fun evaluateForAllLiverLesionStates(
        expected: EvaluationResult,
        labValues: List<LabValue>,
        expectedMessageWithLiverLesions: String,
        expectedMessageWithoutOrUnknownLiverLesions: String = expectedMessageWithLiverLesions
    ) {
        assertEvaluation(
            expected, function.evaluate(recordWithLiverLesions.copy(labValues = labValues)), expectedMessageWithLiverLesions
        )
        assertEvaluation(
            expected,
            function.evaluate(recordWithoutLiverLesions.copy(labValues = labValues)),
            expectedMessageWithoutOrUnknownLiverLesions
        )
        assertEvaluation(
            expected,
            function.evaluate(recordWithUnknownLiverLesions.copy(labValues = labValues)),
            expectedMessageWithoutOrUnknownLiverLesions
        )
    }
}