package com.hartwig.actin.database

interface DatabaseLoaderConfig {
    fun dbUser(): String
    fun dbPass(): String
    fun dbUrl(): String
}