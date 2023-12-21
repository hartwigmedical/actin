package com.hartwig.actin.clinical.interpretation

import com.google.common.collect.Lists
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory.createProperTestClinicalRecord
import com.hartwig.actin.clinical.interpretation.LabInterpreter.interpret
import org.junit.Assert
import org.junit.Test

class LabInterpreterTest {
    @Test
    fun canGenerateLabInterpretation() {
        Assert.assertNotNull(interpret(Lists.newArrayList()))
        Assert.assertNotNull(interpret(createProperTestClinicalRecord().labValues()))
    }

    @Test
    fun canMapValues() {
        val firstKey = LabInterpreter.MAPPINGS.keys.iterator().next()
        val firstValue = LabInterpreter.MAPPINGS[firstKey]
        val values: MutableList<LabValue> = Lists.newArrayList()
        values.add(LabInterpretationTestFactory.builder().code(firstKey.code()).unit(firstKey.defaultUnit()).build())
        values.add(LabInterpretationTestFactory.builder().code(firstValue!!.code()).unit(firstValue.defaultUnit()).build())
        val interpretation = interpret(values)
        Assert.assertEquals(1, interpretation.allValues(firstKey)!!.size.toLong())
        Assert.assertEquals(2, interpretation.allValues(firstValue)!!.size.toLong())
        for (labValue in interpretation.allValues(firstValue)!!) {
            Assert.assertEquals(firstValue.code(), labValue.code())
            Assert.assertEquals(firstValue.defaultUnit(), labValue.unit())
        }
    }
}