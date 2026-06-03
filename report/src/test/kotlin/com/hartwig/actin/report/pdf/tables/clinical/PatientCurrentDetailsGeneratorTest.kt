package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.Gender
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.PatientDetails
import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.SurgeryStatus
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.actin.report.pdf.tables.CellTestUtil.extractTextFromCell
import com.hartwig.actin.util.ApplicationConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val KEY_WIDTH = 100f
private const val VALUE_WIDTH = 200f
private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy", ApplicationConfig.LOCALE)

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
                Surgery(
                    name = "Surgery 2",
                    endDateMinus6,
                    status = SurgeryStatus.FINISHED,
                    treatmentType = OtherTreatmentType.DEBULKING_SURGERY
                ),
                Surgery(
                    name = "Surgery 1",
                    endDate,
                    status = SurgeryStatus.FINISHED,
                    treatmentType = OtherTreatmentType.CYTOREDUCTIVE_SURGERY
                ),
                Surgery(name = null, endDateMinus4, status = SurgeryStatus.FINISHED, treatmentType = OtherTreatmentType.OTHER_SURGERY)
            )
        )

        val patientCurrentDetailsGenerator =
            PatientCurrentDetailsGenerator(patientRecord, KEY_WIDTH, VALUE_WIDTH, referenceDate)
        val table = patientCurrentDetailsGenerator.contents()

        assertThat(table.numberOfRows).isEqualTo(3)
        assertThat(extractTextFromCell(table.getCell(2, 0))).isEqualTo("Recent surgeries")
        assertThat(extractTextFromCell(table.getCell(2, 1))).isEqualTo(
            "%s Surgery 1, %s, %s Surgery 2",
            DATE_FORMAT.format(endDate),
            DATE_FORMAT.format(endDateMinus4),
            DATE_FORMAT.format(endDateMinus6)
        )
    }

    @Test
    fun `Should include toxicities with grade equal or above 2 or unknown and not older than 2 years from reference date`() {
        val patientRecord = minimalPatientRecord.copy(
            comorbidities = listOf(
                toxicity("Toxicity 1", null, 3),
                toxicity("Toxicity 2", referenceDate.minusMonths(1), 2),
                toxicity("Toxicity 3", referenceDate.minusYears(5), 2),
                toxicity("Toxicity 4", referenceDate.minusMonths(1), null),
                toxicity("Toxicity 5", referenceDate.minusMonths(1), 1)
            )
        )
        val patientCurrentDetailsGenerator =
            PatientCurrentDetailsGenerator(patientRecord, KEY_WIDTH, VALUE_WIDTH, referenceDate)
        val table = patientCurrentDetailsGenerator.contents()

        assertThat(table.numberOfRows).isEqualTo(2)
        assertThat(extractTextFromCell(table.getCell(0, 0))).isEqualTo("Toxicities grade >= 2 or unknown")
        assertThat(extractTextFromCell(table.getCell(0, 1)))
            .isEqualTo("Toxicity 1 (GR 3, unknown date), Toxicity 2 (GR 2, 1/9/2024), Toxicity 4 (unknown grade, 1/9/2024)")
    }

    @Test
    fun `Should include questionnaire toxicities with details unknown`() {
        val patientRecord = minimalPatientRecord.copy(
            comorbidities = listOf(
                toxicity("", null, null, ToxicitySource.QUESTIONNAIRE),
            )
        )
        val patientCurrentDetailsGenerator =
            PatientCurrentDetailsGenerator(patientRecord, KEY_WIDTH, VALUE_WIDTH, referenceDate)
        val table = patientCurrentDetailsGenerator.contents()

        assertThat(table.numberOfRows).isEqualTo(2)
        assertThat(extractTextFromCell(table.getCell(0, 0))).isEqualTo("Toxicities grade >= 2 or unknown")
        assertThat(extractTextFromCell(table.getCell(0, 1))).isEqualTo("Yes (details unknown)")
    }

    @Test
    fun `Should include questionnaire toxicities without grade specified`() {
        val patientRecord = minimalPatientRecord.copy(
            comorbidities = listOf(
                toxicity("neuropathy", null, null, ToxicitySource.QUESTIONNAIRE),
            )
        )
        val patientCurrentDetailsGenerator =
            PatientCurrentDetailsGenerator(patientRecord, KEY_WIDTH, VALUE_WIDTH, referenceDate)
        val table = patientCurrentDetailsGenerator.contents()

        assertThat(table.numberOfRows).isEqualTo(2)
        assertThat(extractTextFromCell(table.getCell(0, 0))).isEqualTo("Toxicities grade >= 2 or unknown")
        assertThat(extractTextFromCell(table.getCell(0, 1))).isEqualTo("neuropathy (unknown date)")
    }

    private fun toxicity(name: String, date: LocalDate?, grade: Int?, source: ToxicitySource = ToxicitySource.EHR) =
        Toxicity(name, setOf(IcdCode(name)), date?.year, date?.monthValue, date?.dayOfMonth, source, grade)
}