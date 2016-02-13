package day.hack.com.gesunokiwami;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import io.socket.client.IO;
import io.socket.emitter.Emitter;
import io.socket.client.Socket;

public class SocketIOStreamer extends ContextSingletonBase<SocketIOStreamer> {
    private SocketIOEventCallback mCallback;
    private Socket mSocket;
    private Object[] values;
    private Handler mHandler;

    public void init(Context context) {
        super.init(context);
        mHandler = new Handler();
        try {
            mSocket = IO.socket(Config.SOCKET_SERVER_ROOT_URL);
            mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... arg0) {
                    Log.d(Config.TAG, "connect!!");
                    for(Object o : arg0){
                        Log.d(Config.TAG, "connect:" + o.toString());
                    }
                }
            });
            mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... arg0) {
                    Log.d(Config.TAG, "error!!");
                    for(Object o : arg0){
                        Log.d(Config.TAG, "error:" + o.toString());
                    }
                }
            });
            mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
                @Override
                public void call(Object... arg0) {
                    Log.d(Config.TAG, "timeout!!");
                    for(Object o : arg0){
                        Log.d(Config.TAG, "timeout:" + o.toString());
                    }
                }
            });
            mSocket.on("grab", new Emitter.Listener() {
                @Override
                public void call(Object... arg0) {
                    Log.d(Config.TAG, "grab!!");
                    for(Object o : arg0){
                        Log.d(Config.TAG, "grab:" + o.toString());
                    }
                }
            });
            mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... arg0) {
                    Log.d(Config.TAG, "discomment!!");
                    for (Object o : arg0) {
                        Log.d(Config.TAG, "discomment:" + o.toString());
                    }
                }
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void connect(){
      mSocket.connect();
    }

    public void emit(HashMap<String, Object> params){
        for(Map.Entry<String, Object> keyValue : params.entrySet()) {
            mSocket.emit(keyValue.getKey(), keyValue.getValue());
        }
        if(mCallback != null) mCallback.onEmit(params);
    }

    public void disConnect() {
        mSocket.disconnect();
    }

    public void release() {
        mCallback = null;
        disConnect();
    }

    public void setOnReceiveCallback(SocketIOEventCallback callback) {
        mCallback = callback;
    }

    public void removeOnReceiveCallback() {
        mCallback = null;
    }

    public interface SocketIOEventCallback{
        public void onCall(String receive);
        public void onEmit(HashMap<String, Object> emitted);
    }

    //デストラクタ
    @Override
    protected void finalize() throws Throwable {
        release();
        super.finalize();
    }
}
