package com.hartwig.actin.datamodel.molecular.evidence

import java.time.LocalDate

object TestMolecularMatchDetailsFactory {

    fun create(sourceDate: LocalDate,
               sourceEvent: String,
               sourceEvidenceType: EvidenceType = EvidenceType.ANY_MUTATION,
               sourceUrl: String = SOURCE_EVENT_URL
    ): MolecularMatchDetails {
        return MolecularMatchDetails(sourceDate, sourceEvent, sourceEvidenceType, null, sourceUrl)
    }
}