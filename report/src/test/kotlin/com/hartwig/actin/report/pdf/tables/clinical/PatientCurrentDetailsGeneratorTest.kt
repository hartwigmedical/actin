package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.Gender
import com.hartwig.actin.datamodel.clinical.PatientDetails
import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.SurgeryStatus
import com.hartwig.actin.report.pdf.tables.clinical.CellTestUtil.extractTextFromCell
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val KEY_WIDTH = 100f
private const val VALUE_WIDTH = 200f
private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy")

class PatientCurrentDetailsGeneratorTest {
    private val minimalPatientRecord = TestPatientFactory.createMinimalTestWGSPatientRecord()

    @Test
    fun `Should return title with questionnaire date`() {

        val questionnaireDate = LocalDateTime.of(2024, 9, 19, 1, 1).toLocalDate()

        val patientRecord = minimalPatientRecord.copy(
            patient = PatientDetails(
                gender = Gender.MALE,
                birthYear = 1950,
                registrationDate = questionnaireDate,
                questionnaireDate = questionnaireDate,
                hasHartwigSequencing = true
            )
        )

        val patientCurrentDetailsGenerator = PatientCurrentDetailsGenerator(patientRecord, KEY_WIDTH, VALUE_WIDTH)
        assertThat(patientCurrentDetailsGenerator.title()).isEqualTo("Patient current details (%s)", DATE_FORMAT.format(questionnaireDate))
    }

    @Test
    fun `Should return content table with surgeries including surgery name`() {

        val endDate = LocalDateTime.of(2024, 9, 19, 1, 1).toLocalDate()
        val endDateMinus6 = endDate.minusDays(6)
        val endDateMinus4 = endDate.minusDays(4)

        val patientRecord = minimalPatientRecord.copy(

            surgeries = listOf(
                Surgery(name = "Surgery 2", endDateMinus6, status = SurgeryStatus.FINISHED),
                Surgery(name = "Surgery 1", endDate, status = SurgeryStatus.FINISHED),
                Surgery(name = null, endDateMinus4, status = SurgeryStatus.FINISHED)
            )
        )

        val patientCurrentDetailsGenerator = PatientCurrentDetailsGenerator(patientRecord, KEY_WIDTH, VALUE_WIDTH)
        val table = patientCurrentDetailsGenerator.contents()

        assertThat(table.numberOfRows).isEqualTo(4)
        assertThat(extractTextFromCell(table.getCell(3, 0))).isEqualTo("Recent surgeries")
        assertThat(
            extractTextFromCell(
                table.getCell(
                    3,
                    1
                )
            )
        ).isEqualTo(
            "%s Surgery 1, %s, %s Surgery 2",
            DATE_FORMAT.format(endDate),
            DATE_FORMAT.format(endDateMinus4),
            DATE_FORMAT.format(endDateMinus6)
        )
    }
}