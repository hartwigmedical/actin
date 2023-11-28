package com.hartwig.actin.molecular.orange.evidence.actionability

import com.hartwig.serve.datamodel.ActionableEvent

interface EvidenceMatcher<T> {

    fun findMatches(event: T): List<ActionableEvent>
}
