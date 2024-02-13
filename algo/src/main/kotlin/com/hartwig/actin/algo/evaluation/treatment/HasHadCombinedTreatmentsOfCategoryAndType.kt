package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadCombinedTreatmentsOfCategoryAndType(
    firstTreatmentCategory: TreatmentCategory, firstTreatmentType: DrugType?,
    secondTreatmentCategory: TreatmentCategory, secondTreatmentType: DrugType?,
    displayOverrule: String?
) : EvaluationFunction {

    private val treatmentDisplay = if (displayOverrule != null) {
        displayOverrule
    } else {
        val firstDrugDisplay = firstTreatmentType?.display() ?: firstTreatmentCategory.display()
        val secondDrugDisplay = secondTreatmentType?.display() ?: secondTreatmentCategory.display()
        "combined therapy with $firstDrugDisplay and $secondDrugDisplay"
    }

    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined(
            "Undetermined if patient has received $treatmentDisplay",
            "Undetermined prior $treatmentDisplay "
        )
    }

}