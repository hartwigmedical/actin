package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasNonMuscleInvasiveBladderCancer(private val doidModel: DoidModel, private val labels: EvaluationLabels.Tumor) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined(labels.hasNonMuscleInvasiveBladderCancerUndeterminedDoidsMissing())
        }
        val isBladderCancer = DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.URINARY_BLADDER_CANCER_DOID)
        val isNonMuscleInvasive = NON_MUSCLE_INVASIVE_TERMS.any { record.tumor.name.lowercase().contains(it) }

        return when {
            isBladderCancer && isNonMuscleInvasive -> EvaluationFactory.pass(labels.hasNonMuscleInvasiveBladderCancerPass())
            isBladderCancer -> EvaluationFactory.undetermined(labels.hasNonMuscleInvasiveBladderCancerUndetermined())
            else -> EvaluationFactory.fail(labels.hasNonMuscleInvasiveBladderCancerFail())
        }
    }

    companion object {
        val NON_MUSCLE_INVASIVE_TERMS = setOf("non muscle invasive", "non-muscle invasive", "NMIBC")
    }
}