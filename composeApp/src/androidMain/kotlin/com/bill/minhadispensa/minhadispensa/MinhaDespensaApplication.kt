package com.bill.minhadispensa.minhadispensa

import android.app.Application
import com.bill.minhadispensa.minhadispensa.di.initKoin
import org.koin.android.ext.koin.androidContext

class MinhaDespensaApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@MinhaDespensaApplication)
        }
    }
}