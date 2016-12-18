package party.danyang.doodles;

import android.app.Application;

import party.danyang.doodles.CrashCatcher.CrashCatcher;

/**
 * Created by dream on 16-12-18.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashCatcher.ready().toCatch(this);
    }
}
