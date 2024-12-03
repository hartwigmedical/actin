package com.hartwig.actin.datamodel.molecular.evidence

object TestEvidenceDirectionFactory {

    fun certainPositiveResponse(): EvidenceDirection {
        return EvidenceDirection(
            hasPositiveResponse = true,
            hasBenefit = true,
            isResistant = false,
            isCertain = true
        )
    }

    fun uncertainPositiveResponse(): EvidenceDirection {
        return EvidenceDirection(
            hasPositiveResponse = true,
            hasBenefit = true,
            isResistant = false,
            isCertain = false
        )
    }

    fun certainResistant(): EvidenceDirection {
        return EvidenceDirection(
            hasPositiveResponse = false,
            hasBenefit = false,
            isResistant = true,
            isCertain = true
        )
    }

    fun uncertainResistant(): EvidenceDirection {
        return EvidenceDirection(
            hasPositiveResponse = false,
            hasBenefit = false,
            isResistant = true,
            isCertain = false
        )
    }

    fun noBenefit(): EvidenceDirection {
        return EvidenceDirection(
            hasPositiveResponse = false,
            hasBenefit = false,
            isResistant = false,
            isCertain = true
        )
    }
}