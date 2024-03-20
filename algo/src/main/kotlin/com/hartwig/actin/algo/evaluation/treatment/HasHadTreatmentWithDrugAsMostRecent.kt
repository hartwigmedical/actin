package com.hartwig.actin.algo.evaluation.treatment


import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment

class HasHadTreatmentWithDrugAsMostRecent(private val drugToMatch: Drug) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val mostRecentDrugMatches = record.oncologicalHistory
            .maxWithOrNull(TreatmentHistoryEntryStartDateComparator())
            ?.allTreatments()?.flatMap { (it as? DrugTreatment)?.drugs ?: emptyList() }
            ?.any { it.name.lowercase() == drugToMatch.name.lowercase() }

        return when (mostRecentDrugMatches) {
            true -> {
                EvaluationFactory.pass(
                    "Patient has received ${drugToMatch.name} as most recent therapy",
                    "Has received ${drugToMatch.name} as most recent therapy"
                )
            }

            false -> {
                EvaluationFactory.fail(
                    "Patient has had prior therapy with ${drugToMatch.name} but not as the most recent line",
                    "Received ${drugToMatch.name} therapy but not as most recent line"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient has not had prior therapy with ${drugToMatch.name}",
                    "No prior ${drugToMatch.name} therapy"
                )
            }
        }
    }
}