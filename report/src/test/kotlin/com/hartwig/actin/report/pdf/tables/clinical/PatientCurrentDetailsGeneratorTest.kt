package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.Gender
import com.hartwig.actin.datamodel.clinical.PatientDetails
import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.SurgeryStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDateTime

private const val KEY_WIDTH = 100f
private const val VALUE_WIDTH = 200f

class PatientCurrentDetailsGeneratorTest {
    private val minimalPatientRecord = TestPatientFactory.createMinimalTestWGSPatientRecord()

    @Test
    fun `Should return title patient current details with questionnaire date`() {

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
        assertThat(patientCurrentDetailsGenerator.title()).isEqualTo("Patient current details (19-Sep-2024)")
    }

    @Test
    fun `Should return content table with surgeries including surgery name`() {

        val endDate = LocalDateTime.of(2024, 9, 19, 1, 1).toLocalDate()
        val patientRecord = minimalPatientRecord.copy(

            surgeries = listOf(
                Surgery(name = "Surgery 2", endDate = endDate.minusDays(6), status = SurgeryStatus.FINISHED),
                Surgery(name = "Surgery 1", endDate = endDate.minusDays(2), status = SurgeryStatus.FINISHED),
                Surgery(endDate = endDate.minusDays(4), status = SurgeryStatus.FINISHED)
            )
        )

        val patientCurrentDetailsGenerator = PatientCurrentDetailsGenerator(patientRecord, KEY_WIDTH, VALUE_WIDTH)
        val table = patientCurrentDetailsGenerator.contents()

        assertThat(table.numberOfRows).isEqualTo(4)
        println(CellTestUtil.extractTextFromCell(table.getCell(3, 0)))
        println(CellTestUtil.extractTextFromCell(table.getCell(3, 1)))
        assertThat(CellTestUtil.extractTextFromCell(table.getCell(3, 0))).isEqualTo("Recent surgeries")
        assertThat(
            CellTestUtil.extractTextFromCell(
                table.getCell(
                    3,
                    1
                )
            )
        ).isEqualTo("17-Sep-2024 Surgery 1, 15-Sep-2024, 13-Sep-2024 Surgery 2")
    }
}