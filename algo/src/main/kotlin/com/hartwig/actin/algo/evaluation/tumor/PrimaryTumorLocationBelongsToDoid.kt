package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions.isOfAtLeastOneDoidType

class PrimaryTumorLocationBelongsToDoid(doidModel: DoidModel, doidToMatch: String) : EvaluationFunction {

    private val doidModel: DoidModel
    private val doidToMatch: String

    init {
        this.doidModel = doidModel
        this.doidToMatch = doidToMatch
    }

    override fun evaluate(record: PatientRecord): Evaluation {
        val doidTerm: String? = doidModel.resolveTermForDoid(doidToMatch)
        val tumorDoids = record.clinical().tumor().doids()
        return when {
            !DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids) ->
                EvaluationFactory.undetermined("Tumor type of patient is not configured", "Unknown tumor type")

            DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, doidToMatch) ->
                EvaluationFactory.pass("Patient has $doidTerm", "Tumor type")

            isPotentialAdenoSquamousMatch(tumorDoids!!, doidToMatch) ->
                EvaluationFactory.warn(
                    "Unclear whether tumor type of patient can be considered " + doidTerm
                            + ", because patient has adenosquamous tumor type", "Unclear if tumor type is considered $doidTerm"
                )

            isUndeterminateUnderMainCancerType(tumorDoids, doidToMatch) -> EvaluationFactory.undetermined(
                "Could not determine based on configured tumor type if patient may have $doidTerm",
                "Undetermined if $doidTerm"
            )

            else -> EvaluationFactory.fail("Patient has no $doidTerm", "Tumor type")
        }
    }

    private fun isPotentialAdenoSquamousMatch(patientDoids: Set<String>, doidToMatch: String): Boolean {
        val doidTreeToMatch: Set<String> = doidModel.adenoSquamousMappingsForDoid(doidToMatch).map { it.adenoSquamousDoid() }.toSet()
        return isOfAtLeastOneDoidType(doidModel, patientDoids, doidTreeToMatch)
    }

    private fun isUndeterminateUnderMainCancerType(tumorDoids: Set<String>, doidToMatch: String): Boolean {
        val fullDoidToMatchTree: Set<String> = doidModel.doidWithParents(doidToMatch)
        val mainCancerTypesToMatch: Set<String> = doidModel.mainCancerDoids(doidToMatch)
        for (tumorDoid in tumorDoids) {
            val fullTumorDoidTree: Set<String> = doidModel.doidWithParents(tumorDoid)
            for (doid in fullTumorDoidTree) {
                if (mainCancerTypesToMatch.contains(doid) && fullDoidToMatchTree.contains(tumorDoid)
                    && !fullTumorDoidTree.contains(doidToMatch)
                ) {
                    return true
                }
            }
        }
        return false
    }
}