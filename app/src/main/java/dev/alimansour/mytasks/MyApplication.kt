package dev.alimansour.mytasks

import android.app.Application
import dev.alimansour.mytasks.di.module.AppModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.annotation.KoinApplication
import org.koin.plugin.module.dsl.startKoin
import timber.log.Timber

@KoinApplication(modules = [AppModule::class])
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin<MyApplication> {
            androidLogger()
            androidContext(this@MyApplication)
        }

        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}
