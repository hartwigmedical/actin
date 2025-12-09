package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencingTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.panel.PanelSpecificationFunctions
import com.hartwig.actin.datamodel.molecular.panel.PanelTargetSpecification
import com.hartwig.actin.datamodel.molecular.panel.PanelTestSpecification
import com.hartwig.actin.datamodel.molecular.panel.TestVersion
import com.hartwig.actin.molecular.filter.GeneFilter
import org.apache.logging.log4j.LogManager

class PanelSpecifications(
    private val geneFilter: GeneFilter,
    panelSpecifications: Map<PanelTestSpecification, List<PanelGeneSpecification>>
) {

    private val logger = LogManager.getLogger(PanelSpecifications::class.java)

    private val molecularTargetsPerTest: Map<PanelTestSpecification, Map<String, List<MolecularTestTarget>>> =
        panelSpecifications.mapValues { (_, geneSpecs) ->
            geneSpecs.groupBy(PanelGeneSpecification::geneName).mapValues { it.value.flatMap(PanelGeneSpecification::targets) }
        }

    val panelTestSpecifications: Set<PanelTestSpecification>
        get() = molecularTargetsPerTest.keys

    fun panelTargetSpecification(input: SequencingTest, testVersion: TestVersion): PanelTargetSpecification {
        val testSpec = PanelTestSpecification(input.test, testVersion)
        val derivedTargets = PanelSpecificationFunctions.derivedGeneTargetMap(input)

        checkForUnknownGenes(derivedTargets.keys, testSpec)

        val baseTargets = molecularTargetsPerTest[testSpec]
            ?: throw IllegalStateException(
                "${logPanelName(testSpec)} is not found in panel specifications. Check curation and map to one " +
                        "of [${molecularTargetsPerTest.keys.joinToString()}] or add this panel to the specification TSV."
            )
        val mergedTargets = (baseTargets.keys + derivedTargets.keys)
            .associateWith { gene ->
                ((baseTargets[gene] ?: emptyList()) + (derivedTargets[gene] ?: emptyList())).distinct()
            }

        if (mergedTargets != baseTargets) {
            logger.warn(
                "${logPanelName(testSpec)} has results containing molecular test target(s) for gene(s) not found in the panel " +
                        "specifications; these molecular test target(s) for gene(s) are used during evaluation"
            )
        }

        return PanelTargetSpecification(mergedTargets, testSpec.testVersion)
    }

    private fun checkForUnknownGenes(results: Set<String>, testSpec: PanelTestSpecification) {
        results.filterNot(geneFilter::include).takeIf { it.isNotEmpty() }
            ?.let { unknownGenes ->
                throw IllegalStateException(
                    "${logPanelName(testSpec)} has results associated containing " +
                            "gene(s) not present in SERVE known genes: ${unknownGenes.joinToString()}." +
                            "Correct this in the feed UI before continuing."
                )
            }
    }

    fun logPanelName(testSpec: PanelTestSpecification) =
        "Panel [${testSpec.testName}${testSpec.testVersion.versionDate?.let { " version $it" } ?: ""}]"
}
