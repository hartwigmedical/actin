package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.MolecularExtractor
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherFusion
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherSkippedExons
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherSmallVariant

private val FUSION_REGEX = Regex("([A-Za-z0-9 ]+)( fusie aangetoond)")
private val EXON_SKIP_REGEX = Regex("([A-Za-z0-9 ]+)( exon )([0-9]+(-[0-9]+)?)( skipping aangetoond)")
private const val NO_FUSIONS = "GEEN fusie(s) aangetoond"
private const val NO_MUTATION = "GEEN mutaties aangetoond"

private enum class ArcherMutationCategory {
    SMALL_VARIANT,
    FUSION,
    EXON_SKIP,
    UNKNOWN
}

class ArcherExtractor : MolecularExtractor<PriorMolecularTest, ArcherPanelExtraction> {
    override fun extract(input: List<PriorMolecularTest>): List<ArcherPanelExtraction> {
        return input.groupBy { it.measureDate }
            .map { (date, results) ->
                val resultsWithMeasure = results.filter { it.measure != null }
                val groupedByCategory = resultsWithMeasure.groupBy {
                    when {
                        it.measure!!.startsWith("c.") -> ArcherMutationCategory.SMALL_VARIANT
                        FUSION_REGEX.find(it.measure!!) != null -> ArcherMutationCategory.FUSION
                        EXON_SKIP_REGEX.find(it.measure!!) != null -> ArcherMutationCategory.EXON_SKIP
                        else -> ArcherMutationCategory.UNKNOWN
                    }
                }

                val variants =
                    groupedByCategory[ArcherMutationCategory.SMALL_VARIANT]?.map { ArcherSmallVariant(it.item!!, it.measure!!) } ?: emptyList()
                val fusions = groupedByCategory[ArcherMutationCategory.FUSION]?.mapNotNull {
                    FUSION_REGEX.find(it.measure!!)?.let { matchResult ->
                        ArcherFusion(matchResult.groupValues[1])
                    }
                } ?: emptyList()
                val exonSkips = groupedByCategory[ArcherMutationCategory.EXON_SKIP]?.mapNotNull {
                    EXON_SKIP_REGEX.find(it.measure!!)?.let { matchResult ->
                        val (start, end) = parseRange(matchResult.groupValues[3])
                        ArcherSkippedExons(matchResult.groupValues[1], start, end)
                    }
                } ?: emptyList()
                val unknownResults =
                    groupedByCategory[ArcherMutationCategory.UNKNOWN]?.filter { it.measure != NO_FUSIONS && it.measure != NO_MUTATION }
                if (!unknownResults.isNullOrEmpty()) {
                    throw IllegalArgumentException("Unknown results in Archer: ${unknownResults.map { "${it.item} ${it.measure}" }}")
                }
                ArcherPanelExtraction(variants, fusions, exonSkips, date)
            }
    }

    private fun parseRange(range: String): Pair<Int, Int> {
        val parts = range.split("-")
        return parts[0].toInt() to parts[parts.size - 1].toInt()
    }
}