package com.hartwig.actin.datamodel.molecular.driver

interface VariantAlteration : GeneAlteration {
    val isHotspot: Boolean
}