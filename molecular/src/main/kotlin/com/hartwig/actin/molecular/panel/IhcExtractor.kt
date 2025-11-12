package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.molecular.MolecularExtractor

private val IHC_FUSION_GENES = setOf("ALK", "ROS1")
private val IHC_FUSION_GENES_IF_POSITIVE = setOf("NTRK1", "NTRK2", "NTRK3")

class IhcExtractor : MolecularExtractor<IhcTest, IhcExtraction> {

    override fun extract(input: List<IhcTest>): List<IhcExtraction> {
        return input.groupBy { it.measureDate }
            .map { (date, tests) ->
                IhcExtraction(
                    date,
                    ihcFusionGenes(tests, IHC_FUSION_GENES + IHC_FUSION_GENES_IF_POSITIVE, "Positive"),
                    ihcFusionGenes(tests, IHC_FUSION_GENES, "Negative")
                )
            }
            .filter { it.fusionPositiveGenes.isNotEmpty() || it.fusionNegativeGenes.isNotEmpty() }
    }

    private fun ihcFusionGenes(ihcTests: List<IhcTest>, genes: Set<String>, scoreText: String): Set<String> {
        return ihcTests.filter { it.item in genes && it.scoreText == scoreText }
            .map { it.item }
            .toSet()
    }
}