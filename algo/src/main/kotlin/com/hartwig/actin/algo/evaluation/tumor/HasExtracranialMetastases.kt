package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasExtracranialMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasNonCnsMetastases = with(record.tumor) {
            hasConfirmedOrSuspectedBoneLesions() == true || hasConfirmedOrSuspectedLungLesions() == true || hasConfirmedOrSuspectedLiverLesions() == true || hasConfirmedOrSuspectedLymphNodeLesions() == true
        }
        val categorizedLesionsUnknown = with(record.tumor) {
            hasConfirmedOrSuspectedBoneLesions() == null || hasConfirmedOrSuspectedLungLesions() == null || hasConfirmedOrSuspectedLiverLesions() == null || hasConfirmedOrSuspectedLymphNodeLesions() == null
        }
        val uncategorizedLesions = record.tumor.otherConfirmedOrSuspectedLesions() ?: emptyList()
        val biopsyLocation = record.tumor.biopsyLocation

        return when {
            hasNonCnsMetastases || biopsyLocation?.let { isExtraCranialLesion(it) } == true || uncategorizedLesions.any(::isExtraCranialLesion) -> {
                EvaluationFactory.pass("Patient has extracranial metastases", "Extracranial metastases present")
            }

            uncategorizedLesions.isNotEmpty() || categorizedLesionsUnknown || biopsyLocation == null -> {
                val message = "Undetermined if extracranial metastases present"
                EvaluationFactory.undetermined(message, message)
            }

            else -> {
                EvaluationFactory.fail("Patient does not have extracranial metastases", "No extracranial metastases")
            }
        }
    }

    companion object {
        private val EXTRACRANIAL_LESION_TERMS = listOf(
            "marrow",
            "peritoneum",
            "peritoneal",
            "adrenal",
            "kidney",
            "pancreas",
            "pancreatic",
            "skin",
            "ovary",
            "spleen",
            "spine",
            "spinal",
            "intestine",
            "colon",
            "thyroid",
            "salivary",
            "bladder",
            "breast",
            "esophagus",
            "stomach"
        )

        private fun isExtraCranialLesion(lesion: String): Boolean {
            return stringCaseInsensitivelyMatchesQueryCollection(lesion, EXTRACRANIAL_LESION_TERMS)
        }
    }
}