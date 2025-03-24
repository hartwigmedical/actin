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
        val latestStart = record.oncologicalHistory.maxOfOrNull { LocalDate.of(it.startYear ?: 0, it.startMonth ?: 1, 1) }
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

        return when {
            treatmentMatches.isEmpty() -> EvaluationFactory.fail("The patient doesn't currently get chemoradiotherapy with $type chemo")
            true in treatmentMatches -> EvaluationFactory.pass("Patient is currently getting chemoradiotherapy with $type chemotherapy and at least $minCycles cycles")
            null in treatmentMatches -> EvaluationFactory.undetermined("Undetermined if patient is currently getting chemoradiotherapy with $type chemotherapy and at least $minCycles cycles")
            else -> EvaluationFactory.fail("No chemoradiotherapy with $type with at least $minCycles is currently received")
        }
    }

    private fun enoughCyclesAndOngoingTreatment(treatmentHistoryEntry: TreatmentHistoryEntry, latestStart: LocalDate?): Boolean? {
        val treatmentHistoryDetails = treatmentHistoryEntry.treatmentHistoryDetails
        return treatmentHistoryDetails?.cycles?.let { cycles ->
            val appearsOngoing = with(treatmentHistoryDetails) {
                val treatmentNotStopped = DateComparison.isAfterDate(referenceDate, stopYear, stopMonth)
                treatmentNotStopped == true ||
                    (treatmentNotStopped == null && (latestStart?.let {
                        DateComparison.isAfterDate(latestStart, treatmentHistoryEntry.startYear, treatmentHistoryEntry.startMonth)
                    }) != false)
            }
            cycles >= minCycles && appearsOngoing
        }
    }
}