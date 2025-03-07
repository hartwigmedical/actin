package com.hartwig.actin.molecular.evidence

import com.hartwig.serve.datamodel.ServeDatabase
import com.hartwig.serve.datamodel.ServeRecord
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.hotspot.ActionableHotspot

object ServeVerifier {

    fun verifyServeDatabase(serveDatabase: ServeDatabase) {
        verifyNoCombinedMolecularProfiles(serveDatabase)
        verifyAllHotspotsHaveConsistentGenes(serveDatabase)
    }

    private fun verifyNoCombinedMolecularProfiles(serveDatabase: ServeDatabase) {
        serveDatabase.records().values.forEach { verifyNoCombinedMolecularProfiles(it) }
    }

    private fun verifyNoCombinedMolecularProfiles(serveRecord: ServeRecord) {
        val hasCombinedEvidence = serveRecord.evidences().any { evidence -> isCombinedProfile(evidence.molecularCriterium()) }
        val hasCombinedTrials = serveRecord.trials().any { trial -> trial.anyMolecularCriteria().any { isCombinedProfile(it) } }

        if (hasCombinedEvidence || hasCombinedTrials) {
            throw IllegalStateException("SERVE record contains combined profiles")
        }
    }

    private fun isCombinedProfile(molecularCriterium: MolecularCriterium): Boolean {
        val criteriaCount = molecularCriterium.hotspots().size +
                molecularCriterium.codons().size +
                molecularCriterium.exons().size +
                molecularCriterium.genes().size +
                molecularCriterium.fusions().size +
                molecularCriterium.characteristics().size +
                molecularCriterium.hla().size

        if (criteriaCount == 0) {
            throw IllegalStateException("Molecular criterium found without actual specific criteria: $molecularCriterium")
        }

        return criteriaCount > 1
    }

    private fun verifyAllHotspotsHaveConsistentGenes(serveDatabase: ServeDatabase) {
        serveDatabase.records().values.forEach { verifyHotspotsInServeRecord(it) }
    }

    private fun verifyHotspotsInServeRecord(serveRecord: ServeRecord) {
        serveRecord.evidences()
            .flatMap { it.molecularCriterium().hotspots() }
            .forEach { verifyHotspotGeneConsistency(it) }

        serveRecord.trials()
            .flatMap { it.anyMolecularCriteria() }
            .flatMap { it.hotspots() }
            .forEach { verifyHotspotGeneConsistency(it) }
    }

    private fun verifyHotspotGeneConsistency(actionableHotspot: ActionableHotspot) {
        if (actionableHotspot.variants().isEmpty()) {
            throw IllegalStateException("Hotspot contains no variant annotations: $actionableHotspot")
        }

        val gene = actionableHotspot.variants().first().gene()
        if (actionableHotspot.variants().any { it.gene() != gene }) {
            throw IllegalStateException("Hotspot contains variant annotations with different genes: $actionableHotspot")
        }
    }
}