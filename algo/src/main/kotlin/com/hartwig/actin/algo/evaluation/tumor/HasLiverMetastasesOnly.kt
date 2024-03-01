package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasLiverMetastasesOnly : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasLiverMetastases = record.clinical.tumor.hasLiverLesions ?: return EvaluationFactory.undetermined(
            "Data regarding presence of liver metastases is missing", "Missing liver metastasis data"
        )
        val hasBoneMetastases = record.clinical.tumor.hasBoneLesions
        val hasCnsMetastases = record.clinical.tumor.hasCnsLesions
        val hasBrainMetastases = record.clinical.tumor.hasBrainLesions
        val hasLungLesions = record.clinical.tumor.hasLungLesions
        val hasLymphNodeLesions = record.clinical.tumor.hasLymphNodeLesions
        val otherLesions = record.clinical.tumor.otherLesions
        if (hasLiverMetastases && listOf(
                hasBoneMetastases,
                hasCnsMetastases,
                hasBrainMetastases,
                hasLungLesions,
                hasLymphNodeLesions,
                otherLesions
            ).all { it == null }
        ) {
            return EvaluationFactory.warn(
                "Patient has liver lesions but data regarding other lesion locations is missing, so unknown if patient has only liver metastases",
                "Lesion location data is missing: unknown if liver metastases only"
            )
        }
        val hasOtherLesion = !otherLesions.isNullOrEmpty()
        val hasAnyOtherLesion = listOf(
            hasBoneMetastases, hasCnsMetastases, hasBrainMetastases, hasLungLesions, hasLymphNodeLesions, hasOtherLesion
        ).any { it == true }

        return if (hasLiverMetastases && !hasAnyOtherLesion) {
            EvaluationFactory.pass("Patient only has liver metastases", "Liver-only metastases")
        } else {
            EvaluationFactory.fail("Patient does not have liver metastases exclusively", "No liver-only metastases")
        }
    }
}