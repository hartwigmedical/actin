package com.hartwig.actin.configuration

data class AlgoConfiguration(val warnIfToxicitiesNotFromQuestionnaire: Boolean = true) {

    companion object {
        fun create(environmentConfigFile: String?): AlgoConfiguration {
            return EnvironmentConfiguration.create(environmentConfigFile).algo
        }
    }
}
