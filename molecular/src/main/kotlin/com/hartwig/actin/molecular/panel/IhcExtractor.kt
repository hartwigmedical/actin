package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.molecular.MolecularExtractor

private val IHC_FUSION_GENES = setOf("ALK", "ROS1")
private val IHC_DELETION_AND_MUTATION_TESTED_GENES = setOf("MLH1", "MSH2", "MSH6", "PMS2", "MTAP")

class IhcExtractor : MolecularExtractor<IhcTest, IhcExtraction> {

    override fun extract(input: List<IhcTest>): List<IhcExtraction> {
        return input.groupBy { it.measureDate }
            .map { (date, tests) ->
                IhcExtraction(
                    date,
                    ihcFusionGenes(tests, "positive"),
                    ihcFusionGenes(tests, "negative"),
                    ihcDeletionAndMutationTestedGenes(tests)
                )
            }
            .filter {
                it.fusionPositiveGenes.isNotEmpty() || it.fusionNegativeGenes.isNotEmpty() ||
                        it.mutationAndDeletionTestedGenes.isNotEmpty()
            }
    }

    private fun ihcFusionGenes(ihcTests: List<IhcTest>, scoreText: String): Set<String> {
        return ihcTests.filter { it.item in IHC_FUSION_GENES && it.scoreText?.lowercase() == scoreText && !it.impliesPotentialIndeterminateStatus }
            .map { it.item }
            .toSet()
    }

    private fun ihcDeletionAndMutationTestedGenes(ihcTests: List<IhcTest>): Set<String> {
        return ihcTests.filter { it.item in IHC_DELETION_AND_MUTATION_TESTED_GENES && !it.impliesPotentialIndeterminateStatus }
            .map { it.item }
            .toSet()
    }
}