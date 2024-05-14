package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.MolecularExtractor
import com.hartwig.actin.molecular.datamodel.AVL_PANEL
import com.hartwig.actin.molecular.datamodel.FREE_TEXT_PANEL
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericFusion
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanel
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanelType
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericVariant

class GenericPanelExtractor : MolecularExtractor<PriorMolecularTest, GenericPanel> {
    override fun extract(input: List<PriorMolecularTest>): List<GenericPanel> {
        return input.groupBy { it.test }
            .flatMap { (test, results) -> groupedByTestDate(results, classify(test)) }
    }

    private fun groupedByTestDate(results: List<PriorMolecularTest>, type: GenericPanelType): List<GenericPanel> {
        return results
            .groupBy { it.measureDate }
            .map { (date, results) ->
                val usableResults = results.filterNot { result -> isKnownIgnorableRecord(result, type) }
                val (fusionRecords, variantRecords) = usableResults.partition { it.item?.contains("::") ?: false }
                val fusions = fusionRecords.mapNotNull { it.item?.let(GenericFusion::parseFusion) }
                val variants = variantRecords.map(GenericVariant::parseVariant)

                GenericPanel(type, variants, fusions, date)
            }
    }

    private fun isKnownIgnorableRecord(result: PriorMolecularTest, type: GenericPanelType): Boolean {
        return when (type) {
            GenericPanelType.AVL -> result.measure == "GEEN mutaties aangetoond met behulp van het AVL Panel"
            else -> false
        }
    }

    private fun classify(type: String?): GenericPanelType {
        return when (type) {
            AVL_PANEL -> GenericPanelType.AVL
            FREE_TEXT_PANEL -> GenericPanelType.FREE_TEXT
            else -> throw IllegalArgumentException("Unknown generic panel type: $type")
        }
    }
}