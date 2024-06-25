package com.hartwig.actin.report.pdf.tables.soc

import com.hartwig.actin.algo.datamodel.AnnotatedTreatmentMatch
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.TreatmentCandidate
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatment
import com.hartwig.actin.efficacy.AnalysisGroup
import com.hartwig.actin.efficacy.PatientPopulation
import com.hartwig.actin.personalization.datamodel.Measurement
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Text
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val TOTAL_POPULATION = 10

class SOCGeneratorFunctionsTest {

    @Test
    fun `Should return single analysis group`() {
        val analysisGroup = AnalysisGroup(1, "ag", 5, emptyList())
        val population = patientPopulation(listOf(analysisGroup))
        assertThat(SOCGeneratorFunctions.analysisGroupForPopulation(population)).isEqualTo(analysisGroup)
    }

    @Test
    fun `Should return analysis group with all patients`() {
        val analysisGroup1 = AnalysisGroup(1, "ag1", 5, emptyList())
        val analysisGroup2 = AnalysisGroup(2, "ag2", TOTAL_POPULATION, emptyList())
        val population = patientPopulation(listOf(analysisGroup1, analysisGroup2))
        assertThat(SOCGeneratorFunctions.analysisGroupForPopulation(population)).isEqualTo(analysisGroup2)
    }

    @Test
    fun `Should return null for analysis group when multiple groups and none contain all patients`() {
        val analysisGroup1 = AnalysisGroup(1, "ag1", 5, emptyList())
        val analysisGroup2 = AnalysisGroup(2, "ag2", 3, emptyList())
        val population = patientPopulation(listOf(analysisGroup1, analysisGroup2))
        assertThat(SOCGeneratorFunctions.analysisGroupForPopulation(population)).isNull()
    }

    @Test
    fun `Should create WHO string for population`() {
        val population = patientPopulation(emptyList()).copy(
            patientsWithWho0 = 1,
            patientsWithWho0to1 = null,
            patientsWithWho1 = 3,
            patientsWithWho1to2 = 4,
            patientsWithWho2 = 5,
            patientsWithWho3 = null,
            patientsWithWho4 = 7
        )
        assertThat(SOCGeneratorFunctions.createWhoString(population)).isEqualTo("0: 1, 1: 3, 1-2: 4, 2: 5, 4: 7")
    }
   
    @Test
    fun `Should create empty WHO string for population with no WHO information`() {
        val population = patientPopulation(emptyList())
        assertThat(SOCGeneratorFunctions.createWhoString(population)).isEmpty()
    }

    @Test
    fun `Should create approved treatment cells for treatments sorted by descending general PFS`() {
        val treatments = listOf(
            annotatedTreatmentMatch(
                "t1",
                listOf(
                    evaluation(EvaluationResult.UNDETERMINED, setOf("no data")),
                    evaluation(EvaluationResult.WARN, setOf("no data", "not recommended")),
                    evaluation(EvaluationResult.PASS, setOf("approved"))
                ),
                Measurement(103.0, 100, 52, 390)
            ),
            annotatedTreatmentMatch("t2", listOf(evaluation(EvaluationResult.WARN, setOf("no data"))), Measurement(116.5, 94, 78, 431)),
            annotatedTreatmentMatch("t3", listOf(evaluation(EvaluationResult.FAIL, setOf("lab value out of range"), recoverable = true)))
        )
        val cells = SOCGeneratorFunctions.approvedTreatmentCells(treatments)
        assertThat(cells).hasSize(12)
        assertThat(cells.map(::firstTextFromOneParagraphElement)).containsExactly(
            "t2",
            "No literature efficacy evidence available yet",
            "no data",
            "116.5 (78-431)",
            "t1",
            "No literature efficacy evidence available yet",
            "no data, not recommended",
            "103.0 (52-390)",
            "t3",
            "No literature efficacy evidence available yet",
            "lab value out of range",
            NA
        )

    }

    private fun firstTextFromOneParagraphElement(cell: Cell): String? {
        val paragraph = cell.children.first() as Paragraph
        return (paragraph.children.first() as Text).text
    }

    private fun annotatedTreatmentMatch(name: String, evaluations: List<Evaluation>, pfs: Measurement? = null): AnnotatedTreatmentMatch {
        return AnnotatedTreatmentMatch(
            TreatmentCandidate(treatment(name, true), false, emptySet()), evaluations, emptyList(), pfs
        )
    }

    private fun evaluation(evaluationResult: EvaluationResult, messages: Set<String>, recoverable: Boolean = false): Evaluation {
        return when (evaluationResult) {
            EvaluationResult.PASS -> Evaluation(evaluationResult, recoverable, passGeneralMessages = messages)
            EvaluationResult.WARN -> Evaluation(evaluationResult, recoverable, warnGeneralMessages = messages)
            else -> Evaluation(evaluationResult, recoverable, undeterminedGeneralMessages = messages)
        }
    }

    private fun patientPopulation(analysisGroups: List<AnalysisGroup>) =
        PatientPopulation("test", false, 20, 80, 50.0, TOTAL_POPULATION, analysisGroups = analysisGroups)
}