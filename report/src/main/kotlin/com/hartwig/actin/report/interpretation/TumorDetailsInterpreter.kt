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
        val categorizedLesions = listOf(
            "CNS" to tumor.hasCnsLesions,
            "Brain" to (tumor.primaryTumorLocation == "Brain" || tumor.primaryTumorType == "Glioma" || tumor.hasBrainLesions == true),
            "Liver" to tumor.hasLiverLesions,
            "Bone" to tumor.hasBoneLesions,
            "Lung" to tumor.hasLungLesions,
            "Lymph node" to tumor.hasLymphNodeLesions
        ).filter { it.second == true }.map { it.first }

        val suspectedCategorizedLesions = listOf(
            "CNS" to tumor.hasSuspectedCnsLesions,
            "Brain" to tumor.hasSuspectedBrainLesions,
            "Liver" to tumor.hasSuspectedLiverLesions,
            "Bone" to tumor.hasSuspectedBoneLesions,
            "Lung" to tumor.hasSuspectedLungLesions,
            "Lymph node" to tumor.hasSuspectedLymphNodeLesions
        ).filter { it.second == true }.map { it.first }
            .filterNot { it in categorizedLesions }
            .map { "$it (suspected)" }

        val otherLesions = tumor.otherLesions
        val suspectedOtherLesions =
            tumor.otherSuspectedLesions?.filterNot { otherLesions?.contains(it) == true }?.map { "$it (suspected)" }

        val lesions =
            listOfNotNull(categorizedLesions, otherLesions, listOfNotNull(tumor.biopsyLocation)).flatten()
                .sorted().distinctBy(String::uppercase)
        val suspectedLesions = (suspectedCategorizedLesions + suspectedOtherLesions.orEmpty())
        val (lymphNodeLesions, nonLymphNodeLesions) = lesions.partition { it.lowercase().startsWith("lymph node") }

        val filteredLymphNodeLesions = lymphNodeLesions.map { lesion ->
            lesion.split(" ").filterNot { it.lowercase() in setOf("lymph", "node", "nodes", "") }.joinToString(" ")
        }.filterNot(String::isEmpty).distinctBy(String::uppercase)

        val lymphNodeLesionsString = if (filteredLymphNodeLesions.isNotEmpty()) {
            listOf("Lymph nodes (${filteredLymphNodeLesions.joinToString(", ")})")
        } else if (lymphNodeLesions.isNotEmpty()) {
            listOf("Lymph nodes")
        } else emptyList()

        return (nonLymphNodeLesions + lymphNodeLesionsString + suspectedLesions).joinToString(", ").ifEmpty { Formats.VALUE_UNKNOWN }
    }
}