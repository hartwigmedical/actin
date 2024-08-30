package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasExtracranialMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasNonCnsMetastases = with(record.tumor) {
            hasBoneLesions == true || hasLungLesions == true || hasLiverLesions == true || hasLymphNodeLesions == true
        }
        val categorizedLesionsUnknown = with(record.tumor) {
            hasBoneLesions == null || hasLungLesions == null || hasLiverLesions == null || hasLymphNodeLesions == null
        }
        val uncategorizedLesions = record.tumor.otherLesions ?: emptyList()
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