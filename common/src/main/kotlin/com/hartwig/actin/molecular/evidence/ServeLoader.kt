package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.datamodel.molecular.RefGenomeVersion
import com.hartwig.serve.datamodel.ImmutableServeRecord
import com.hartwig.serve.datamodel.RefGenome
import com.hartwig.serve.datamodel.ServeDatabase
import com.hartwig.serve.datamodel.ServeRecord
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.serialization.ServeJson
import com.hartwig.serve.datamodel.trial.ActionableTrial
import com.hartwig.serve.datamodel.trial.ImmutableActionableTrial

object ServeLoader {

    fun loadServeDatabase(jsonFilePath: String): ServeDatabase {
        return ServeJson.read(jsonFilePath)
    }

    fun loadServe37Record(jsonFilePath: String): ServeRecord {
        return loadServeRecord(jsonFilePath, RefGenomeVersion.V37)
    }

    fun loadServeRecord(jsonFilePath: String, refGenomeVersion: RefGenomeVersion): ServeRecord {
        val serveDatabase = loadServeDatabase(jsonFilePath)
        val serveRefGenomeVersion = toServeRefGenomeVersion(refGenomeVersion)
        val serveRecord = serveDatabase.records()[serveRefGenomeVersion]
            ?: throw IllegalStateException("No serve record for ref genome version $serveRefGenomeVersion")

        return ImmutableServeRecord.builder()
            .from(serveRecord)
            .trials(expandTrials(serveRecord.trials()))
            .build()
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

    fun expandTrials(trials: List<ActionableTrial>): List<ActionableTrial> {
        return trials.flatMap { trial ->
            trial.anyMolecularCriteria().flatMap { criterium -> expandWithIndicationAndCriterium(trial, criterium) }
        }
    }

    private fun expandWithIndicationAndCriterium(
        baseTrial: ActionableTrial,
        criterium: MolecularCriterium
    ): List<ActionableTrial> {
        return baseTrial.indications().map { indication ->
            ImmutableActionableTrial.builder()
                .from(baseTrial)
                .anyMolecularCriteria(listOf(criterium))
                .indications(listOf(indication))
                .build()
        }
    }
}