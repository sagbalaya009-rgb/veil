package com.veil.app.di

import android.content.Context
import androidx.room.Room
import com.veil.app.data.ProtectionEventDao
import com.veil.app.data.VeilDatabase
import com.veil.app.data.VeilRepository
import com.veil.app.data.VeilSettingsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VeilDatabase =
        Room.databaseBuilder(context, VeilDatabase::class.java, "veil.db")
            .addMigrations(VeilDatabase.MIGRATION_1_2)
            .addMigrations(VeilDatabase.MIGRATION_2_3)
            .addMigrations(VeilDatabase.MIGRATION_3_4)
            .build()

    @Provides
    fun provideSettingsDao(database: VeilDatabase): VeilSettingsDao = database.settingsDao()

    @Provides
    fun provideProtectionEventDao(database: VeilDatabase): ProtectionEventDao = database.eventDao()

    @Provides
    @Singleton
    fun provideRepository(
        settingsDao: VeilSettingsDao,
        eventDao: ProtectionEventDao,
    ): VeilRepository = VeilRepository(settingsDao, eventDao)
}
