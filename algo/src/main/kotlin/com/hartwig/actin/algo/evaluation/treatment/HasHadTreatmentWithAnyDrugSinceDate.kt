package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithOr
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import java.time.LocalDate

class HasHadTreatmentWithAnyDrugSinceDate(private val drugs: Set<Drug>, private val minDate: LocalDate) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return TreatmentSinceDateFunctions.evaluateTreatmentMatchingPredicateSinceDate(
            record, minDate, "containing '${concatItemsWithOr(drugs)}'"
        ) { it is DrugTreatment && it.drugs.intersect(drugs).isNotEmpty() }
    }
}