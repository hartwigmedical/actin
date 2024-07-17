package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.MolecularExtractor
import com.hartwig.actin.molecular.datamodel.panel.PanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelVariantExtraction
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericExonDeletionExtraction
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericFusionExtraction
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanelExtraction

class GenericPanelExtractor : MolecularExtractor<PriorMolecularTest, PanelExtraction> {

    override fun extract(input: List<PriorMolecularTest>): List<PanelExtraction> {
        return input.groupBy { it.test }
            .flatMap { (test, results) -> groupedByTestDate(results, test) }
    }

    private fun groupedByTestDate(results: List<PriorMolecularTest>, type: String): List<GenericPanelExtraction> {
        return results
            .groupBy { it.measureDate }
            .map { (date, results) ->
                val usableResults = results.filterNot { result -> isKnownIgnorableRecord(result) }
                val (fusionRecords, nonFusionRecords) = usableResults.partition { it.item?.contains("::") ?: false }
                val fusions = fusionRecords.mapNotNull { it.item?.let { item -> GenericFusionExtraction.parseFusion(item) } }

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
                    throw IllegalArgumentException("Unrecognized records in $type panel: ${
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

    private fun isKnownIgnorableRecord(result: PriorMolecularTest): Boolean {
        return result.measure == "GEEN mutaties aangetoond met behulp van het AVL Panel"

    }

    private fun parseVariant(priorMolecularTest: PriorMolecularTest): PanelVariantExtraction {
        return if (priorMolecularTest.item != null && priorMolecularTest.measure != null) {
            PanelVariantExtraction(gene = priorMolecularTest.item!!, hgvsImpact = priorMolecularTest.measure!!)
        } else {
            throw IllegalArgumentException(
                "Expected item and measure for variant but got ${priorMolecularTest.item} and ${priorMolecularTest.measure}"
            )
        }
    }
}