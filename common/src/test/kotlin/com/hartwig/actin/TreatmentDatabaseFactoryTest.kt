package com.hartwig.actin

import com.hartwig.actin.TreatmentDatabaseFactory.createFromPath
import com.hartwig.actin.testutil.ResourceLocator
import java.nio.file.NoSuchFileException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class TreatmentDatabaseFactoryTest {
    private val resourceLocator = ResourceLocator()

    @Test
    fun `Should create database from directory`() {
        val treatmentDatabase = createFromPath(resourceLocator.onClasspath("clinical"))
        assertThat(treatmentDatabase).isNotNull()
        assertThat(treatmentDatabase.findTreatmentByName("Capecitabine+Oxaliplatin")).isNotNull()
        assertThat(treatmentDatabase.findTreatmentByName("CAPECITABINE AND OXALIPLATIN")).isNotNull()
    }

    @Test
    fun `Should throw exception on create when files are missing`() {
        assertThatThrownBy { createFromPath(resourceLocator.onClasspath("molecular")) }
            .isInstanceOf(NoSuchFileException::class.java)
    }
}
