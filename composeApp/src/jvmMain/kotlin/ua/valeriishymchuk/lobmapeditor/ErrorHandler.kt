package ua.valeriishymchuk.lobmapeditor

import java.io.File

object ErrorHandler : Thread.UncaughtExceptionHandler {

    private val originalHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(t: Thread, e: Throwable) {

        val file = File("error.log")
        println("Got an error, print to error log")
        file.printWriter().use { writer ->
            e.printStackTrace(writer)
        }

        originalHandler?.uncaughtException(t, e) ?: e.printStackTrace()
    }
}