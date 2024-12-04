package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.datamodel.molecular.RefGenomeVersion
import com.hartwig.serve.datamodel.RefGenome
import com.hartwig.serve.datamodel.ServeDatabase
import com.hartwig.serve.datamodel.ServeRecord
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.serialization.ServeJson

object ServeLoader {

    fun loadServeDatabase(jsonFilePath: String): ServeDatabase {
        val serveDatabase = ServeJson.read(jsonFilePath)

        verifyNoCombinedMolecularProfiles(serveDatabase)

        return serveDatabase
    }

    fun loadServeRecord(jsonFilePath: String, refGenomeVersion: RefGenomeVersion): ServeRecord {
        val serveDatabase = loadServeDatabase(jsonFilePath)
        val serveRefGenomeVersion = toServeRefGenomeVersion(refGenomeVersion)

        return serveDatabase.records()[serveRefGenomeVersion]
            ?: throw IllegalStateException("No serve record for ref genome version $serveRefGenomeVersion")
    }

    fun loadServe37Record(jsonFilePath: String): ServeRecord {
        return loadServeRecord(jsonFilePath, RefGenomeVersion.V37)
    }

    private fun toServeRefGenomeVersion(refGenomeVersion: RefGenomeVersion): RefGenome {
        return when (refGenomeVersion) {
            RefGenomeVersion.V37 -> {
                RefGenome.V37
            }

            RefGenomeVersion.V38 -> {
                RefGenome.V38
            }
        }
    }

    fun verifyNoCombinedMolecularProfiles(serveDatabase: ServeDatabase) {
        serveDatabase.records().values.map { verifyNoCombinedMolecularProfiles(it) }
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
            throw IllegalStateException("Molecular criterium found without actual criteria: $molecularCriterium")
        }

        return criteriaCount > 1
    }
}
