package com.hartwig.actin.molecular.evidence

import com.hartwig.serve.datamodel.ServeDatabase
import com.hartwig.serve.datamodel.ServeRecord
import com.hartwig.serve.datamodel.molecular.MolecularCriterium

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
        val allMolecularCriteria = serveRecord.evidences().asSequence().map { it.molecularCriterium() } +
                serveRecord.trials().asSequence().flatMap { it.anyMolecularCriteria() }

        val hotspotWithoutVariants = allMolecularCriteria.flatMap(MolecularCriterium::hotspots)
            .find { it.variants().isEmpty() }

        val inconsistentHotspot = allMolecularCriteria.flatMap(MolecularCriterium::hotspots)
            .find {
                it.variants().isNotEmpty() && it.variants().any { variant -> variant.gene() != it.variants().first().gene() }
            }

        if (hotspotWithoutVariants != null || inconsistentHotspot != null) {
            val message = buildString {
                if (hotspotWithoutVariants != null) {
                    append("Hotspot without variants: $hotspotWithoutVariants\n")
                }
                if (inconsistentHotspot != null) {
                    append("Hotspot with mismatched genes: $inconsistentHotspot\n")
                }
            }
            throw IllegalStateException("SERVE record contains invalid hotspots:\n$message")
        }
    }
}