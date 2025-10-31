package com.hartwig.actin.configuration

data class AlgoConfiguration(
    val warnIfToxicitiesNotFromQuestionnaire: Boolean = true,
    val maxMolecularTestAgeInDays: Int? = null
) {

    companion object {
        fun create(environmentConfigFile: String?): AlgoConfiguration {
            return EnvironmentConfiguration.create(environmentConfigFile).algo
        }
    }
}
