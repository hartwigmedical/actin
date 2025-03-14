package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails

class CurrentlyGetsChemoradiotherapyWithSpecificChemotherapyTypeAndMinimumCycles(
    private val type: TreatmentType,
    private val minCycles: Int
) : EvaluationFunction {

    private val now = java.time.LocalDate.now()

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentMatches = record.oncologicalHistory.groupBy {
            when {
                (it.categories().containsAll(setOf(TreatmentCategory.CHEMOTHERAPY, TreatmentCategory.RADIOTHERAPY)) &&
                        it.isOfType(type) == true &&
                        enoughCyclesAndOngoingTreatment(it.treatmentHistoryDetails) == true) -> {
                    true
                }
                !(it.categories().containsAll(setOf(TreatmentCategory.CHEMOTHERAPY, TreatmentCategory.RADIOTHERAPY)) ||
                        it.isOfType(type) == false ||
                        enoughCyclesAndOngoingTreatment(it.treatmentHistoryDetails) == false) -> {
                    false
                }
                else -> {
                    null
                }
            }
        }

        return when {
            treatmentMatches.isEmpty() -> EvaluationFactory.fail("No treatments received")
            true in treatmentMatches -> EvaluationFactory.pass("Patient is currently getting chemoradiotherapy with $type chemotherapy and at least $minCycles cycles")
            null in treatmentMatches -> EvaluationFactory.undetermined("Undetermined if patient is currently getting chemoradiotherapy with $type chemotherapy and at least $minCycles cycles")
            else -> EvaluationFactory.fail("No chemo radio therapy with $type with at least $minCycles is currently received")
        }
    }

    fun enoughCyclesAndOngoingTreatment(treatmentHistoryDetails: TreatmentHistoryDetails?): Boolean?{
        if (treatmentHistoryDetails == null)
            return null

        val cycles = treatmentHistoryDetails.cycles
        if(cycles == null)
            return null
        else if(cycles < minCycles)
            return false

        val stopYear = treatmentHistoryDetails.stopYear
        val stopMonth = treatmentHistoryDetails.stopMonth

        return when {
            stopYear == null -> null
            now.year < stopYear -> true
            now.year > stopYear -> false
            stopMonth == null -> null
            now.monthValue <= stopMonth -> true
            else -> false
        }
    }
}