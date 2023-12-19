package com.hartwig.actin.molecular.datamodel.driver

import com.hartwig.actin.molecular.datamodel.evidence.ImmutableActionableEvidence

internal object TestDriverFactory {
    fun createEmptyDriver(): Driver {
        return object : Driver {
            override val isReportable: Boolean
                get() = false

            override fun event(): String {
                return org.apache.logging.log4j.util.Strings.EMPTY
            }

            override fun driverLikelihood(): DriverLikelihood? {
                return null
            }

            override fun evidence(): ActionableEvidence {
                return ImmutableActionableEvidence.builder().build()
            }
        }
    }
}
