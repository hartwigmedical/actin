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

class ArcherExtractor : MolecularExtractor<PriorMolecularTest, ArcherPanel> {
    override fun extract(input: List<PriorMolecularTest>): List<ArcherPanel> {
        return input.groupBy { it.measureDate }
            .map { (date, results) ->
                val resultsWithItemAndMeasure = results.filter { it.item != null && it.measure != null }
                val variants = resultsWithItemAndMeasure
                    .filter { it.measure!!.startsWith("c.") }
                    .map {
                        ArcherVariant(it.item!!, it.measure!!) to it
                    }
                val fusions = resultsWithItemAndMeasure
                    .mapNotNull {
                        FUSION_REGEX.find(it.measure!!)?.let { matchResult -> ArcherFusion(matchResult.groupValues[1]) to it }
                    }
                val exonSkips = resultsWithItemAndMeasure
                    .mapNotNull {
                        EXON_SKIP_REGEX.find(it.measure!!)?.let { matchResult ->
                            val (start, end) = parseRange(matchResult.groupValues[3])
                            ArcherSkippedExons(matchResult.groupValues[1], start, end) to it
                        }
                    }
                checkForUnknownResults(results, variants, fusions, exonSkips)
                ArcherPanel(variants.map { it.first }, fusions.map { it.first }, exonSkips.map { it.first }, date)
            }
    }

    private fun checkForUnknownResults(
        results: List<PriorMolecularTest>,
        variants: List<Pair<ArcherVariant, PriorMolecularTest>>,
        fusions: List<Pair<ArcherFusion, PriorMolecularTest>>,
        exonSkips: List<Pair<ArcherSkippedExons, PriorMolecularTest>>
    ) {
        val relevantResults = results.filter { it.measure != NO_FUSIONS && it.measure != NO_MUTATION }.toSet()
        val processedResults = (variants + fusions + exonSkips).map { it.second }.toSet()
        val unknownResults = relevantResults - processedResults
        if (unknownResults.isNotEmpty()) {
            throw IllegalArgumentException("Unknown results in Archer: ${unknownResults.map { "${it.item} ${it.measure}" }}")
        }
    }

    private fun parseRange(range: String): Pair<Int, Int> {
        val parts = range.split("-")
        return parts[0].toInt() to parts[parts.size - 1].toInt()
    }
}