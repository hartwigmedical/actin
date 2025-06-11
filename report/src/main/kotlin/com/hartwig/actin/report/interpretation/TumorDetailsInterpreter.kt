package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.report.pdf.util.Formats

object TumorDetailsInterpreter {

    const val CUP_STRING = "(CUP)"

    fun hasCancerOfUnknownPrimary(name: String): Boolean {
        return name.contains(CUP_STRING)
    }

    fun lesions(tumor: TumorDetails): String {
        val allCategorizedLesions = with(tumor) {
            listOf(
                Triple(
                    TumorDetails.CNS + if (hasCnsLesions == true && hasActiveCnsLesions == true) " (active)" else "",
                    hasCnsLesions,
                    hasSuspectedCnsLesions
                ),
                Triple(
                    TumorDetails.BRAIN + if (hasBrainLesions == true && hasActiveBrainLesions == true) " (active)" else "",
                    (name.lowercase().contains("brain") || (name.lowercase().contains("glioma") || hasBrainLesions == true)),
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
            .map { it.first }

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