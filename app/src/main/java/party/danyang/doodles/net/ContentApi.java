package party.danyang.doodles.net;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * Created by dream on 16-8-9.
 */
public class ContentApi {
    public static final String BASE_URL = "https://www.google.com/";

    public interface ContentsApi {
        @GET("doodles/{name}")
        Observable<String> load(@Path("name") String name, @QueryMap Map<String, String> query);
    }

    public static final Observable<String> load(String name) {
        return load(name, "zh-CN");
    }

    public static final Observable<String> load(String name, String language) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        Map<String, String> query = new HashMap<>();
        query.put("hl", language);
        return retrofit.create(ContentsApi.class).load(name, query);
    }
}
