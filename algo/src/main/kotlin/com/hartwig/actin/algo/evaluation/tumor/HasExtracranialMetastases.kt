package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.complication.PatternMatcher

class HasExtracranialMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasNonCnsMetastases = with(record.tumor) {
            hasBoneLesions == true || hasLungLesions == true || hasLiverLesions == true || hasLymphNodeLesions == true
        }
        val categorizedLesionsUnknown = with(record.tumor) {
            hasBoneLesions == null || hasLungLesions == null || hasLiverLesions == null || hasLymphNodeLesions == null
        }
        val uncategorizedLesions = record.tumor.otherLesions ?: emptyList()
        val biopsyLocation = record.tumor.biopsyLocation ?: ""

        return when {
            hasNonCnsMetastases || isExtraCranialLesion(biopsyLocation) || uncategorizedLesions.any(::isExtraCranialLesion) -> {
                EvaluationFactory.pass("Patient has extracranial metastases", "Extracranial metastases present")
            }

            uncategorizedLesions.isNotEmpty() || record.tumor.hasCnsLesions == true || categorizedLesionsUnknown -> {
                val message = "Undetermined if extracranial metastases present"
                EvaluationFactory.undetermined(message, message)
            }

            else -> {
                EvaluationFactory.fail("Patient does not have extracranial metastases", "No extracranial metastases")
            }
        }
    }

    companion object {
        private val EXTRACRANIAL_LESION_PATTERNS = setOf(
            listOf("bone", "marrow"),
            listOf("peritoneum"),
            listOf("peritoneal"),
            listOf("adrenal"),
            listOf("kidney"),
            listOf("pancreas"),
            listOf("pancreatic"),
            listOf("skin"),
            listOf("ovary"),
            listOf("spleen"),
            listOf("intestine"),
            listOf("colon"),
            listOf("thyroid"),
            listOf("salivary"),
            listOf("bladder"),
            listOf("breast"),
            listOf("esophagus"),
            listOf("stomach")
        )

        private fun isExtraCranialLesion(lesion: String): Boolean {
            return PatternMatcher.isMatch(lesion, EXTRACRANIAL_LESION_PATTERNS)
        }
    }
}