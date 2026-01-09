package com.hartwig.actin.configuration

data class MolecularConfiguration(val variantPathogenicityIsConfirmed: Boolean = false) {

    companion object {
        fun create(environmentConfigFile: String?): MolecularConfiguration {
            return EnvironmentConfiguration.create(environmentConfigFile).molecular
        }
    }
}