package dev.alimansour.mytasks.di.module

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
@ComponentScan("dev.alimansour.mytasks")
class AppModule {
    @Single
    fun provideCoroutineDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
