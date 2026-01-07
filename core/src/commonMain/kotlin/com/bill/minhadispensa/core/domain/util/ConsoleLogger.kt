/*
 * Copyright (c) 2026 Willian Santos
 *
 * Licensed under the Creative Commons Attribution-NonCommercial 4.0
 * International License (CC BY-NC 4.0).
 *
 * You may use, copy, modify, and distribute this file for non-commercial
 * purposes only, provided that proper attribution is given.
 *
 * The copyright holder retains all commercial rights and may
 * license this work under different terms.
 *
 * License: https://creativecommons.org/licenses/by-nc/4.0/
 *
 */

package com.bill.minhadispensa.core.domain.util

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ConsoleLogger(private val moduleName: String) : AppLogger {

    private val appName = "Minha Despensa"

    override fun d(tag: String, message: String) {
        println(buildLogLine("D", tag, message))
    }

    override fun e(tag: String, message: String, error: Throwable?) {
        println(buildLogLine("E", tag, message))
        error?.printStackTrace()
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