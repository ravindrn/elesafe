package com.elephant.safety.api;

import android.content.Context;
import com.elephant.safety.utils.SharedPrefManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    // FOR ANDROID EMULATOR
    private static final String BASE_URL = "http://10.0.2.2:8080/";

    private static Retrofit retrofit = null;
    private static Context appContext;

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            if (appContext == null) {
                throw new IllegalStateException("ApiClient not initialized");
            }

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
                        okhttp3.Request original = chain.request();
                        okhttp3.Request.Builder requestBuilder = original.newBuilder();

                        // Add authorization header if user is logged in
                        if (SharedPrefManager.getInstance(appContext).isLoggedIn()) {
                            String token = SharedPrefManager.getInstance(appContext).getToken();
                            if (token != null && !token.isEmpty()) {
                                // Make sure token doesn't already have "Bearer " prefix
                                String authToken = token.startsWith("Bearer ") ? token : "Bearer " + token;
                                requestBuilder.addHeader("Authorization", authToken);
                                android.util.Log.d("ApiClient", "Adding Authorization header: " + authToken.substring(0, Math.min(30, authToken.length())) + "...");
                            } else {
                                android.util.Log.w("ApiClient", "User is logged in but token is null or empty");
                            }
                        } else {
                            android.util.Log.d("ApiClient", "User not logged in, no auth header");
                        }

                        return chain.proceed(requestBuilder.build());
                    })
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getClient(Context context) {
        if (appContext == null && context != null) {
            appContext = context.getApplicationContext();
        }
        return getClient();
    }
}