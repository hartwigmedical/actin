package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.report.pdf.util.Formats

object TumorDetailsInterpreter {
    const val CUP_LOCATION = "Unknown"
    const val CUP_SUB_LOCATION = "CUP"

    fun isCUP(tumor: TumorDetails): Boolean {
        val location = tumor.primaryTumorLocation
        val subLocation = tumor.primaryTumorSubLocation
        return location != null && subLocation != null && location == CUP_LOCATION && subLocation == CUP_SUB_LOCATION
    }

    fun lesions(tumor: TumorDetails): String {
        val allCategorizedLesions = with(tumor) {
            listOf(
                Triple(TumorDetails.CNS, hasCnsLesions, hasSuspectedCnsLesions),
                Triple(
                    TumorDetails.BRAIN,
                    (primaryTumorLocation == "Brain" || primaryTumorType == "Glioma" || hasBrainLesions == true),
                    hasSuspectedBrainLesions
                ),
                Triple(TumorDetails.LIVER, hasLiverLesions, hasSuspectedLiverLesions),
                Triple(TumorDetails.BONE, hasBoneLesions, hasSuspectedBoneLesions),
                Triple(TumorDetails.LUNG, hasLungLesions, hasSuspectedLungLesions),
                Triple(TumorDetails.LYMPH_NODE, hasLymphNodeLesions, hasSuspectedLymphNodeLesions)
            )
        }

        val confirmedCategorizedLesions = allCategorizedLesions
            .filter { it.second == true }
            .map {
                when {
                    (it.first == TumorDetails.CNS && tumor.hasActiveCnsLesions == true) ||
                            (it.first == TumorDetails.BRAIN && tumor.hasActiveBrainLesions == true) -> {
                                "${it.first} (active)"
                    }
                    else -> it.first
                }
            }

        val suspectedCategorizedLesions = allCategorizedLesions
            .filter { it.second != true && it.third == true }
            .map { "${it.first} (suspected)" }

        val confirmedOtherLesions = tumor.otherLesions.orEmpty()
        val suspectedOtherLesions = tumor.otherSuspectedLesions
            ?.filterNot { it in confirmedOtherLesions }
            ?.map { "$it (suspected)" }
            .orEmpty()

        val allLesions = (confirmedCategorizedLesions + confirmedOtherLesions + listOfNotNull(tumor.biopsyLocation))
            .sorted()
            .distinctBy(String::uppercase)

        val (lymphNodeLesions, nonLymphNodeLesions) = allLesions.partition {
            it.lowercase().startsWith(TumorDetails.LYMPH_NODE.lowercase())
        }
        val filteredLymphNodeLesions = lymphNodeLesions.map { lesion ->
            lesion.split(" ").filterNot { it.lowercase() in setOf("lymph", "node", "nodes", "") }
                .joinToString(" ")
        }.filterNot(String::isEmpty).distinctBy(String::uppercase)

        val lymphNodeLesionsString = if (filteredLymphNodeLesions.isNotEmpty()) {
            listOf("Lymph nodes (${filteredLymphNodeLesions.joinToString(", ")})")
        } else if (lymphNodeLesions.isNotEmpty()) {
            listOf("Lymph nodes")
        } else emptyList()

        return (nonLymphNodeLesions + lymphNodeLesionsString + suspectedCategorizedLesions + suspectedOtherLesions)
            .joinToString(", ")
            .ifEmpty { Formats.VALUE_UNKNOWN }
    }
}