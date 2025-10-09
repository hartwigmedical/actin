package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencedNegativeResult
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.panel.PanelTargetSpecification
import com.hartwig.actin.datamodel.molecular.panel.PanelTestSpecification
import com.hartwig.actin.molecular.filter.GeneFilter

class PanelSpecifications(
    private val geneFilter: GeneFilter,
    panelSpecifications: Map<PanelTestSpecification, List<PanelGeneSpecification>>
) {

    private val molecularTargetsPerTest: Map<PanelTestSpecification, Map<String, List<MolecularTestTarget>>> =
        panelSpecifications.mapValues { (_, geneSpecs) ->
            geneSpecs.groupBy(PanelGeneSpecification::geneName).mapValues { it.value.flatMap(PanelGeneSpecification::targets) }
        }

    val panelTestSpecifications: Set<PanelTestSpecification>
        get() = molecularTargetsPerTest.keys

    fun panelTargetSpecification(
        testSpec: PanelTestSpecification,
        negativeResults: Set<SequencedNegativeResult>?
    ): PanelTargetSpecification {

        checkForUnknownGenesInNegativeResults(negativeResults, testSpec)

        val baseTargets = molecularTargetsPerTest[testSpec]
            ?: throw IllegalStateException(
                "${logPanelName(testSpec)} is not found in panel specifications. Check curation and map to one " +
                        "of [${molecularTargetsPerTest.keys.joinToString()}] or add this panel to the specification TSV."
            )
        val negativeTargets =
            (negativeResults?.groupBy(keySelector = { it.gene }, valueTransform = { it.molecularTestTarget }) ?: emptyMap())
        val mergedTargets = (baseTargets.keys + negativeTargets.keys)
            .associateWith { gene ->
                ((baseTargets[gene] ?: emptyList()) + (negativeTargets[gene] ?: emptyList())).distinct()
            }
        return PanelTargetSpecification(mergedTargets)
    }

    private fun checkForUnknownGenesInNegativeResults(
        negativeResults: Set<SequencedNegativeResult>?,
        testSpec: PanelTestSpecification
    ) {
        negativeResults?.map(SequencedNegativeResult::gene)?.toSet()
            ?.filterNot(geneFilter::include).takeIf { it?.isNotEmpty() == true }
            ?.let { unknownGenes ->
                throw IllegalStateException(
                    "${logPanelName(testSpec)} has negative results associated containing " +
                            "gene(s) not present in SERVE known genes: ${unknownGenes.joinToString()}." +
                            "Correct this in the feed UI before continuing."
                )
            }
    }

    fun logPanelName(testSpec: PanelTestSpecification) = "Panel [${testSpec.testName}${testSpec.versionDate?.let { " version $it" } ?: ""}]"
}
