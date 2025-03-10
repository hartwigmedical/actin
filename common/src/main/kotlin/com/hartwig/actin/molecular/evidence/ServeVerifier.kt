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
        val allMolecularCriteria = serveRecord.evidences().map { it.molecularCriterium() } +
                serveRecord.trials().flatMap { it.anyMolecularCriteria() }

        val (hotspotsWithoutVariants, hotspotsWithVariants) = allMolecularCriteria.flatMap(MolecularCriterium::hotspots)
            .partition { it.variants().isEmpty() }

        val inconsistentHotspots = hotspotsWithVariants.filter { hotspot ->
            val gene = hotspot.variants().first().gene()
            hotspot.variants().any { it.gene() != gene }
        }

        if (hotspotsWithoutVariants.isNotEmpty() || inconsistentHotspots.isNotEmpty()) {
            val message = buildString {
                if (hotspotsWithoutVariants.isNotEmpty()) {
                    append("Hotspots without variants: $hotspotsWithoutVariants\n")
                }
                if (inconsistentHotspots.isNotEmpty()) {
                    append("Hotspot with mismatched genes: $inconsistentHotspots\n")
                }
            }
            throw IllegalStateException("SERVE record contains invalid hotspots:\n$message")
        }
    }

    private fun verifyHotspotsInServeRecordMine(serveRecord: ServeRecord) {
        val allMolecularCriteria = serveRecord.evidences().map { it.molecularCriterium() } +
                serveRecord.trials().flatMap { it.anyMolecularCriteria() }

        val (hotspotsWithoutVariants, hotspotsWithVariants) = allMolecularCriteria.flatMap(MolecularCriterium::hotspots)
            .partition { it.variants().isEmpty() }

        val inconsistentHotspots = hotspotsWithVariants.filter { hotspot ->
            val gene = hotspot.variants().first().gene()
            hotspot.variants().any { it.gene() != gene }
        }

        if (hotspotsWithoutVariants.isNotEmpty() || inconsistentHotspots.isNotEmpty()) {
            val message = buildString {
                if (hotspotsWithoutVariants.isNotEmpty()) {
                    append("Hotspots without variants: $hotspotsWithoutVariants\n")
                }
                if (inconsistentHotspots.isNotEmpty()) {
                    append("Hotspot with mismatched genes: $inconsistentHotspots\n")
                }
            }
            throw IllegalStateException("SERVE record contains invalid hotspots:\n$message")
        }
    }
}