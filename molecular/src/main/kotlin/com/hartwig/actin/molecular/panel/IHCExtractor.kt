package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.PriorIHCTest
import com.hartwig.actin.molecular.MolecularExtractor

private val IHC_FUSION_GENES = setOf("ALK", "ROS1")

class IHCExtractor : MolecularExtractor<PriorIHCTest, IHCExtraction> {
    override fun extract(input: List<PriorIHCTest>): List<IHCExtraction> {
        return input.groupBy { it.measureDate }
            .map { (date, tests) ->
                IHCExtraction(
                    date,
                    IHCFusionGenes(tests, "Positive"),
                    IHCFusionGenes(tests, "Negative")
                )
            }.filter { it.fusionPositiveGenes.isNotEmpty() || it.fusionNegativeGenes.isNotEmpty() }
    }

    private fun IHCFusionGenes(priorIhcTests: List<PriorIHCTest>, scoreText: String): Set<String> {
        return priorIhcTests.filter { it.test == "IHC" && it.item in IHC_FUSION_GENES && it.scoreText == scoreText }
            .mapNotNull { it.item }
            .toSet()
    }
}