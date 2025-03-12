package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.interpretation.InterpretedCohortTestFactory
import com.itextpdf.kernel.pdf.PdfName
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Table
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val KEY_FOR_URI_IN_CELL = 1

class ActinGeneralizedTrialGeneratorFunctionsTest {

    private val cohort1 = InterpretedCohortTestFactory.interpretedCohort(
        "trial1",
        "T1",
        isPotentiallyEligible = true,
        isOpen = true,
        hasSlotsAvailable = false,
        molecularEvents = setOf("MSI"),
        cohort = "Cohort A",
        nctId = "nct001"
    )
    private val cohort2 = cohort1.copy("trial2", name = "Cohort B", source = TrialSource.NKI, nctId = "")
    private val cohort3 =
        cohort1.copy("trial3", name = "Cohort C", source = TrialSource.LKO, locations = listOf("Erasmus"))

    @Test
    fun `Should return correct table title based on source`() {
        assertThat(ActinTrialGeneratorFunctions.createTableTitleStart(null)).isEqualTo("Trials")
        assertThat(ActinTrialGeneratorFunctions.createTableTitleStart("")).isEqualTo(" trials")
        assertThat(ActinTrialGeneratorFunctions.createTableTitleStart("1")).isEqualTo("1 trials")
    }

    @Test
    fun `Should return all cohorts in the own list when source is null`() {
        val (own, others) = ActinTrialGeneratorFunctions.partitionBySource(listOf(cohort1, cohort2, cohort3), null)
        assertThat(own).size().isEqualTo(3)
        assertThat(others).isEmpty()
    }

    @Test
    fun `Should return all cohorts in the primary list when there are no other sources`() {
        val (primary, others) = ActinTrialGeneratorFunctions.partitionBySource(
            listOf(cohort1, cohort2, cohort3.copy(source = null)),
            TrialSource.NKI
        )
        assertThat(primary).size().isEqualTo(3)
        assertThat(others).isEmpty()
    }

    @Test
    fun `Should return cohorts in both lists when there are other sources`() {
        val (primary, others) = ActinTrialGeneratorFunctions.partitionBySource(listOf(cohort1, cohort2, cohort3), TrialSource.NKI)
        assertThat(primary).size().isEqualTo(2)
        assertThat(others).size().isEqualTo(1)
    }

    @Test
    fun `Should render non-local trial as a link to clinicaltrials DOT gov if its nctId is available`() {
        val cellCaptor = mutableListOf<Cell>()
        val table = mockk<Table> {
            every { addCell(capture(cellCaptor)) } returns mockk()
        }
        val feedbackFunction = InterpretedCohort::warnings
        val columnWidths = listOf(1f, 1f, 1f, 1f).toFloatArray()
        ActinTrialGeneratorFunctions.addTrialsToTable(
            listOf(cohort1, cohort2),
            table,
            columnWidths,
            feedbackFunction,
            false,
            includeLocation = true
        )

        val trialCellOne = cellCaptor.first()
        assertThat(trialCellOne.children).hasSize(1)
        val actionOne = trialCellOne.getProperty<PdfAction>(KEY_FOR_URI_IN_CELL)
        val urisOne = actionOne.pdfObject.keySet().filter { it.type == PdfName.URI.type && it.value == "URI" }
        assertThat(urisOne).hasSize(1)
        assertThat(actionOne.pdfObject.get(urisOne.first()).toString()).isEqualTo("https://clinicaltrials.gov/study/${cohort1.nctId}")

        val trialCellTwo = cellCaptor.get(2)
        assertThat(trialCellTwo.children).hasSize(1)
        assertThat(trialCellTwo.hasProperty(KEY_FOR_URI_IN_CELL)).isFalse()
    }
}