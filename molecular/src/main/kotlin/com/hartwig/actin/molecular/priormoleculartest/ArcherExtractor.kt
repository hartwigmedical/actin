package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.MolecularExtractor
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherFusion
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanel
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherSkippedExons
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherVariant

private val FUSION_REGEX = Regex("([A-Za-z0-9 ]+)( fusie aangetoond)")
private val EXON_SKIP_REGEX = Regex("([A-Za-z0-9 ]+)( exon )([0-9]+(-[0-9]+)?)( skipping aangetoond)")
private const val NO_FUSIONS = "GEEN fusie(s) aangetoond"
private const val NO_MUTATION = "GEEN mutaties aangetoond"

private enum class ArcherVariantCategory {
    VARIANT,
    FUSION,
    EXON_SKIP,
    UNKNOWN
}

class ArcherExtractor : MolecularExtractor<PriorMolecularTest, ArcherPanel> {
    override fun extract(input: List<PriorMolecularTest>): List<ArcherPanel> {
        return input.groupBy { it.measureDate }
            .map { (date, results) ->
                val resultsWithMeasure = results.filter { it.measure != null }
                val groupedByCategory = resultsWithMeasure.groupBy {
                    when {
                        it.measure!!.startsWith("c.") -> ArcherVariantCategory.VARIANT
                        FUSION_REGEX.find(it.measure!!) != null -> ArcherVariantCategory.FUSION
                        EXON_SKIP_REGEX.find(it.measure!!) != null -> ArcherVariantCategory.EXON_SKIP
                        else -> ArcherVariantCategory.UNKNOWN
                    }
                }

                val variants =
                    groupedByCategory[ArcherVariantCategory.VARIANT]?.map { ArcherVariant(it.item!!, it.measure!!) } ?: emptyList()
                val fusions = groupedByCategory[ArcherVariantCategory.FUSION]?.mapNotNull {
                    FUSION_REGEX.find(it.measure!!)?.let { matchResult ->
                        ArcherFusion(matchResult.groupValues[1])
                    }
                } ?: emptyList()
                val exonSkips = groupedByCategory[ArcherVariantCategory.EXON_SKIP]?.mapNotNull {
                    EXON_SKIP_REGEX.find(it.measure!!)?.let { matchResult ->
                        val (start, end) = parseRange(matchResult.groupValues[3])
                        ArcherSkippedExons(matchResult.groupValues[1], start, end)
                    }
                } ?: emptyList()
                val unknownResults =
                    groupedByCategory[ArcherVariantCategory.UNKNOWN]?.filter { it.measure != NO_FUSIONS && it.measure != NO_MUTATION }
                if (!unknownResults.isNullOrEmpty()) {
                    throw IllegalArgumentException("Unknown results in Archer: ${unknownResults.map { "${it.item} ${it.measure}" }}")
                }
                ArcherPanel(variants, fusions, exonSkips, date)
            }
    }

    private fun parseRange(range: String): Pair<Int, Int> {
        val parts = range.split("-")
        return parts[0].toInt() to parts[parts.size - 1].toInt()
    }
}