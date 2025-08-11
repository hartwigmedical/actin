package com.hartwig.actin.datamodel.molecular.panel

import com.hartwig.actin.datamodel.clinical.SequencingTest
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import java.time.LocalDate

object PanelSpecificationFunctions {

    fun derivedGeneTargetMap(testResults: SequencingTest): Map<String, List<MolecularTestTarget>> {
        val geneTargetPairs = (
                testResults.variants.map { it.gene to listOf(MolecularTestTarget.MUTATION) } +
                        testResults.fusions.flatMap {
                            listOfNotNull(
                                it.geneUp,
                                it.geneDown
                            ).map { gene -> gene to listOf(MolecularTestTarget.FUSION) }
                        } +
                        testResults.amplifications.map {
                            it.gene to listOf(
                                MolecularTestTarget.MUTATION,
                                MolecularTestTarget.AMPLIFICATION
                            )
                        } +
                        testResults.deletions.map { it.gene to listOf(MolecularTestTarget.MUTATION, MolecularTestTarget.DELETION) } +
                        testResults.skippedExons.map { it.gene to listOf(MolecularTestTarget.FUSION, MolecularTestTarget.MUTATION) } +
                        testResults.negativeResults.map { it.gene to listOf(it.molecularTestTarget) }
                )

        return geneTargetPairs
            .groupBy({ it.first }, { it.second })
            .mapValues { entry -> entry.value.flatten().distinct() }
    }

    fun determineTestVersion(
        test: SequencingTest,
        panelTestSpecifications: Set<PanelTestSpecification>,
        registrationDate: LocalDate
    ): LocalDate? {
        val referenceDate = test.date ?: registrationDate
        return panelTestSpecifications
            .filter { it.testName == test.test }
            .filter { it.versionDate?.isAfter(referenceDate) == false }
            .maxByOrNull { it.versionDate!! }
            ?.versionDate
    }
}