package com.hartwig.actin.report.pdf.tables.soc

import com.hartwig.actin.datamodel.algo.AnnotatedTreatmentMatch
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.StaticMessage
import com.hartwig.actin.datamodel.algo.TreatmentCandidate
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.efficacy.AnalysisGroup
import com.hartwig.actin.datamodel.efficacy.PatientPopulation
import com.hartwig.actin.datamodel.personalization.Measurement
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.IElement
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
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
    fun `Should use short treatment name annotation`() {
        assertThat(SOCGeneratorFunctions.abbreviate("FOLFOX+BEVACIZUMAB")).isEqualTo("FOLFOX-B")
        assertThat(SOCGeneratorFunctions.abbreviate("FOLFOX+PANITUMUMAB")).isEqualTo("FOLFOX-P")
        assertThat(SOCGeneratorFunctions.abbreviate("FOLFOX")).isEqualTo("FOLFOX")
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
                Measurement(103.0, 100, 52, 390, 100.0)
            ),
            annotatedTreatmentMatch("t2", listOf(evaluation(EvaluationResult.WARN, setOf("no data"))), Measurement(116.5, 94, 78, 431)),
            annotatedTreatmentMatch("t3", listOf(evaluation(EvaluationResult.FAIL, setOf("lab value out of range"), recoverable = true)))
        )
        val cells = SOCGeneratorFunctions.approvedTreatmentCells(treatments)
        assertThat(cells).hasSize(12)
        assertThat(extractAllTextFromCell(cells)).containsExactly(
            "t2",
            "Not available yet",
            "PFS: ",
            "3.8 months",
            "OS: ",
            NA,
            "no data",
            "t1",
            "Not available yet",
            "PFS: ",
            "3.4 months, IQR: 3.3",
            "OS: ",
            NA,
            "no data, not recommended",
            "t3",
            "Not available yet",
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

    private fun annotatedTreatmentMatch(
        name: String,
        evaluations: List<Evaluation>,
        pfs: Measurement? = null,
        os: Measurement? = null
    ): AnnotatedTreatmentMatch {
        return AnnotatedTreatmentMatch(
            TreatmentCandidate(treatment(name, true), false, emptySet()), evaluations, emptyList(), pfs, os, emptyList()
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