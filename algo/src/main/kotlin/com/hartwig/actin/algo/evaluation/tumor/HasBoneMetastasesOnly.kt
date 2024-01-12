package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasBoneMetastasesOnly : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasBoneMetastases = record.clinical.tumor.hasBoneLesions ?: return EvaluationFactory.undetermined(
            "Data regarding presence of bone metastases is missing", "Missing bone metastasis data"
        )
        val hasLiverMetastases = record.clinical.tumor.hasLiverLesions
        val hasCnsMetastases = record.clinical.tumor.hasCnsLesions
        val hasBrainMetastases = record.clinical.tumor.hasBrainLesions
        val hasLungLesions = record.clinical.tumor.hasLungLesions
        val hasLymphNodeLesions = record.clinical.tumor.hasLymphNodeLesions
        val otherLesions = record.clinical.tumor.otherLesions
        if (hasBoneMetastases && listOf(
                hasLiverMetastases,
                hasCnsMetastases,
                hasBrainMetastases,
                hasLungLesions,
                hasLymphNodeLesions,
                otherLesions
            ).all { it == null }
        ) {
            return EvaluationFactory.warn(
                "Patient has bone lesions but data regarding other lesion locations is missing, so unknown if patient has only bone metastases",
                "Lesion location data is missing: unknown if bone metastases only"
            )
        }
        val hasOtherLesion = !otherLesions.isNullOrEmpty()
        val hasAnyOtherLesion = listOf(
            hasLiverMetastases, hasCnsMetastases, hasBrainMetastases, hasLungLesions, hasLymphNodeLesions, hasOtherLesion
        ).any { it == true }

        return if (hasBoneMetastases && !hasAnyOtherLesion) {
            EvaluationFactory.pass("Patient only has bone metastases", "Bone-only metastases")
        } else {
            EvaluationFactory.fail("Patient does not have bone metastases exclusively", "No bone-only metastases")
        }
    }
}