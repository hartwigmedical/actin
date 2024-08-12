package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.serve.datamodel.ActionableEvent

interface EvidenceMatcher<T> {

    fun findMatches(event: T): List<ActionableEvent>
}
