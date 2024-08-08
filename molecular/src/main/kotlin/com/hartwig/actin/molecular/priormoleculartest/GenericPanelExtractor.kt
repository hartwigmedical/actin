package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.clinical.datamodel.PriorIHCTest
import com.hartwig.actin.molecular.MolecularExtractor
import com.hartwig.actin.molecular.datamodel.panel.PanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelFusionExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelVariantExtraction
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericExonDeletionExtraction
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanelExtraction
import org.apache.logging.log4j.LogManager

class GenericPanelExtractor : MolecularExtractor<PriorIHCTest, PanelExtraction> {

    private val logger = LogManager.getLogger(GenericPanelExtractor::class.java)

    override fun extract(input: List<PriorIHCTest>): List<PanelExtraction> {
        return input.groupBy { it.test }
            .flatMap { (test, results) -> groupedByTestDate(results, test) }
    }

    private fun groupedByTestDate(results: List<PriorIHCTest>, type: String): List<GenericPanelExtraction> {
        return results
            .groupBy { it.measureDate }
            .map { (date, results) ->
                val usableResults = results.filterNot { result -> isKnownIgnorableRecord(result) }
                val (fusionRecords, nonFusionRecords) = usableResults.partition { it.item?.contains("::") ?: false }
                val fusions = fusionRecords.mapNotNull { it.item?.let { item -> PanelFusionExtraction.parseFusion(item) } }

                val (exonDeletionRecords, nonExonDeletionRecords) = nonFusionRecords.partition { it.measure?.endsWith(" del") ?: false }
                val exonDeletions = exonDeletionRecords.map { record -> GenericExonDeletionExtraction.parse(record) }

                val (variantRecords, nonVariantRecordsGene) = nonExonDeletionRecords.partition {
                    it.measure?.let { measure -> measure.startsWith("c.") || measure.startsWith("p.") } ?: false
                }
                val variants = variantRecords.map { record -> parseVariant(record) }

                val (geneWithNegativeResultsRecords, unknownRecords) =
                    nonVariantRecordsGene.partition { it.scoreText?.lowercase() == "negative" }
                val geneWithNegativeResults = geneWithNegativeResultsRecords.mapNotNull { it.item }.toSet()

                if (unknownRecords.isNotEmpty()) {
                    logger.error("Unrecognized records in $type panel: ${
                        nonVariantRecordsGene.joinToString(", ") { "item \"${it.item}\" measure \"${it.measure}\"" }
                    }")
                }

                GenericPanelExtraction(
                    panelType = type,
                    fusions = fusions,
                    exonDeletions = exonDeletions,
                    genesWithNegativeResults = geneWithNegativeResults,
                    variants = variants,
                    date = date
                )
            }
    }

    private fun isKnownIgnorableRecord(result: PriorIHCTest): Boolean {
        return result.measure?.trim()?.startsWith("GEEN") ?: false

    }

    private fun parseVariant(priorMolecularTest: PriorIHCTest): PanelVariantExtraction {
        return if (priorMolecularTest.item != null && priorMolecularTest.measure != null) {
            PanelVariantExtraction(gene = priorMolecularTest.item!!, hgvsCodingOrProteinImpact = priorMolecularTest.measure!!)
        } else {
            throw IllegalArgumentException(
                "Expected item and measure for variant but got ${priorMolecularTest.item} and ${priorMolecularTest.measure}"
            )
        }
    }
}