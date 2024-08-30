package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.datamodel.clinical.AtcClassification
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.medication.AtcTree

internal object AtcTestFactory {

    fun atcClassification(anatomicalCode: String = ""): AtcClassification {
        return AtcClassification(
            anatomicalMainGroup = AtcLevel(name = "", code = anatomicalCode),
            chemicalSubGroup = createMinimalAtcLevel(),
            chemicalSubstance = createMinimalAtcLevel(),
            pharmacologicalSubGroup = createMinimalAtcLevel(),
            therapeuticSubGroup = createMinimalAtcLevel()
        )
    }

    fun createProperAtcTree(): AtcTree {
        return AtcTree(
            mapOf(
                "A07A" to NAME_NOT_APPLICABLE,
                "A07AC" to NAME_NOT_APPLICABLE,
                "B01" to NAME_NOT_APPLICABLE,
                "B01AA" to NAME_NOT_APPLICABLE,
                "B01AB" to NAME_NOT_APPLICABLE,
                "B01AC" to NAME_NOT_APPLICABLE,
                "B01AD" to NAME_NOT_APPLICABLE,
                "B01AE" to NAME_NOT_APPLICABLE,
                "B01AF" to NAME_NOT_APPLICABLE,
                "B01AX" to NAME_NOT_APPLICABLE,
                "B02" to NAME_NOT_APPLICABLE,
                "B03X" to NAME_NOT_APPLICABLE,
                "B03XA" to NAME_NOT_APPLICABLE,
                "D01AC" to NAME_NOT_APPLICABLE,
                "G01AA" to NAME_NOT_APPLICABLE,
                "G01AF" to NAME_NOT_APPLICABLE,
                "G01AG" to NAME_NOT_APPLICABLE,
                "G01BF" to NAME_NOT_APPLICABLE,
                "G03XA" to NAME_NOT_APPLICABLE,
                "G03XA" to NAME_NOT_APPLICABLE,
                "G03XA" to NAME_NOT_APPLICABLE,
                "H01CA" to NAME_NOT_APPLICABLE,
                "H01CA" to NAME_NOT_APPLICABLE,
                "H01CA" to NAME_NOT_APPLICABLE,
                "H01CC" to NAME_NOT_APPLICABLE,
                "H01CC" to NAME_NOT_APPLICABLE,
                "H01CC" to NAME_NOT_APPLICABLE,
                "H02" to NAME_NOT_APPLICABLE,
                "H05" to NAME_NOT_APPLICABLE,
                "J01" to NAME_NOT_APPLICABLE,
                "J02" to NAME_NOT_APPLICABLE,
                "J02AB" to NAME_NOT_APPLICABLE,
                "J02AC" to NAME_NOT_APPLICABLE,
                "J04" to NAME_NOT_APPLICABLE,
                "L01" to NAME_NOT_APPLICABLE,
                "L01A" to NAME_NOT_APPLICABLE,
                "L01BC" to NAME_NOT_APPLICABLE,
                "L01BC07" to NAME_NOT_APPLICABLE,
                "L01BC08" to NAME_NOT_APPLICABLE,
                "L01CD" to NAME_NOT_APPLICABLE,
                "L01F" to NAME_NOT_APPLICABLE,
                "L01FF" to NAME_NOT_APPLICABLE,
                "L01FX04" to NAME_NOT_APPLICABLE,
                "L01XA" to NAME_NOT_APPLICABLE,
                "L01XK" to NAME_NOT_APPLICABLE,
                "L02" to NAME_NOT_APPLICABLE,
                "L02" to NAME_NOT_APPLICABLE,
                "L02AE" to NAME_NOT_APPLICABLE,
                "L02AE" to NAME_NOT_APPLICABLE,
                "L02AE" to NAME_NOT_APPLICABLE,
                "L03" to NAME_NOT_APPLICABLE,
                "L03AA" to NAME_NOT_APPLICABLE,
                "L03AA" to NAME_NOT_APPLICABLE,
                "L04" to NAME_NOT_APPLICABLE,
                "M01" to NAME_NOT_APPLICABLE,
                "M01BA" to NAME_NOT_APPLICABLE,
                "M05B" to NAME_NOT_APPLICABLE,
                "M05BA" to NAME_NOT_APPLICABLE,
                "M05BB" to NAME_NOT_APPLICABLE,
                "M05BX04" to NAME_NOT_APPLICABLE,
                "N03" to NAME_NOT_APPLICABLE,
                "R02AB" to NAME_NOT_APPLICABLE,
                "string" to NAME_NOT_APPLICABLE,
                "string1" to NAME_NOT_APPLICABLE,
                "string2" to NAME_NOT_APPLICABLE,
                "string3" to NAME_NOT_APPLICABLE,
            )
        )
    }

    private fun createMinimalAtcLevel(): AtcLevel {
        return AtcLevel(name = "", code = "")
    }

    private const val NAME_NOT_APPLICABLE = ""
}