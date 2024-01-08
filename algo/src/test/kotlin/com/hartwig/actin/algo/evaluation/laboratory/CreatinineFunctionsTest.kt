package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionTestFactory
import com.hartwig.actin.clinical.datamodel.Gender
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import org.junit.Assert
import org.junit.Test
import java.time.LocalDateTime

class CreatinineFunctionsTest {
    @Test
    fun `Should correctly calculate MDRD`() {
        val creatinine: LabValue = LabTestFactory.forMeasurement(LabMeasurement.CREATININE).value(70.0).build()
        val maleValues = CreatinineFunctions.calcMDRD(1971, 2021, Gender.MALE, creatinine)
        Assert.assertEquals(103.54, maleValues[0], EPSILON)
        Assert.assertEquals(125.49, maleValues[1], EPSILON)
        val femaleValues = CreatinineFunctions.calcMDRD(1971, 2021, Gender.FEMALE, creatinine)
        Assert.assertEquals(76.83, femaleValues[0], EPSILON)
        Assert.assertEquals(93.11, femaleValues[1], EPSILON)
    }

    @Test
    fun `Should correctly calculate CDKEPI`() {
        val creatinine: LabValue = LabTestFactory.forMeasurement(LabMeasurement.CREATININE).value(70.0).build()
        val maleValues = CreatinineFunctions.calcCKDEPI(1971, 2021, Gender.MALE, creatinine)
        Assert.assertEquals(104.62, maleValues[0], EPSILON)
        Assert.assertEquals(121.25, maleValues[1], EPSILON)
        val femaleValues = CreatinineFunctions.calcCKDEPI(1971, 2021, Gender.FEMALE, creatinine)
        Assert.assertEquals(87.07, femaleValues[0], EPSILON)
        Assert.assertEquals(100.91, femaleValues[1], EPSILON)
    }

    @Test
    fun `Should correctly evaluate EGFR evaluations`() {
        Assert.assertEquals(EvaluationResult.FAIL, CreatinineFunctions.interpretEGFREvaluations(setOf(EvaluationResult.FAIL)))
        Assert.assertEquals(
            EvaluationResult.UNDETERMINED,
            CreatinineFunctions.interpretEGFREvaluations(setOf(EvaluationResult.FAIL, EvaluationResult.PASS))
        )
        Assert.assertEquals(
            EvaluationResult.UNDETERMINED,
            CreatinineFunctions.interpretEGFREvaluations(setOf(EvaluationResult.UNDETERMINED, EvaluationResult.PASS))
        )
        Assert.assertEquals(
            EvaluationResult.PASS,
            CreatinineFunctions.interpretEGFREvaluations(setOf(EvaluationResult.PASS))
        )
    }

    @Test
    fun `Should correctly calculate Cockcroft Gault`() {
        val creatinine: LabValue = LabTestFactory.forMeasurement(LabMeasurement.CREATININE).value(70.0).build()
        Assert.assertEquals(95.24, CreatinineFunctions.calcCockcroftGault(1971, 2021, Gender.MALE, 60.0, creatinine), EPSILON)
        Assert.assertEquals(80.95, CreatinineFunctions.calcCockcroftGault(1971, 2021, Gender.FEMALE, 60.0, creatinine), EPSILON)
        Assert.assertEquals(111.11, CreatinineFunctions.calcCockcroftGault(1971, 2021, Gender.MALE, 70.0, creatinine), EPSILON)
        Assert.assertEquals(94.44, CreatinineFunctions.calcCockcroftGault(1971, 2021, Gender.FEMALE, 70.0, creatinine), EPSILON)
        Assert.assertEquals(103.17, CreatinineFunctions.calcCockcroftGault(1971, 2021, Gender.MALE, null, creatinine), EPSILON)
        Assert.assertEquals(67.46, CreatinineFunctions.calcCockcroftGault(1971, 2021, Gender.FEMALE, null, creatinine), EPSILON)
    }

    @Test
    fun `Should correctly determine median weight`() {
        val weights = listOf(
            ImmutableBodyWeight.builder().date(LocalDateTime.of(2020, 1, 1, 12, 30, 0)).value(50.0).unit(EXPECTED_UNIT).valid(true).build(),
            ImmutableBodyWeight.builder().date(LocalDateTime.of(2021, 1, 1, 12, 30, 0)).value(60.0).unit(EXPECTED_UNIT).valid(true).build()
        )
        Assert.assertEquals(55.0, CreatinineFunctions.determineMedianWeight(VitalFunctionTestFactory.withBodyWeights(weights))!!, EPSILON)
        Assert.assertNull(CreatinineFunctions.determineMedianWeight(VitalFunctionTestFactory.withBodyWeights(emptyList())))
    }

    companion object {
        private const val EPSILON = 1.0E-2
        private const val EXPECTED_UNIT = "kilogram"
    }
}