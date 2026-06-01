package com.example.melofy.di

import com.example.melofy.data.api.ITunesSearchApi
import com.example.melofy.data.api.JamendoApi
import com.example.melofy.data.api.GroqApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val ITUNES_BASE_URL = "https://itunes.apple.com/"
    private const val JAMENDO_BASE_URL = "https://api.jamendo.com/v3.0/"
    private const val GROQ_BASE_URL = "https://api.groq.com/openai/v1/"

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("iTunes")
    fun provideITunesRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ITUNES_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    @Named("Jamendo")
    fun provideJamendoRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(JAMENDO_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideITunesSearchApi(@Named("iTunes") retrofit: Retrofit): ITunesSearchApi {
        return retrofit.create(ITunesSearchApi::class.java)
    }

    @Provides
    @Singleton
    fun provideJamendoApi(@Named("Jamendo") retrofit: Retrofit): JamendoApi {
        return retrofit.create(JamendoApi::class.java)
    }

    @Provides
    @Singleton
    @Named("Groq")
    fun provideGroqRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(GROQ_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideGroqApi(@Named("Groq") retrofit: Retrofit): GroqApi {
        return retrofit.create(GroqApi::class.java)
    }
}
