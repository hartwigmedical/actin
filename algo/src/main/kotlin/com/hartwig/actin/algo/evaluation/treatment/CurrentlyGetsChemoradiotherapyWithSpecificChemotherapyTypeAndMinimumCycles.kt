package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

class CurrentlyGetsChemoradiotherapyWithSpecificChemotherapyTypeAndMinimumCycles(
    private val type: TreatmentType,
    private val minCycles: Int,
    private val referenceDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val latestStart = record.oncologicalHistory.map { LocalDate.of(it.startYear ?: 0, it.startMonth ?: 1, 1) }
            .sortedDescending()
            .firstOrNull { !it.isAfter(referenceDate) }

        val treatmentMatches = record.oncologicalHistory.groupBy {
            val matchingCategories = it.categories().containsAll(setOf(TreatmentCategory.CHEMOTHERAPY, TreatmentCategory.RADIOTHERAPY))

            val enoughCyclesAndOngoingTreatment = enoughCyclesAndOngoingTreatment(it, latestStart)

            when {
                matchingCategories && it.isOfType(type) == true && enoughCyclesAndOngoingTreatment == true -> true
                !matchingCategories && it.categories().isNotEmpty() ||
                        it.isOfType(type) == false || enoughCyclesAndOngoingTreatment == false -> false

                else -> null
            }
        }

        val typeString = type.display()
        return when {
            treatmentMatches.isEmpty() -> EvaluationFactory.fail("Currently receives chemoradiotherapy with $typeString chemotherapy")
            true in treatmentMatches -> EvaluationFactory.pass("Currently receives chemoradiotherapy with $typeString chemotherapy and at least $minCycles cycles")
            null in treatmentMatches -> EvaluationFactory.undetermined("Undetermined if patient currently receives chemoradiotherapy with $typeString chemotherapy and at least $minCycles cycles")
            else -> EvaluationFactory.fail("Does not currently receive chemoradiotherapy with $typeString chemotherapy with at least $minCycles cycles")
        }
    }

    private fun enoughCyclesAndOngoingTreatment(treatmentHistoryEntry: TreatmentHistoryEntry, latestStart: LocalDate?): Boolean? {
        val treatmentHistoryDetails = treatmentHistoryEntry.treatmentHistoryDetails
        return treatmentHistoryDetails?.cycles?.let { cycles ->
            val appearsOngoing = with(treatmentHistoryDetails) {
                val treatmentNotStopped = DateComparison.isAfterDate(referenceDate, stopYear, stopMonth)
                treatmentNotStopped == true ||
                    (treatmentNotStopped == null && latestStart?.let {
                        DateComparison.isAfterDate(latestStart, treatmentHistoryEntry.startYear, treatmentHistoryEntry.startMonth)
                    } != false)
            }
            cycles >= minCycles && appearsOngoing
        }
    }
}