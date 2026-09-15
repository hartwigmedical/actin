package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasHadOrganTransplant(private val icdModel: IcdModel, private val minYear: Int?) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val matchingComorbidities = icdModel.findInstancesMatchingAnyIcdCode(
            record.comorbidities, IcdConstants.TRANSPLANTATION_SET.map { IcdCode(it) }.toSet()
        ).fullMatches

        val grouped = matchingComorbidities.groupBy { comorbidity ->
            if (minYear == null) true else comorbidity.year?.let { it >= minYear }
        }
        val passesDateRequirement = grouped[true] ?: emptyList()
        val withUnknownDate = grouped[null]

        return when {
            passesDateRequirement.isNotEmpty() -> {
                val dateMessage = minYear?.let { " since $minYear" } ?: ""
                EvaluationFactory.pass("An organ transplant$dateMessage in provided history")
            }

            !withUnknownDate.isNullOrEmpty() -> {
                EvaluationFactory.undetermined("An organ transplant in provided history but unclear if after $minYear (date unknown)")
            }

            else -> EvaluationFactory.fail("No organ transplant in provided history")
        }
    }
}