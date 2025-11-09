package dev.alimansour.mytasks.di.module

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [UseCaseModule::class])
@ComponentScan("dev.alimansour.mytasks")
class AppModule
