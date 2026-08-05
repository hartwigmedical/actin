package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasExtracranialMetastases(private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasNonCnsMetastases = with(record.tumor) {
            listOf(hasLiverLesions, hasBoneLesions, hasLungLesions, hasLymphNodeLesions).any { it == true }
        }
        val hasSuspectedNonCnsMetastases = with(record.tumor) {
            hasSuspectedLesions() && listOf(hasSuspectedCnsLesions, hasSuspectedBrainLesions).none { it == true }
        }
        val anyCategorizedLesionUnknown =
            with(record.tumor) { listOf(hasBoneLesions, hasLungLesions, hasLiverLesions, hasLymphNodeLesions) }.any { it == null }
        val uncategorizedLesions = record.tumor.otherLesions ?: emptyList()
        val uncategorizedSuspectedLesions = record.tumor.otherSuspectedLesions ?: emptyList()

        return when {
            hasNonCnsMetastases || uncategorizedLesions.any(::isExtraCranialLesion) -> {
                EvaluationFactory.pass(labels.hasExtracranialMetastasesPass())
            }

            hasSuspectedNonCnsMetastases || uncategorizedSuspectedLesions.any(::isExtraCranialLesion) -> {
                EvaluationFactory.warn(labels.hasExtracranialMetastasesWarn())
            }

            uncategorizedLesions.isNotEmpty() || anyCategorizedLesionUnknown -> {
                EvaluationFactory.undetermined(labels.hasExtracranialMetastasesUndetermined())
            }

            else -> {
                EvaluationFactory.fail(labels.hasExtracranialMetastasesFail())
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