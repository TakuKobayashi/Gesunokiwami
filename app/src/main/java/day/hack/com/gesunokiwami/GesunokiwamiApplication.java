package day.hack.com.gesunokiwami;

import android.app.Application;

public class GesunokiwamiApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		SocketIOStreamer.getInstance(SocketIOStreamer.class).init(this);
		SocketIOStreamer.getInstance(SocketIOStreamer.class).connect();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}
}
