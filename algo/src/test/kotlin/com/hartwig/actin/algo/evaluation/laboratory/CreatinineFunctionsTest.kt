package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.clinical.datamodel.BodyWeight
import com.hartwig.actin.clinical.datamodel.Gender
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.Test
import java.time.LocalDate

class CreatinineFunctionsTest {

    @Test
    fun `Should calc MDRD`() {
        val creatinine: LabValue = LabTestFactory.create(LabMeasurement.CREATININE, 70.0)
        val maleValues = CreatinineFunctions.calcMDRD(1971, 2021, Gender.MALE, creatinine)
        assertThat(maleValues[0]).isEqualTo(103.54, Offset.offset(EPSILON))
        assertThat(maleValues[1]).isEqualTo(125.49, Offset.offset(EPSILON))
        val femaleValues = CreatinineFunctions.calcMDRD(1971, 2021, Gender.FEMALE, creatinine)
        assertThat(femaleValues[0]).isEqualTo(76.83, Offset.offset(EPSILON))
        assertThat(femaleValues[1]).isEqualTo(93.11, Offset.offset(EPSILON))
    }

    @Test
    fun `Should calc CDKEPI`() {
        val creatinine: LabValue = LabTestFactory.create(LabMeasurement.CREATININE, 70.0)
        val maleValues = CreatinineFunctions.calcCKDEPI(1971, 2021, Gender.MALE, creatinine)
        assertThat(maleValues[0]).isEqualTo(104.62, Offset.offset(EPSILON))
        assertThat(maleValues[1]).isEqualTo(121.25, Offset.offset(EPSILON))
        val femaleValues = CreatinineFunctions.calcCKDEPI(1971, 2021, Gender.FEMALE, creatinine)
        assertThat(femaleValues[0]).isEqualTo(87.07, Offset.offset(EPSILON))
        assertThat(femaleValues[1]).isEqualTo(100.91, Offset.offset(EPSILON))
    }

    @Test
    fun `Should evaluate eGFR evaluations`() {
        assertThat(CreatinineFunctions.interpretEGFREvaluations(setOf(EvaluationResult.FAIL))).isEqualTo(EvaluationResult.FAIL)
        assertThat(CreatinineFunctions.interpretEGFREvaluations(setOf(EvaluationResult.FAIL, EvaluationResult.PASS)))
            .isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(CreatinineFunctions.interpretEGFREvaluations(setOf(EvaluationResult.UNDETERMINED, EvaluationResult.PASS)))
            .isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(CreatinineFunctions.interpretEGFREvaluations(setOf(EvaluationResult.PASS))).isEqualTo(EvaluationResult.PASS)
    }

    @Test
    fun `Should calc Cockcroft-Gault`() {
        val creatinine: LabValue = LabTestFactory.create(LabMeasurement.CREATININE, 70.0)
        assertThat(CreatinineFunctions.calcCockcroftGault(1971, 2021, Gender.MALE, 60.0, creatinine)).isEqualTo(
            95.24,
            Offset.offset(EPSILON)
        )
        assertThat(CreatinineFunctions.calcCockcroftGault(1971, 2021, Gender.FEMALE, 60.0, creatinine)).isEqualTo(
            80.95,
            Offset.offset(EPSILON)
        )
        assertThat(CreatinineFunctions.calcCockcroftGault(1971, 2021, Gender.MALE, 70.0, creatinine)).isEqualTo(
            111.11,
            Offset.offset(EPSILON)
        )
        assertThat(CreatinineFunctions.calcCockcroftGault(1971, 2021, Gender.FEMALE, 70.0, creatinine)).isEqualTo(
            94.44,
            Offset.offset(EPSILON)
        )
        assertThat(CreatinineFunctions.calcCockcroftGault(1971, 2021, Gender.MALE, null, creatinine)).isEqualTo(
            103.17,
            Offset.offset(EPSILON)
        )
        assertThat(CreatinineFunctions.calcCockcroftGault(1971, 2021, Gender.FEMALE, null, creatinine)).isEqualTo(
            67.46,
            Offset.offset(EPSILON)
        )
    }

    @Test
    fun `Should determine weight`() {
        val weights = listOf(
            BodyWeight(date = LocalDate.of(2020, 1, 1), value = 50.0, unit = ""),
            BodyWeight(date = LocalDate.of(2021, 1, 1), value = 60.0, unit = "") 
        )
        assertThat(CreatinineFunctions.determineWeight(weights)!!).isEqualTo(60.0, Offset.offset(EPSILON))
        assertThat(CreatinineFunctions.determineWeight(emptyList())).isNull()
    }

    companion object {
        private const val EPSILON = 1.0E-2
    }
}