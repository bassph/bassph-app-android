package org.projectbass.bass.inject

import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import org.projectbass.bass.post.api.RestAPI
import com.readystatesoftware.chuck.ChuckInterceptor
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.schedulers.Schedulers



/**
 * YOYO HOLDINGS

 * @author A-Ar Andrew Concepcion
 * * *
 * *
 * @since 14/12/2016
 */
@Module
class RestModule {

    @Provides
    @PerApplication
    fun provideOkHttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
                .addInterceptor(ChuckInterceptor(context))
                .addInterceptor(StethoInterceptor())
                .build()
    }

    @Provides
    @PerApplication
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .client(client)
                .baseUrl(RestAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
    }

    @Provides
    @PerApplication
    fun provideRestApi(retrofit: Retrofit): RestAPI {
        return retrofit.create(RestAPI::class.java)
    }
}