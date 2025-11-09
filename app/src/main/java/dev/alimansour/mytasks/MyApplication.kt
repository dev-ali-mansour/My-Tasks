package dev.alimansour.mytasks

import androidx.multidex.BuildConfig
import androidx.multidex.MultiDexApplication
import dev.alimansour.mytasks.di.module.AppModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module
import timber.log.Timber

class MyApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(AppModule().module)
        }

        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}
