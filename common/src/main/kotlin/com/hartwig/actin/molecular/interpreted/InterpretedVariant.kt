package com.hartwig.actin.molecular.interpreted

import com.hartwig.actin.molecular.datamodel.driver.Driver
import com.hartwig.actin.molecular.datamodel.driver.GeneAlteration
import com.hartwig.actin.molecular.datamodel.driver.TranscriptImpact
import com.hartwig.actin.molecular.datamodel.driver.VariantType

interface InterpretedVariant : Driver, GeneAlteration {
    val chromosome: String
    val position: Int
    val ref: String
    val alt: String
    val type: VariantType
    val isHotspot: Boolean
    val canonicalImpact: TranscriptImpact
    val clonalLikelihood: Double
}