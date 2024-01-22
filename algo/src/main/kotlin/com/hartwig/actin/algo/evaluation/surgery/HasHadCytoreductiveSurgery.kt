package com.hartwig.actin.algo.evaluation.surgery

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadCytoreductiveSurgery : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        // TODO: once we curate surgery names from the surgeries tsv file evaluate these as well
        val oncologicalHistory = record.clinical().oncologicalHistory()

        val undeterminedSurgery = oncologicalHistory
            .any { it.categories().contains(TreatmentCategory.SURGERY) && it.treatmentName().equals("Surgery", true) }

        val hasHadCytoreductiveSurgery = oncologicalHistory
            .any {
                (it.categories().contains(TreatmentCategory.SURGERY) && it.treatmentName()
                    .contains("cytoreduct", true)) || it.treatmentName().contains("HIPEC", true)
            }

        val hasHadDebulkingSurgery = oncologicalHistory
            .any { it.categories().contains(TreatmentCategory.SURGERY) && it.treatmentName().contains("debulking", true) }

        return when {
            hasHadCytoreductiveSurgery -> {
                EvaluationFactory.pass(
                    "Patient has had cytoreductive surgery",
                    "Has had cytoreductive surgery"
                )
            }

            undeterminedSurgery -> {
                EvaluationFactory.undetermined(
                    "Undetermined if the surgery the patient received was cytoreductive",
                    "Undetermined if surgery patient received was cytoreductive"
                )
            }

            hasHadDebulkingSurgery -> {
                EvaluationFactory.undetermined("Undetermined if the performed debulking surgery meets the criteria of cytoreductive surgery")
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient has not received cytoreductive surgery",
                    "Has not received cytoreductive surgery"
                )
            }

        }
    }
}