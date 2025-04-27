package com.hartwig.actin.report.interpretation

import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.datamodel.clinical.IHCTest
import com.hartwig.actin.datamodel.clinical.TestClinicalFactory
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private val BASE_PATIENT_RECORD =
    PatientRecordFactory.fromInputs(TestClinicalFactory.createMinimalTestClinicalRecord(), MolecularHistory(emptyList()))
private val DEFAULT_DATE = LocalDate.of(2025, 2, 10)

class IHCTestInterpreterTest {

    private val interpreter = IHCTestInterpreter()

    @Test
    fun `Should interpret IHC test based on score text`() {
        val result = interpreter.interpret(
            BASE_PATIENT_RECORD.copy(
                ihcTests = listOf(ihcMolecularTest("HER2", "Positive"))
            )
        )
        assertThat(result).containsExactly(
            MolecularTestInterpretation(
                "IHC", listOf(MolecularTestResultInterpretation("Positive", "HER2", DEFAULT_DATE))
            )
        )
    }

    @Test
    fun `Should interpret IHC test based score value`() {
        val result = interpreter.interpret(
            BASE_PATIENT_RECORD.copy(
                ihcTests = listOf(ihcMolecularTest("HER2", scoreValue = 90.0, scoreValueUnit = "%"))
            )
        )
        assertThat(result).containsExactly(
            MolecularTestInterpretation(
                "IHC", listOf(MolecularTestResultInterpretation("HER2", "Score 90%", DEFAULT_DATE, 1))
            )
        )
    }

    @Test
    fun `Should correctly handle score based IHC test without unit`() {
        val result = interpreter.interpret(
            BASE_PATIENT_RECORD.copy(
                ihcTests = listOf(ihcMolecularTest("HER2", scoreValue = 90.0, scoreValueUnit = null))
            )
        )
        assertThat(result).containsExactly(
            MolecularTestInterpretation(
                "IHC", listOf(MolecularTestResultInterpretation("HER2", "Score 90", DEFAULT_DATE, 1))
            )
        )
    }

    @Test
    fun `Should correctly handle null date`() {
        val result = interpreter.interpret(
            BASE_PATIENT_RECORD.copy(
                ihcTests = listOf(ihcMolecularTest("HER2", "Positive").copy(measureDate = null))
            )
        )
        assertThat(result).containsExactly(
            MolecularTestInterpretation(
                "IHC", listOf(MolecularTestResultInterpretation("Positive", "HER2", null))
            )
        )
    }

    private fun ihcMolecularTest(protein: String, scoreText: String? = null, scoreValue: Double? = null, scoreValueUnit: String? = null) =
        IHCTest(
            item = protein,
            measureDate = DEFAULT_DATE,
            scoreText = scoreText,
            test = "IHC",
            scoreValue = scoreValue,
            scoreValueUnit = scoreValueUnit,
            impliesPotentialIndeterminateStatus = false
        )
}