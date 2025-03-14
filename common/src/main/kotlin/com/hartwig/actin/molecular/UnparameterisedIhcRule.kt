package com.hartwig.actin.molecular

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class UnparameterisedIhcRule(val targetProtein: String) {
    companion object {
        const val HER2 = "HER2"
        const val PDL1 = "PDL1"
    }
}