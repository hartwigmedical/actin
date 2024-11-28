package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.molecular.evidence.actionability.ActionableEvents
import com.hartwig.actin.molecular.evidence.actionability.ActionableEventsExtraction
import com.hartwig.serve.datamodel.RefGenome
import com.hartwig.serve.datamodel.molecular.KnownEvents
import com.hartwig.serve.datamodel.serialization.ServeJson

object ServeLoader {

    fun loadServe(filePath: String, serveRefGenomeVersion: RefGenome): Pair<KnownEvents, ActionableEvents> {
        val serveDatabase = ServeJson.read(filePath)
        val serveRecord = serveDatabase.records()[serveRefGenomeVersion]
            ?: throw IllegalStateException("No serve record for ref genome version $serveRefGenomeVersion")
        val expandedTrials = ActionableEventsExtraction.expandTrials(serveRecord.trials())
        return Pair(serveRecord.knownEvents(), ActionableEvents(serveRecord.evidences(), expandedTrials))
    }
}