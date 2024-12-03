package com.hartwig.actin.clinical

import com.hartwig.actin.datamodel.clinical.QTProlongatingRisk
import com.hartwig.actin.testutil.ResourceLocator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class QTProlongatingDatabaseTest {

    private val database: QtProlongatingDatabase =
        QtProlongatingDatabase.create(ResourceLocator.resourceOnClasspath("medication/qt_prolongating.tsv"))

    @Test
    fun `Should return QTProlongatingRisk from medication name`() {
        assertThat(database.annotateWithQTProlongating("paracetamol")).isEqualTo(QTProlongatingRisk.KNOWN)
    }

    @Test
    fun `Should return QtProlongatingRisk NONE for unknown medication`() {
        assertThat(database.annotateWithQTProlongating("unknown medication")).isEqualTo(QTProlongatingRisk.NONE)
    }
}