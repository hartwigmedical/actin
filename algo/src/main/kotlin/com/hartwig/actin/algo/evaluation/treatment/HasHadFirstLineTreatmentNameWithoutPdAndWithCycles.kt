package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.TreatmentHistoryEntryFunctions.containsTreatment
import com.hartwig.actin.clinical.interpretation.ProgressiveDiseaseFunctions
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry

class HasHadFirstLineTreatmentNameWithoutPdAndWithCycles(
    private val treatmentName: String, private val minCycles: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val systemic = record.oncologicalHistory.filter(SystemicTreatmentAnalyser::treatmentHistoryEntryIsSystemic)
        val (targetWithDate, targetWithoutDate) = systemic.filter { it.containsTreatment(treatmentName) }.partition { it.startYear != null }
        val datedFirst = SystemicTreatmentAnalyser.firstSystemicTreatment(systemic.filter { it.startYear != null })
        val candidateFirst = datedFirst ?: systemic.singleOrNull()
        val targetIsFirstLine = candidateFirst?.containsTreatment(treatmentName) == true

        val messageStartNegative = "Has not received $treatmentName"
        val messageStartPositive = "Has received $treatmentName"
        val asFirstLine = " as first-line treatment"
        val messageEnd = " without PD and with at least $minCycles cycles"

        return when {
            (targetWithDate + targetWithoutDate).isEmpty() -> EvaluationFactory.fail(messageStartNegative)

            targetWithoutDate.isEmpty() && datedFirst?.containsTreatment(treatmentName) == false ->
                EvaluationFactory.fail(messageStartNegative + asFirstLine)

            targetIsFirstLine && pdStatus(candidateFirst) == true ->
                EvaluationFactory.fail("$messageStartPositive$asFirstLine but resulted in PD")

            targetIsFirstLine && cyclesMeets(candidateFirst) == false ->
                EvaluationFactory.fail("$messageStartPositive$asFirstLine but with less than $minCycles cycles")

            !targetIsFirstLine && targetWithoutDate.isNotEmpty() && systemic.size > 1 ->
                EvaluationFactory.undetermined("$messageStartPositive but undetermined if first line (dates missing)")

            targetIsFirstLine && pdStatus(candidateFirst) == null ->
                EvaluationFactory.undetermined("$messageStartPositive$asFirstLine but undetermined if without PD")

            targetIsFirstLine && cyclesMeets(candidateFirst) == null ->
                EvaluationFactory.undetermined("$messageStartPositive$asFirstLine but undetermined if with at least $minCycles cycles")

            meetsPdAndCycles(candidateFirst) ->
                EvaluationFactory.pass(messageStartPositive + asFirstLine + messageEnd)

            else ->
                EvaluationFactory.fail(messageStartNegative + asFirstLine + messageEnd)
        }
    }

    fun pdStatus(entry: TreatmentHistoryEntry?): Boolean? = entry?.let { ProgressiveDiseaseFunctions.treatmentResultedInPD(it) }
    fun cyclesMeets(entry: TreatmentHistoryEntry?): Boolean? = entry?.treatmentHistoryDetails?.cycles?.let { it >= minCycles }
    fun meetsPdAndCycles(entry: TreatmentHistoryEntry?): Boolean = pdStatus(entry) == false && cyclesMeets(entry) == true
}