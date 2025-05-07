package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.IHCTest
import com.hartwig.actin.molecular.MolecularExtractor

private val IHC_FUSION_GENES = setOf("ALK", "ROS1")

class IHCExtractor : MolecularExtractor<IHCTest, IHCExtraction> {

    override fun extract(input: List<IHCTest>): List<IHCExtraction> {
        return input.groupBy { it.measureDate }
            .map { (date, tests) ->
                IHCExtraction(
                    date,
                    ihcFusionGenes(tests, "Positive"),
                    ihcFusionGenes(tests, "Negative")
                )
            }.filter { it.fusionPositiveGenes.isNotEmpty() || it.fusionNegativeGenes.isNotEmpty() }
    }

    private fun ihcFusionGenes(ihcTests: List<IHCTest>, scoreText: String): Set<String> {
        return ihcTests.filter { it.item in IHC_FUSION_GENES && it.scoreText == scoreText }
            .mapNotNull { it.item }
            .toSet()
    }
}