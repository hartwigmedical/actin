package com.hartwig.actin.datamodel.molecular.driver

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory

object TestTranscriptVariantImpactFactory {

    fun createMinimal(): TranscriptVariantImpact {
        // TODO (KD): Simplify test factories for molecular objects
        return TestMolecularFactory.createMinimalTranscriptImpact()
    }
}
