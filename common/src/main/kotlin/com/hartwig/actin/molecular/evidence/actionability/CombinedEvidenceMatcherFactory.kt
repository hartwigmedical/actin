package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.serve.datamodel.ServeRecord

object CombinedEvidenceMatcherFactory {

    fun create(serveRecord: ServeRecord): CombinedEvidenceMatcher {
        return CombinedEvidenceMatcher(serveRecord.evidences())
    }
}