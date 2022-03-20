package com.childsafe.Notification;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APISERVICE {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAn-kvz6Q:APA91bElezQDyQrHZrcMqCz6PHvF5pfoaVPhP300H9C_sGyzE79ZHPtB42yR2MUN4hOFDOdkc4-tDpDPH4ZzmzfGoYFWJbp5rB-bd4jOEFR5VEnqCXBoBgTLnDSE3To057fsRMwzm4S1"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
