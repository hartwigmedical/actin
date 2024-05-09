package com.hartwig.actin.molecular.clinical

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.MolecularInterpreter
import com.hartwig.actin.molecular.datamodel.AVL_PANEL
import com.hartwig.actin.molecular.datamodel.FREE_TEXT_PANEL
import com.hartwig.actin.molecular.datamodel.GenericPanelMolecularTest
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericFusion
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanel
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanelType
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericVariant

class GenericPanelInterpreter : MolecularInterpreter<PriorMolecularTest, GenericPanel> {
    override fun interpret(input: List<PriorMolecularTest>): List<MolecularTest<GenericPanel>> {
        return input.groupBy { it.test }
            .flatMap { (test, results) -> groupedByTestDate(results, classify(test)) }
    }

    private fun groupedByTestDate(results: List<PriorMolecularTest>, type: GenericPanelType): List<GenericPanelMolecularTest> {
        return results
            .groupBy { it.measureDate }
            .map { (date, results) ->
                val usableResults = results.filterNot { result -> isKnownIgnorableRecord(result, type) }
                val (fusionRecords, variantRecords) = usableResults.partition { it.item?.contains("::") ?: false }
                val fusions = fusionRecords.mapNotNull { it.item?.let { item -> GenericFusion.parseFusion(item) } }
                val variants = variantRecords.map { record -> GenericVariant.parseVariant(record) }

                GenericPanelMolecularTest(date = date, result = GenericPanel(type, variants, fusions))
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