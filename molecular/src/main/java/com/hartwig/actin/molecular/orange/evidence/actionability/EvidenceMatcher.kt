package com.hartwig.actin.molecular.orange.evidence.actionability

import com.hartwig.serve.datamodel.ActionableEvent

interface EvidenceMatcher<T> {
    open fun findMatches(event: T?): MutableList<ActionableEvent?>
}
