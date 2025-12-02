package com.hartwig.actin.molecular.filter

import com.hartwig.actin.datamodel.molecular.MolecularTest

class MolecularTestFilter(private val useInsufficientQualityRecords: Boolean) {

    fun apply(tests: List<MolecularTest>): List<MolecularTest> =
        if (useInsufficientQualityRecords) tests else tests.filter { it.hasSufficientQuality }
}