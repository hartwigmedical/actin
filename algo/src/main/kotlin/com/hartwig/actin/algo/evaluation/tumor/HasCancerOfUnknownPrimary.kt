package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.trial.input.datamodel.TumorTypeInput

class HasCancerOfUnknownPrimary(private val doidModel: DoidModel, private val tumorType: TumorTypeInput) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined("Undetermined if cancer is of unknown primary")
        }
        val isCUP = TumorEvaluationFunctions.hasCancerOfUnknownPrimary(record.tumor.name)
        val hasTargetTumorType = DoidEvaluationFunctions.isOfExclusiveDoidType(doidModel, tumorDoids, tumorType.doid())
        val hasOrganSystemCancer = DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.ORGAN_SYSTEM_CANCER_DOID)

        return when {
            hasTargetTumorType && !hasOrganSystemCancer -> {
                if (isCUP) {
                    EvaluationFactory.pass("Cancer is of unknown primary")
                } else {
                    EvaluationFactory.warn("Undetermined if ${record.tumor.name} may be cancer of unknown primary")
                }
            }

            DoidEvaluationFunctions.isOfExactDoid(tumorDoids, DoidConstants.CANCER_DOID) -> {
                if (isCUP) {
                    EvaluationFactory.undetermined("Cancer is of unknown primary but undetermined if ${tumorType.display()}")
                } else {
                    EvaluationFactory.undetermined("Undetermined if ${record.tumor.name} may be cancer of unknown primary")
                }
            }

            else -> EvaluationFactory.fail("No cancer of unknown primary")
        }
    }
}