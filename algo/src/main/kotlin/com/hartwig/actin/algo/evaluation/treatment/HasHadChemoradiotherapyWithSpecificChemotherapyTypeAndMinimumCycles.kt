package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasHadChemoradiotherapyWithSpecificChemotherapyTypeAndMinimumCycles(private val type: TreatmentType, private val minCycles: Int) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentMatches = record.oncologicalHistory.map {
            val matchingCategories = it.categories().containsAll(setOf(TreatmentCategory.CHEMOTHERAPY, TreatmentCategory.RADIOTHERAPY))
            val chemotherapies = it.allTreatments().filter { treatment -> treatment.categories().contains(TreatmentCategory.CHEMOTHERAPY) }
            val matchingChemoType = chemotherapies.any { t -> type in t.types() }
            val allChemoTypesAreKnown = chemotherapies.all { t -> t.types().isNotEmpty() }
            val matchingCycles = it.treatmentHistoryDetails?.cycles?.let { cycles -> cycles >= minCycles }

            when {
                matchingCategories && matchingChemoType && matchingCycles == true -> true
                !matchingCategories && it.categories()
                    .isNotEmpty() || !matchingChemoType && allChemoTypesAreKnown || matchingCycles == false -> false

                else -> null
            }
        }.toSet()

        val typeString = type.display()
        return when {
            true in treatmentMatches -> EvaluationFactory.pass("Had received chemoradiotherapy with $typeString chemotherapy and at least $minCycles cycles")
            null in treatmentMatches -> EvaluationFactory.undetermined("Undetermined if patient received chemoradiotherapy with $typeString chemotherapy and at least $minCycles cycles")
            else -> EvaluationFactory.fail("Has not received chemoradiotherapy with $typeString chemotherapy")
        }
    }
}