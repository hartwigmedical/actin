package com.hartwig.actin.report.pdf.util

import com.itextpdf.kernel.colors.DeviceRgb
import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class StylesTest {

    @AfterEach
    fun resetColors() {
        Styles.loadColors()
    }

    @Test
    fun `Should override colors present in colors file and keep defaults for others`(@TempDir tempDir: File) {
        val colorsFile = File(tempDir, "colors.properties").apply { writeText("red=1, 2, 3\n") }

        Styles.loadColors(colorsFile.path)

        assertThat(Styles.color(PaletteColor.RED)).isEqualTo(DeviceRgb(1, 2, 3))
        assertThat(Styles.color(PaletteColor.BLUE)).isEqualTo(PaletteColor.BLUE.default)
    }

    @Test
    fun `Should reset to default colors when no colors file is given`(@TempDir tempDir: File) {
        Styles.loadColors(File(tempDir, "colors.properties").apply { writeText("red=1,2,3\n") }.path)

        Styles.loadColors()

        assertThat(Styles.color(PaletteColor.RED)).isEqualTo(PaletteColor.RED.default)
    }

    @Test
    fun `Should throw exception on unknown color`(@TempDir tempDir: File) {
        val colorsFile = File(tempDir, "colors.properties").apply { writeText("purple=1,2,3\n") }

        assertThatThrownBy { Styles.loadColors(colorsFile.path) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("purple")
    }

    @Test
    fun `Should throw exception on malformed color`(@TempDir tempDir: File) {
        val colorsFile = File(tempDir, "colors.properties").apply { writeText("red=1,2,300\n") }

        assertThatThrownBy { Styles.loadColors(colorsFile.path) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("red")
    }
}
