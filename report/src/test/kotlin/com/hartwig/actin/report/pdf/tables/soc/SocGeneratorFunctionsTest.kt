package com.hartwig.actin.report.pdf.tables.soc

import com.hartwig.actin.datamodel.algo.AnnotatedTreatmentMatch
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.StaticMessage
import com.hartwig.actin.datamodel.algo.TreatmentCandidate
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.efficacy.AnalysisGroup
import com.hartwig.actin.datamodel.efficacy.PatientPopulation
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.IElement
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val TOTAL_POPULATION = 10

class SoCGeneratorFunctionsTest {

    @Test
    fun `Should return single analysis group`() {
        val analysisGroup = AnalysisGroup(1, "ag", 5, emptyList())
        val population = patientPopulation(listOf(analysisGroup))
        assertThat(SoCGeneratorFunctions.analysisGroupForPopulation(population)).isEqualTo(analysisGroup)
    }

    @Test
    fun `Should return analysis group with all patients`() {
        val analysisGroup1 = AnalysisGroup(1, "ag1", 5, emptyList())
        val analysisGroup2 = AnalysisGroup(2, "ag2", TOTAL_POPULATION, emptyList())
        val population = patientPopulation(listOf(analysisGroup1, analysisGroup2))
        assertThat(SoCGeneratorFunctions.analysisGroupForPopulation(population)).isEqualTo(analysisGroup2)
    }

    @Test
    fun `Should return null for analysis group when multiple groups and none contain all patients`() {
        val analysisGroup1 = AnalysisGroup(1, "ag1", 5, emptyList())
        val analysisGroup2 = AnalysisGroup(2, "ag2", 3, emptyList())
        val population = patientPopulation(listOf(analysisGroup1, analysisGroup2))
        assertThat(SoCGeneratorFunctions.analysisGroupForPopulation(population)).isNull()
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
        assertThat(SoCGeneratorFunctions.createWhoString(population)).isEqualTo("0: 1, 1: 3, 1-2: 4, 2: 5, 4: 7")
    }

    @Test
    fun `Should create empty WHO string for population with no WHO information`() {
        val population = patientPopulation(emptyList())
        assertThat(SoCGeneratorFunctions.createWhoString(population)).isEmpty()
    }

    @Test
    fun `Should use short treatment name annotation`() {
        assertThat(SoCGeneratorFunctions.abbreviate("FOLFOX+BEVACIZUMAB")).isEqualTo("FOLFOX-B")
        assertThat(SoCGeneratorFunctions.abbreviate("FOLFOX+PANITUMUMAB")).isEqualTo("FOLFOX-P")
        assertThat(SoCGeneratorFunctions.abbreviate("FOLFOX")).isEqualTo("FOLFOX")
    }

    @Test
    fun `Should create approved treatment cells for treatments sorted by descending number of annotations`() {
        val treatments = listOf(
            annotatedTreatmentMatch(
                "t1",
                listOf(
                    evaluation(EvaluationResult.UNDETERMINED, setOf("no data")),
                    evaluation(EvaluationResult.WARN, setOf("no data", "not recommended")),
                    evaluation(EvaluationResult.PASS, setOf("approved"))
                )
            ),
            annotatedTreatmentMatch("t2", listOf(evaluation(EvaluationResult.WARN, setOf("no data")))),
            annotatedTreatmentMatch("t3", listOf(evaluation(EvaluationResult.FAIL, setOf("lab value out of range"), recoverable = true)))
        )
        val cells = SoCGeneratorFunctions.approvedTreatmentCells(treatments)
        assertThat(cells).hasSize(9)
        assertThat(extractAllTextFromCell(cells)).containsExactly(
            "t1",
            "Not available yet",
            "no data, not recommended",
            "t2",
            "Not available yet",
            "no data",
            "t3",
            "Not available yet",
            "lab value out of range"
        )
    }

    private tailrec fun extractAllTextFromCell(elements: List<IElement>, textList: List<String> = emptyList()): List<String> {
        return if (elements.isEmpty()) {
            textList
        } else {
            when (val element = elements.first()) {
                is Paragraph -> {
                    val newText = listOfNotNull(element.children.filterIsInstance<Text>().firstOrNull()?.text)
                    extractAllTextFromCell(elements.drop(1), textList + newText)
                }

                is Table -> {
                    extractAllTextFromCell(element.children.filterIsInstance<Cell>() + elements.drop(1), textList)
                }

                is Cell -> {
                    extractAllTextFromCell(element.children + elements.drop(1), textList)
                }

                else -> extractAllTextFromCell(elements.drop(1), textList)
            }
        }
    }

    private fun annotatedTreatmentMatch(name: String, evaluations: List<Evaluation>): AnnotatedTreatmentMatch {
        return AnnotatedTreatmentMatch(
            TreatmentCandidate(treatment(name, true), false, emptySet()), evaluations, emptyList(), emptyList()
        )
    }

    private fun evaluation(evaluationResult: EvaluationResult, messages: Set<String>, recoverable: Boolean = false): Evaluation {
        return when (evaluationResult) {
            EvaluationResult.PASS -> Evaluation(evaluationResult, recoverable, passMessages = staticMessages(messages))
            EvaluationResult.WARN -> Evaluation(evaluationResult, recoverable, warnMessages = staticMessages(messages))
            else -> Evaluation(evaluationResult, recoverable, undeterminedMessages = staticMessages(messages))
        }
    }

    private fun staticMessages(messages: Set<String>) = messages.map { StaticMessage(it) }.toSet()

    private fun patientPopulation(analysisGroups: List<AnalysisGroup>) =
        PatientPopulation("test", false, 20, 80, 50.0, TOTAL_POPULATION, analysisGroups = analysisGroups)
}