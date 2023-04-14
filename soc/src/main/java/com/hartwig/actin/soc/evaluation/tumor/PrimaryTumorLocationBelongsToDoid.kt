package com.hartwig.actin.soc.evaluation.tumor

import com.google.common.collect.Sets
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation

class PrimaryTumorLocationBelongsToDoid(doidModel: DoidModel, doidToMatch: String) : EvaluationFunction {
    private val doidModel: DoidModel
    private val doidToMatch: String

    init {
        this.doidModel = doidModel
        this.doidToMatch = doidToMatch
    }

    fun evaluate(record: PatientRecord): Evaluation {
        val doidTerm: String = doidModel.resolveTermForDoid(doidToMatch)
        val tumorDoids = record.clinical().tumor().doids()
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Tumor type of patient is not configured")
                    .addUndeterminedGeneralMessages("Unknown tumor type")
                    .build()
        }
        if (DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, doidToMatch)) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has $doidTerm")
                    .addPassGeneralMessages("Tumor type")
                    .build()
        }
        if (isPotentialAdenoSquamousMatch(tumorDoids!!, doidToMatch)) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages("Unclear whether tumor type of patient can be considered " + doidTerm
                            + ", because patient has adenosquamous tumor type")
                    .addWarnGeneralMessages("Tumor type")
                    .build()
        }
        return if (isUndeterminateUnderMainCancerType(tumorDoids, doidToMatch)) {
            EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Could not determine based on configured tumor type if patient may have $doidTerm")
                    .addUndeterminedGeneralMessages("Tumor type")
                    .build()
        } else EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Patient has no $doidTerm")
                .addFailGeneralMessages("Tumor type")
                .build()
    }

    private fun isPotentialAdenoSquamousMatch(patientDoids: Set<String>, doidToMatch: String): Boolean {
        val doidTreeToMatch: Set<String> = doidModel.doidWithParents(doidToMatch)
        val patientDoidTree = expandToFullDoidTree(patientDoids)
        for (doidEntryToMatch in doidTreeToMatch) {
            for (mapping in doidModel.adenoSquamousMappingsForDoid(doidEntryToMatch)) {
                if (patientDoidTree.contains(mapping.adenoSquamousDoid())) {
                    return true
                }
            }
        }
        return false
    }

    private fun isUndeterminateUnderMainCancerType(tumorDoids: Set<String>, doidToMatch: String): Boolean {
        val fullDoidToMatchTree: Set<String> = doidModel.doidWithParents(doidToMatch)
        val mainCancerTypesToMatch: Set<String> = doidModel.mainCancerDoids(doidToMatch)
        for (tumorDoid in tumorDoids) {
            val fullTumorDoidTree: Set<String> = doidModel.doidWithParents(tumorDoid)
            for (doid in fullTumorDoidTree) {
                if (mainCancerTypesToMatch.contains(doid) && fullDoidToMatchTree.contains(tumorDoid)
                        && !fullTumorDoidTree.contains(doidToMatch)) {
                    return true
                }
            }
        }
        return false
    }

    private fun expandToFullDoidTree(doids: Set<String>): Set<String> {
        val doidTree: MutableSet<String> = Sets.newHashSet()
        for (doid in doids) {
            doidTree.addAll(doidModel.doidWithParents(doid))
        }
        return doidTree
    }
}