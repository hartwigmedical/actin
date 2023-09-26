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
                "H05" to NAME_NOT_APPLICABLE,
                "M05B" to NAME_NOT_APPLICABLE,
                "B01" to NAME_NOT_APPLICABLE,
                "B02" to NAME_NOT_APPLICABLE,
                "D01AC" to NAME_NOT_APPLICABLE,
                "J02AC" to NAME_NOT_APPLICABLE,
                "J02AB" to NAME_NOT_APPLICABLE,
                "B01AA" to NAME_NOT_APPLICABLE,
                "H01CC" to NAME_NOT_APPLICABLE,
                "H01CA" to NAME_NOT_APPLICABLE,
                "G03XA" to NAME_NOT_APPLICABLE,
                "L02AE" to NAME_NOT_APPLICABLE,
                "L01" to NAME_NOT_APPLICABLE,
                "L02" to NAME_NOT_APPLICABLE,
                "L04" to NAME_NOT_APPLICABLE,
                "H01CC" to NAME_NOT_APPLICABLE,
                "H01CA" to NAME_NOT_APPLICABLE,
                "G03XA" to NAME_NOT_APPLICABLE,
                "L02AE" to NAME_NOT_APPLICABLE,
                "L01FF02" to NAME_NOT_APPLICABLE,
                "L01FF01" to NAME_NOT_APPLICABLE,
                "L01FX04" to NAME_NOT_APPLICABLE,
                "L01FF06" to NAME_NOT_APPLICABLE,
                "L01FF04" to NAME_NOT_APPLICABLE,
                "L01BC07" to NAME_NOT_APPLICABLE,
                "L01BC08" to NAME_NOT_APPLICABLE,
                "L01XA" to NAME_NOT_APPLICABLE,
                "L01BC" to NAME_NOT_APPLICABLE,
                "L01CD" to NAME_NOT_APPLICABLE,
                "L01A" to NAME_NOT_APPLICABLE,
                "H01CC" to NAME_NOT_APPLICABLE,
                "H01CA" to NAME_NOT_APPLICABLE,
                "G03XA" to NAME_NOT_APPLICABLE,
                "L02AE" to NAME_NOT_APPLICABLE,
                "L01XK" to NAME_NOT_APPLICABLE,
                "B03XA" to NAME_NOT_APPLICABLE,
                "L03AA" to NAME_NOT_APPLICABLE,
                "A07A" to NAME_NOT_APPLICABLE,
                "G01AA" to NAME_NOT_APPLICABLE,
                "R02AB" to NAME_NOT_APPLICABLE,
                "L01D" to NAME_NOT_APPLICABLE,
                "J01" to NAME_NOT_APPLICABLE,
                "J02" to NAME_NOT_APPLICABLE,
                "J04" to NAME_NOT_APPLICABLE,
                "H02" to NAME_NOT_APPLICABLE,
                "N02" to NAME_NOT_APPLICABLE,
                "L01F" to NAME_NOT_APPLICABLE,
                "L02" to NAME_NOT_APPLICABLE,
                "B03X" to NAME_NOT_APPLICABLE,
                "L03AA" to NAME_NOT_APPLICABLE,
                "M05BA" to NAME_NOT_APPLICABLE,
                "M05BB" to NAME_NOT_APPLICABLE,
                "string" to NAME_NOT_APPLICABLE,
                "string1" to NAME_NOT_APPLICABLE,
                "string2" to NAME_NOT_APPLICABLE,
                "string3" to NAME_NOT_APPLICABLE,
            )
        )
    }

    private const val NAME_NOT_APPLICABLE = ""
}