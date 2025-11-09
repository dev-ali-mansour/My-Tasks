package dev.alimansour.mytasks.di.module

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [UseCaseModule::class])
@ComponentScan("eg.edu.cu.csds.icare")
class AppModule
