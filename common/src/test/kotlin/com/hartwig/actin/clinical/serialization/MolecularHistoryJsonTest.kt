package com.hartwig.actin.clinical.serialization

import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import com.hartwig.actin.molecular.serialization.MolecularHistoryJson.fromJson
import com.hartwig.actin.molecular.serialization.MolecularHistoryJson.toJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MolecularHistoryJsonTest {

    @Test
    fun `Should be able to read and write Molecular History to JSON`() {
        val proper = TestMolecularFactory.createExhaustiveTestMolecularHistory()
        val convertedProper = fromJson(toJson(proper))
        assertThat(convertedProper).isEqualTo(proper)
    }
}