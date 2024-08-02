package com.hartwig.actin.serve

import com.hartwig.serve.datamodel.MutationType
import com.hartwig.serve.datamodel.fusion.FusionPair
import com.hartwig.serve.datamodel.gene.GeneAnnotation
import com.hartwig.serve.datamodel.gene.GeneEvent
import com.hartwig.serve.datamodel.hotspot.VariantHotspot
import com.hartwig.serve.datamodel.range.RangeAnnotation

object TestServeFactory {

    fun createEmptyHotspot(): VariantHotspot {
        return object : VariantHotspot {
            override fun gene(): String {
                return ""
            }

            override fun ref(): String {
                return ""
            }

            override fun alt(): String {
                return ""
            }

            override fun chromosome(): String {
                return ""
            }

            override fun position(): Int {
                return 0
            }
        }
    }

    fun createEmptyRangeAnnotation(): RangeAnnotation {
        return object : RangeAnnotation {
            override fun gene(): String {
                return ""
            }

            override fun applicableMutationType(): MutationType {
                return MutationType.ANY
            }

            override fun chromosome(): String {
                return ""
            }

            override fun start(): Int {
                return 0
            }

            override fun end(): Int {
                return 0
            }
        }
    }

    fun createEmptyGeneAnnotation(): GeneAnnotation {
        return object : GeneAnnotation {
            override fun gene(): String {
                return ""
            }

            override fun event(): GeneEvent {
                return GeneEvent.ANY_MUTATION
            }
        }
    }

    fun createEmptyFusionPair(): FusionPair {
        return object : FusionPair {
            override fun geneUp(): String {
                return ""
            }

            override fun minExonUp(): Int? {
                return null
            }

            override fun maxExonUp(): Int? {
                return null
            }

            override fun geneDown(): String {
                return ""
            }

            override fun minExonDown(): Int? {
                return null
            }

            override fun maxExonDown(): Int? {
                return null
            }
        }
    }
}
