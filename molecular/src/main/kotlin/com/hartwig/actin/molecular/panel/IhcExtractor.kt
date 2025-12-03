package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.molecular.MolecularExtractor
import com.hartwig.actin.molecular.util.GeneConstants

class IhcExtractor : MolecularExtractor<IhcTest, IhcExtraction> {

    override fun extract(input: List<IhcTest>): List<IhcExtraction> {
        return input.groupBy { it.measureDate }
            .map { (date, tests) ->
                IhcExtraction(
                    date,
                    extractTestedGenes(tests, GeneConstants.IHC_FUSION_EVALUABLE_GENES),
                    extractTestedGenes(tests, GeneConstants.IHC_LOSS_EVALUABLE_GENES),
                )
            }
            .filter {
                it.fusionTestedGenes.isNotEmpty() || it.mutationAndDeletionTestedGenes.isNotEmpty()
            }
    }

    private fun extractTestedGenes(ihcTests: List<IhcTest>, ihcResults: Set<String>): Set<String> {
        return ihcTests.filter { it.item in ihcResults && !it.impliesPotentialIndeterminateStatus}
            .map { it.item }
            .toSet()
    }
}