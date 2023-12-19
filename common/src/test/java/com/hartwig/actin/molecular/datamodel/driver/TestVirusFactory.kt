package com.hartwig.actin.molecular.datamodel.driver

import org.apache.logging.log4j.util.Strings

object TestVirusFactory {
    @JvmStatic
    fun builder(): ImmutableVirus.Builder {
        return ImmutableVirus.builder()
            .from(TestDriverFactory.createEmptyDriver())
            .name(Strings.EMPTY)
            .type(VirusType.OTHER)
            .isReliable(false)
            .integrations(0)
    }
}
