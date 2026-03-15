/*
 *   Copyright (c) 2026 Willian Santos
 *
 *   This work is licensed under the Creative Commons
 *   Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0).
 *
 *   You are free to:
 *     - Share  — copy and redistribute the material in any medium or format
 *     - Adapt  — remix, transform, and build upon the material
 *
 *   Under the following terms:
 *     - Attribution    — You must give appropriate credit, provide a link to
 *                        the license, and indicate if changes were made.
 *     - NonCommercial  — You may not use the material for commercial purposes.
 *
 *   Owner rights:
 *     - Willian Santos retains all commercial rights.
 *    - The copyright holder may use, sell, sublicense, or relicense this
 *       work under different terms at any time.
 *
 *   Full license: https://creativecommons.org/licenses/by-nc/4.0/legalcode
 */

package com.bitlabbr.minhadespensa.core.domain.util

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ConsoleLogger(private val moduleName: String) : AppLogger {

    private val appName = "Minha Despensa:"

    override fun d(tag: String, message: String) {
        println(buildLogLine("D", tag, message))
    }

    override fun e(tag: String, message: String, error: Throwable?) {
        val fullMessage = message + (error?.let { "\n${it.stackTraceToString()}" } ?: "")
        println(buildLogLine("E", tag, fullMessage))
    }

    private fun buildLogLine(type: String, tag: String, message: String): String {
        val time = getCurrentTime()
        return "$time [$type] [$appName] [$moduleName] [$tag]: $message"
    }

    private fun getCurrentTime(): String {
        val now = Clock.System.now()
        val local = now.toLocalDateTime(TimeZone.Companion.currentSystemDefault())

        val year = local.year
        val month = local.monthNumber.pad()
        val day = local.dayOfMonth.pad()
        val hour = local.hour.pad()
        val minute = local.minute.pad()
        val second = local.second.pad()
        val millis = (local.nanosecond / 1_000_000).pad(3)

        return "$year-$month-$day $hour:$minute:$second.$millis"
    }

    private fun Int.pad(digits: Int = 2): String {
        return this.toString().padStart(digits, '0')
    }
}