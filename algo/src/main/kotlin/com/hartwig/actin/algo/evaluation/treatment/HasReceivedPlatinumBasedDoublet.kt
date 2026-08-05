package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasReceivedPlatinumBasedDoublet(
    private val doidModel: DoidModel,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentHistoryAnalysis = TreatmentHistoryAnalysis.create(record)
        val isNsclc = DoidEvaluationFunctions.isOfDoidType(doidModel, record.tumor.doids, DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)
        val isGynaecologicalCancer =
            DoidEvaluationFunctions.isOfDoidType(doidModel, record.tumor.doids, DoidConstants.FEMALE_REPRODUCTIVE_ORGAN_CANCER_DOID)
        val undefinedPlatinumInNsclcMessage: (String, String) -> String =
            { treatmentType, cancerType -> labels.hasReceivedPlatinumBasedDoubletPassUndefined(treatmentType, cancerType) }

        return when {
            treatmentHistoryAnalysis.receivedPlatinumDoublet() -> {
                EvaluationFactory.pass(labels.hasReceivedPlatinumBasedDoubletPass())
            }

            isNsclc && treatmentHistoryAnalysis.receivedUndefinedChemoradiation() -> {
                EvaluationFactory.pass(undefinedPlatinumInNsclcMessage("chemoradiation", "NSCLC"))
            }

            isNsclc && treatmentHistoryAnalysis.receivedUndefinedChemoImmunotherapy() -> {
                EvaluationFactory.pass(undefinedPlatinumInNsclcMessage("chemo-immunotherapy", "NSCLC"))
            }

            isGynaecologicalCancer && treatmentHistoryAnalysis.receivedUndefinedChemotherapy() -> {
                EvaluationFactory.pass(undefinedPlatinumInNsclcMessage("chemo-immunotherapy", "gynaecological cancer"))
            }

            treatmentHistoryAnalysis.receivedPlatinumTripletOrAbove() -> {
                EvaluationFactory.warn(labels.hasReceivedPlatinumBasedDoubletWarn())
            }

            else -> {
                EvaluationFactory.fail(labels.hasReceivedPlatinumBasedDoubletFail())
            }
        }
    }
}
