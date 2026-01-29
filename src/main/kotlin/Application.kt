package com.assignment

import com.assignment.database.configureDatabases
import io.ktor.server.application.Application

fun main(args: Array<String>) {
    io.ktor.server.tomcat.jakarta.EngineMain.main(args)
}

fun Application.module() {
    configureExceptionHandler()
    configureSerialization()
    configureDatabases()
}
