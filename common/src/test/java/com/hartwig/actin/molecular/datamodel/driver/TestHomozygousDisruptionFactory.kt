package com.hartwig.actin.molecular.datamodel.driver

import org.apache.logging.log4j.util.Strings

object TestHomozygousDisruptionFactory {
    @JvmStatic
    fun builder(): ImmutableHomozygousDisruption.Builder {
        return ImmutableHomozygousDisruption.builder()
            .from(TestDriverFactory.createEmptyDriver())
            .gene(Strings.EMPTY)
            .geneRole(GeneRole.UNKNOWN)
            .proteinEffect(ProteinEffect.UNKNOWN)
    }
}
