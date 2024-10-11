package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.Gender
import com.hartwig.actin.datamodel.clinical.PatientDetails
import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.SurgeryStatus
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.report.pdf.tables.clinical.CellTestUtil.extractTextFromCell
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val KEY_WIDTH = 100f
private const val VALUE_WIDTH = 200f
private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy")

class PatientCurrentDetailsGeneratorTest {
    private val minimalPatientRecord = TestPatientFactory.createMinimalTestWGSPatientRecord()
    private val referenceDate = LocalDate.of(2024, 10, 1)

    @Test
    fun `Should return title with questionnaire date`() {
        val questionnaireDate = LocalDate.of(2024, 9, 19)

        val patientRecord = minimalPatientRecord.copy(
            patient = PatientDetails(
                gender = Gender.MALE,
                birthYear = 1950,
                registrationDate = questionnaireDate,
                questionnaireDate = questionnaireDate,
                hasHartwigSequencing = true
            )
        )

        val patientCurrentDetailsGenerator =
            PatientCurrentDetailsGenerator(patientRecord, KEY_WIDTH, VALUE_WIDTH, referenceDate)
        assertThat(patientCurrentDetailsGenerator.title())
            .isEqualTo("Patient current details (%s)", DATE_FORMAT.format(questionnaireDate))
    }

    @Test
    fun `Should return content table with surgeries including surgery name`() {
        val endDate = LocalDate.of(2024, 9, 19)
        val endDateMinus6 = endDate.minusDays(6)
        val endDateMinus4 = endDate.minusDays(4)

        val patientRecord = minimalPatientRecord.copy(
            surgeries = listOf(
                Surgery(name = "Surgery 2", endDateMinus6, status = SurgeryStatus.FINISHED),
                Surgery(name = "Surgery 1", endDate, status = SurgeryStatus.FINISHED),
                Surgery(name = null, endDateMinus4, status = SurgeryStatus.FINISHED)
            )
        )

        val patientCurrentDetailsGenerator =
            PatientCurrentDetailsGenerator(patientRecord, KEY_WIDTH, VALUE_WIDTH, referenceDate)
        val table = patientCurrentDetailsGenerator.contents()

        assertThat(table.numberOfRows).isEqualTo(4)
        assertThat(extractTextFromCell(table.getCell(3, 0))).isEqualTo("Recent surgeries")
        assertThat(extractTextFromCell(table.getCell(3, 1))).isEqualTo(
            "%s Surgery 1, %s, %s Surgery 2",
            DATE_FORMAT.format(endDate),
            DATE_FORMAT.format(endDateMinus4),
            DATE_FORMAT.format(endDateMinus6)
        )
    }

    @Test
    fun `Should include toxicities with known sufficient grade that were unresolved as of reference date`() {
        val patientRecord = minimalPatientRecord.copy(
            toxicities = listOf(
                toxicity("Toxicity 1", null, 3),
                toxicity("Toxicity 2", referenceDate.plusMonths(1), 2),
                toxicity("Toxicity 3", referenceDate.minusDays(5), 2),
                toxicity("Toxicity 4", referenceDate.plusMonths(1), null),
                toxicity("Toxicity 5", referenceDate.plusMonths(1), 1)
            )
        )
        val patientCurrentDetailsGenerator =
            PatientCurrentDetailsGenerator(patientRecord, KEY_WIDTH, VALUE_WIDTH, referenceDate)
        val table = patientCurrentDetailsGenerator.contents()

        assertThat(table.numberOfRows).isEqualTo(3)
        assertThat(extractTextFromCell(table.getCell(0, 0))).isEqualTo("Unresolved toxicities grade => 2")
        assertThat(extractTextFromCell(table.getCell(0, 1))).isEqualTo("From EHR: Toxicity 1 (3), Toxicity 2 (2)")
    }

    private fun toxicity(name: String, endDate: LocalDate?, grade: Int?) =
        Toxicity(name, emptySet(), referenceDate.minusMonths(1), ToxicitySource.EHR, grade, endDate)
}