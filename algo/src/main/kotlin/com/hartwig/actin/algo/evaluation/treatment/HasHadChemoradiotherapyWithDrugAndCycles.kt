package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.MedicationFunctions.createTreatmentHistoryEntriesFromMedications
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithAnd
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithOr
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
class HasHadChemoradiotherapyWithDrugAndCycles(private val drugsToFind: Set<Drug>, private val minCycles: Int?) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {


        val chemoradioCategoriesSet = setOf(TreatmentCategory.CHEMOTHERAPY, TreatmentCategory.RADIOTHERAPY)
        val chemoradioOncoHistory = record.oncologicalHistory.filter {
            it.categories().containsAll(chemoradioCategoriesSet)
        }
        val chemoradioMedications = record.medications?.filter { chemoradioCategoriesSet.contains(it.drug?.category) }

        val effectiveTreatmentHistory = chemoradioOncoHistory + createTreatmentHistoryEntriesFromMedications(chemoradioMedications)


        // REUSED CODE START
        val namesToMatch = drugsToFind.map { it.name.lowercase() }.toSet()
        val drugList = concatItemsWithOr(drugsToFind)

        val drugsByEvaluationResult: Map<EvaluationResult, Set<Drug>> = effectiveTreatmentHistory
            .mapNotNull { entry ->
                TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry) { treatment ->
                    (treatment as? DrugTreatment)?.drugs?.any { it.name.lowercase() in namesToMatch } == true
                }?.let { matchingEntry -> evaluateCyclesForMatchingDrugs(matchingEntry, namesToMatch) }
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { entry -> entry.value.flatten().toSet() }

        val (drugsMatchingCycles, drugsWithUnknownCycles, drugsNotMatchingCycles) =
            listOf(EvaluationResult.PASS, EvaluationResult.UNDETERMINED, EvaluationResult.FAIL).map(drugsByEvaluationResult::get)

        // REUSED CODE STOP

        return when {
            effectiveTreatmentHistory.isEmpty() -> {
                EvaluationFactory.fail("Has not received any chemoradiotherapy")
            }

            drugsMatchingCycles != null -> {
                val cyclesString = minCycles?.let { " for at least $minCycles cycles" } ?: ""
                EvaluationFactory.pass("Has received chemoradiotherapy and has received treatments with ${concatItemsWithAnd(drugsMatchingCycles)}$cyclesString")
            }

            drugsWithUnknownCycles != null -> {
                EvaluationFactory.undetermined(
                    "Has received chemoradiotherapy and has received treatments with ${concatItemsWithAnd(drugsWithUnknownCycles)} " +
                            "but undetermined if at least $minCycles cycles"
                )
            }

            effectiveTreatmentHistory.any { TrialFunctions.treatmentMayMatchAsTrial(it, drugsToFind.map(Drug::category)) } -> {
                EvaluationFactory.undetermined("Has received chemoradiotherapy but undetermined if received any treatments containing $drugList")
            }

            drugsNotMatchingCycles != null -> {
                EvaluationFactory.fail(
                    "Has received chemoradiotherapy but has received treatments with ${concatItemsWithAnd(drugsNotMatchingCycles)} " +
                            "but not at least $minCycles cycles"
                )
            }

            else -> {
                EvaluationFactory.fail("Has received chemoradiotherapy but has not received any treatments containing $drugList")
            }
        }
    }


    // REUSED CODE START
    private fun evaluateCyclesForMatchingDrugs(
        matchingEntry: TreatmentHistoryEntry,
        namesToMatch: Set<String>
    ): Pair<EvaluationResult, List<Drug>> {
        val hasCycles = matchingEntry.treatmentHistoryDetails?.cycles.let { cycles ->
            when {
                minCycles == null -> EvaluationResult.PASS
                cycles == null -> EvaluationResult.UNDETERMINED
                cycles >= minCycles -> EvaluationResult.PASS
                else -> EvaluationResult.FAIL
            }
        }

        val matchingDrugs = matchingEntry.allTreatments()
            .mapNotNull { it as? DrugTreatment }
            .flatMap { it.drugs }
            .filter { it.name.lowercase() in namesToMatch }

        return hasCycles to matchingDrugs
    }
    // REUSED CODE STOP



//    override fun evaluate(record: PatientRecord): Evaluation {
//
//
//        val treatmentMatches = record.oncologicalHistory.groupBy {
//            val matchingCategories = it.categories().containsAll(setOf(TreatmentCategory.CHEMOTHERAPY, TreatmentCategory.RADIOTHERAPY))
//            val enoughCycles= enoughCycles(it)
//
//            when {
//                matchingCategories && receivedDrugs.isNotEmpty() && enoughCycles == true -> true
//                !matchingCategories && it.categories().isNotEmpty() ||
//                        receivedDrugs.isEmpty() || enoughCycles == false -> false
//
//                else -> null
//            }
//        }

//        val effectiveTreatmentHistory = record.oncologicalHistory + createTreatmentHistoryEntriesFromMedications(record.medications)
//        val namesToMatch = drugs.map { it.name.lowercase() }.toSet()
//        val drugList = concatItemsWithOr(drugs)
//        val treatmentReceived : Boolean = record.oncologicalHistory.any {
//            it -> it.treatments.any {
//                it.categories().contains(TreatmentCategory.CHEMOTHERAPY) ||
//                    it.categories().contains(TreatmentCategory.RADIOTHERAPY)} &&
//                it.treatmentHistoryDetails?.cycles!! >= cycles
//        }
//        val drugsReceived: List<Drug>? = record.medications?.filter { drugs.contains(it.drug) }?.map { it.drug as Drug }
//
//        return if (treatmentReceived && drugsReceived?.isNotEmpty() == true) {
//            EvaluationFactory.pass("Patient did receive chemoradiotherapy with ${Format.concatItemsWithAnd(drugsReceived)} and at least $cycles cycles")
//        } else {
//            EvaluationFactory.fail("Patient did not receive chemoradiotherapy with ${Format.concatItemsWithOr(drugs)} and at least $cycles cycles")
//        }

//        return EvaluationFactory.undetermined("Undetermined if received chemoradiotherapy with ${Format.concatItemsWithOr(drugs)} and at least $cycles cycles")
//    }

//    private fun enoughCycles(treatmentHistoryEntry: TreatmentHistoryEntry): Boolean? {
//        val treatmentHistoryDetails = treatmentHistoryEntry.treatmentHistoryDetails
//        return treatmentHistoryDetails?.cycles?.let { cycles ->
//            cycles >= minCycles
//        }
//    }


}