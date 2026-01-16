package com.hartwig.actin.configuration

data class MolecularConfiguration(val eventPathogenicityIsConfirmed: Boolean = false) {

    companion object {
        fun create(environmentConfigFile: String?): MolecularConfiguration {
            return EnvironmentConfiguration.create(environmentConfigFile).molecular
        }
    }
}