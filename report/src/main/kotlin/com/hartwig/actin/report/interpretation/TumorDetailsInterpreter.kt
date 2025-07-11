package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.report.pdf.util.Formats

object TumorDetailsInterpreter {

    const val CUP_STRING = "(CUP)"

    data class Lesions(
        val nonLymphNodeLesions: List<String>,
        val lymphNodeLesions: List<String>,
        val suspectedCategorizedLesions: List<String>,
        val suspectedOtherLesions: List<String>,
        val negativeCategories: List<String>,
        val unknownLesions: List<String>
    )

    private data class Lesion(val type: String, val hasLesion: Boolean?, val hasSuspectedLesion: Boolean?)

    fun hasCancerOfUnknownPrimary(name: String): Boolean {
        return name.contains(CUP_STRING)
    }

    fun lesionString(tumor: TumorDetails): String {
        return with(classifyLesions(tumor)) {
            (nonLymphNodeLesions + lymphNodeLesions + suspectedCategorizedLesions + suspectedOtherLesions)
                .joinToString(", ")
                .ifEmpty { Formats.VALUE_UNKNOWN }
        }
    }

    fun classifyLesions(tumor: TumorDetails): Lesions {
        val allCategorizedLesions = with(tumor) {
            listOf(
                Lesion(
                    TumorDetails.CNS + if (hasCnsLesions == true && hasActiveCnsLesions == true) " (active)" else "",
                    hasCnsLesions,
                    hasSuspectedCnsLesions
                ),
                Lesion(
                    TumorDetails.BRAIN + if (hasBrainLesions == true && hasActiveBrainLesions == true) " (active)" else "",
                    when {
                        hasBrainLesions == true -> true
                        name.contains("brain", ignoreCase = true) || name.contains("glioma", ignoreCase = true) -> true
                        hasBrainLesions == null -> null
                        else -> false
                    },
                    hasSuspectedBrainLesions
                ),
                Lesion(TumorDetails.LIVER, hasLiverLesions, hasSuspectedLiverLesions),
                Lesion(TumorDetails.BONE, hasBoneLesions, hasSuspectedBoneLesions),
                Lesion(TumorDetails.LUNG, hasLungLesions, hasSuspectedLungLesions),
                Lesion(TumorDetails.LYMPH_NODE, hasLymphNodeLesions, hasSuspectedLymphNodeLesions)
            )
        }

        val confirmedCategorizedLesions = allCategorizedLesions.filter { it.hasLesion == true }.map { it.type }
        val suspectedCategorizedLesions =
            allCategorizedLesions.filter { it.hasLesion != true && it.hasSuspectedLesion == true }.map { "${it.type} (suspected)" }
        val negativeCategorizedLesions =
            allCategorizedLesions.filter { it.hasLesion == false && it.hasSuspectedLesion != true }.map { it.type }
        val confirmedOtherLesions = tumor.otherLesions.orEmpty()
        val suspectedOtherLesions =
            tumor.otherSuspectedLesions?.filterNot { it in confirmedOtherLesions }?.map { "$it (suspected)" }.orEmpty()
        val unknownCategories = allCategorizedLesions.filter { it.hasLesion == null && it.hasSuspectedLesion != true }.map { it.type }
        val unknownLesions = if (tumor.otherLesions != null) unknownCategories else unknownCategories + "Other"

        val allLesions = (confirmedCategorizedLesions + confirmedOtherLesions + listOfNotNull(tumor.biopsyLocation))
            .sorted()
            .distinctBy(String::uppercase)

        val (lymphNodeLesions, nonLymphNodeLesions) = allLesions.partition {
            it.lowercase().startsWith(TumorDetails.LYMPH_NODE.lowercase())
        }

        return Lesions(
            nonLymphNodeLesions,
            lymphNodeLesionsString(lymphNodeLesions),
            suspectedCategorizedLesions,
            suspectedOtherLesions,
            negativeCategorizedLesions,
            unknownLesions
        )
    }

    private fun lymphNodeLesionsString(lymphNodeLesions: List<String>): List<String> {
        val filteredLymphNodeLesions = lymphNodeLesions.map { lesion ->
            lesion.split(" ").filterNot { it.lowercase() in setOf("lymph", "node", "nodes", "") }
                .joinToString(" ")
        }.filterNot(String::isEmpty).distinctBy(String::uppercase)

        return if (filteredLymphNodeLesions.isNotEmpty()) {
            listOf("Lymph nodes (${filteredLymphNodeLesions.joinToString(", ")})")
        } else if (lymphNodeLesions.isNotEmpty()) {
            listOf("Lymph nodes")
        } else emptyList()
    }
}