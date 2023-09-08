package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.clinical.datamodel.ImmutableAtcClassification
import com.hartwig.actin.clinical.datamodel.ImmutableAtcLevel

internal object AtcTestFactory {

    fun atcClassificationBuilder(): ImmutableAtcClassification.Builder {
        return ImmutableAtcClassification.builder().anatomicalMainGroup(atcLevelBuilder().build())
            .chemicalSubGroup(atcLevelBuilder().build())
            .chemicalSubstance(atcLevelBuilder().build())
            .pharmacologicalSubGroup(atcLevelBuilder().build())
            .therapeuticSubGroup(atcLevelBuilder().build())
    }

    fun atcLevelBuilder(): ImmutableAtcLevel.Builder {
        return ImmutableAtcLevel.builder().name("").code("")
    }

    fun createProperAtcTree(): AtcTree {
        return AtcTree(
            mapOf(
                "H05" to "",
                "M05B" to "",
                "B01" to "",
                "B02" to "",
                "D01AC" to "",
                "J02AC" to "",
                "J02AB" to "",
                "B01AA" to "",
                "H01CC" to "",
                "H01CA" to "",
                "G03XA" to "",
                "L02AE" to "",
                "L01" to "",
                "L02" to "",
                "L04" to "",
                "H01CC" to "",
                "H01CA" to "",
                "G03XA" to "",
                "L02AE" to "",
                "L01FF02" to "",
                "L01FF01" to "",
                "L01FX04" to "",
                "L01FF06" to "",
                "L01FF04" to "",
                "L01BC07" to "",
                "L01BC08" to "",
                "L01XA" to "",
                "L01BC" to "",
                "L01CD" to "",
                "L01A" to "",
                "H01CC" to "",
                "H01CA" to "",
                "G03XA" to "",
                "L02AE" to "",
                "L01XK" to "",
                "B03XA" to "",
                "L03AA" to "",
                "string" to "",
                "string1" to "",
                "string2" to "",
                "string3" to ""
            )
        )
    }
}