package com.hartwig.actin.algo.evaluation.surgery

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.SurgeryType
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

class HasHadCytoreductiveSurgery : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val oncologicalHistory = record.oncologicalHistory

        val undeterminedSurgery = oncologicalHistory
            .any { it.categories().contains(TreatmentCategory.SURGERY) && it.treatmentName().equals("surgery", true) }

        val hasHadCytoreductiveSurgery = record.surgeries.any { it.type == SurgeryType.CYTOREDUCTIVE_SURGERY }
                || oncologicalHistory.any {
                    it.isOfType(OtherTreatmentType.CYTOREDUCTIVE_SURGERY) == true || it.allTreatments()
                        .any { treatment -> treatment.name.uppercase() == "HIPEC" }
                }

        val hasHadDebulkingSurgery = record.surgeries.any { it.type == SurgeryType.DEBULKING_SURGERY }
                || oncologicalHistory
                    .any { it.isOfType(OtherTreatmentType.DEBULKING_SURGERY) == true }

        return when {
            hasHadCytoreductiveSurgery -> {
                EvaluationFactory.pass("Has had cytoreductive surgery")
            }

            undeterminedSurgery -> {
                EvaluationFactory.undetermined("Undetermined if performed surgery was cytoreductive")
            }

            hasHadDebulkingSurgery -> {
                EvaluationFactory.undetermined("Undetermined if performed debulking surgery meets the criteria of cytoreductive surgery")
            }

            else -> {
                EvaluationFactory.fail("Has not received cytoreductive surgery")
            }

        }
    }
}