package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.molecular.MolecularExtractor

private val IHC_FUSION_GENES = setOf("ALK", "ROS1")

class IhcExtractor : MolecularExtractor<IhcTest, IhcExtraction> {

    override fun extract(input: List<IhcTest>): List<IhcExtraction> {
        return input.groupBy { it.measureDate }
            .map { (date, tests) ->
                IhcExtraction(
                    date,
                    ihcFusionGenes(tests, "Positive"),
                    ihcFusionGenes(tests, "Negative")
                )
            }.filter { it.fusionPositiveGenes.isNotEmpty() || it.fusionNegativeGenes.isNotEmpty() }
    }

    private fun ihcFusionGenes(ihcTests: List<IhcTest>, scoreText: String): Set<String> {
        return ihcTests.filter { it.item in IHC_FUSION_GENES && it.scoreText == scoreText }
            .mapNotNull { it.item }
            .toSet()
    }
}