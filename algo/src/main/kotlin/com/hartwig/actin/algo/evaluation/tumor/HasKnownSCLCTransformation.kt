package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants.LUNG_ADENOCARCINOMA_DOID
import com.hartwig.actin.algo.doid.DoidConstants.LUNG_ADENOSQUAMOUS_CARCINOMA_DOID
import com.hartwig.actin.algo.doid.DoidConstants.LUNG_COMBINED_TYPE_SMALL_CELL_CARCINOMA_DOID
import com.hartwig.actin.algo.doid.DoidConstants.LUNG_LARGE_CELL_CARCINOMA_DOID
import com.hartwig.actin.algo.doid.DoidConstants.LUNG_MIXED_SMALL_CELL_AND_SQUAMOUS_CELL_CARCINOMA_DOID
import com.hartwig.actin.algo.doid.DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID
import com.hartwig.actin.algo.doid.DoidConstants.LUNG_NON_SQUAMOUS_NON_SMALL_CARCINOMA_DOID
import com.hartwig.actin.algo.doid.DoidConstants.LUNG_OCCULT_SMALL_CELL_CARCINOMA_DOID
import com.hartwig.actin.algo.doid.DoidConstants.LUNG_SMALL_CELL_CARCINOMA_DOID
import com.hartwig.actin.algo.doid.DoidConstants.LUNG_SQUAMOUS_CELL_CARCINOMA_DOID
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.doid.DoidModel

class HasKnownSCLCTransformation(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined(
                "Could not determine whether SCLC transformation occurred (tumor doids missing)",
                "Undetermined SCLC transformation (tumor doids missing)"
            )
        }
        val currentlyIsSCLC = DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, tumorDoids, SCLC_DOID_SET)
        val priorNSCLC = record.priorSecondPrimaries.flatMap { it.doids }.any { it in NSCLC_DOID_SET }

        return when {
            (currentlyIsSCLC && priorNSCLC) -> {
                EvaluationFactory.pass(
                    "Patient has lung cancer with transformation to small cell lung cancer",
                    "Has small cell lung cancer transformation"
                )
            }

            (currentlyIsSCLC) -> {
                EvaluationFactory.undetermined(
                    "Patient has SCLC but undetermined if SCLC transformation occurred",
                    "Undetermined SCLC transformation"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient does not have lung cancer with transformation to small cell lung cancer",
                    "No small cell lung cancer transformation"
                )
            }
        }
    }

    companion object {
        private val SCLC_DOID_SET = setOf(
            LUNG_SMALL_CELL_CARCINOMA_DOID,
            LUNG_OCCULT_SMALL_CELL_CARCINOMA_DOID,
            LUNG_COMBINED_TYPE_SMALL_CELL_CARCINOMA_DOID,
            LUNG_MIXED_SMALL_CELL_AND_SQUAMOUS_CELL_CARCINOMA_DOID
        )
        private val NSCLC_DOID_SET = setOf(
            LUNG_SQUAMOUS_CELL_CARCINOMA_DOID,
            LUNG_ADENOSQUAMOUS_CARCINOMA_DOID,
            LUNG_NON_SMALL_CELL_CARCINOMA_DOID,
            LUNG_NON_SQUAMOUS_NON_SMALL_CARCINOMA_DOID,
            LUNG_ADENOCARCINOMA_DOID,
            LUNG_LARGE_CELL_CARCINOMA_DOID
        )
    }
}