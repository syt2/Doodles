package party.danyang.doodles.net;

import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import party.danyang.doodles.entity.Doodle;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * Created by dream on 16-12-12.
 */

public class DoodleApi {
    private static final String BASE_URL = "https://www.google.com/";

    public interface DoodlesApi {
        @GET("doodles/json/{year}/{month}")
        Observable<List<Doodle>> load(@Path("year") String year, @Path("month") String month, @QueryMap Map<String, String> query);
    }

    public static final Observable<List<Doodle>> load(String year, String month, String language) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        Map<String, String> query = new HashMap<>();
        query.put("hl", language);
        return retrofit.create(DoodlesApi.class).load(year, month, query);
    }

    public static final Observable<List<Doodle>> load(String year, String month) {
        return load(year, month, "zh_CN");
    }
}
