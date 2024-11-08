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
                Triple("CNS", hasCnsLesions, hasSuspectedCnsLesions),
                Triple(
                    "Brain",
                    (primaryTumorLocation == "Brain" || primaryTumorType == "Glioma" || hasBrainLesions == true),
                    hasSuspectedBrainLesions
                ),
                Triple("Liver", hasLiverLesions, hasSuspectedLiverLesions),
                Triple("Bone", hasBoneLesions, hasSuspectedBoneLesions),
                Triple("Lung", hasLungLesions, hasSuspectedLungLesions),
                Triple("Lymph node", hasLymphNodeLesions, hasSuspectedLymphNodeLesions)
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

        val (lymphNodeLesions, nonLymphNodeLesions) = allLesions.partition { it.lowercase().startsWith("lymph node") }
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