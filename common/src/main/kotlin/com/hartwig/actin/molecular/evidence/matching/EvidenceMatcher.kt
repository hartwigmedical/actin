package com.hartwig.actin.molecular.evidence.matching

import com.hartwig.actin.molecular.evidence.actionability.ActionableEvents

interface EvidenceMatcher<T> {

    fun findMatches(event: T): ActionableEvents
}
