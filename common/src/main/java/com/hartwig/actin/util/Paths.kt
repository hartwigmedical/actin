package com.hartwig.actin.util

import java.io.File

object Paths {
    @JvmStatic
    fun forceTrailingFileSeparator(path: String): String {
        return if (!path.endsWith(File.separator)) path + File.separator else path
    }
}
