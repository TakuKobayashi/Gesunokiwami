package day.hack.com.gesunokiwami;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.FieldOfView;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;

import jp.live2d.sample.LAppDefine;
import jp.live2d.sample.LAppLive2DManager;
import jp.live2d.sample.LAppView;
import jp.live2d.utils.android.FileManager;
import jp.live2d.utils.android.SoundManager;

public class MainActivity extends CardboardActivity{

    //  Live2Dの管理
    private LAppLive2DManager live2DMgr ;
    static private Activity instance;
    private Config.GrabState currentGrabState = Config.GrabState.none;

    public MainActivity( )
    {
        instance=this;
        if(LAppDefine.DEBUG_LOG)
        {
            Log.d("", "==============================================\n") ;
            Log.d( "", "   Live2D Sample  \n" ) ;
            Log.d( "", "==============================================\n" ) ;
        }

        SoundManager.init(this);
        live2DMgr = new LAppLive2DManager() ;
    }


    static public void exit()
    {
        SoundManager.release();
        instance.finish();
        SocketIOStreamer.getInstance(SocketIOStreamer.class).release();
    }


    /*
     * Activityが作成されたときのイベント
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // GUIを初期化
        setupGUI();

        FileManager.init(this.getApplicationContext());
        /*
        SocketIOStreamer.getInstance(SocketIOStreamer.class).setOnReceiveCallback(new SocketIOStreamer.SocketIOEventCallback() {
            @Override
            public void onCall(String receive) {
                Log.d(Config.TAG, "recieve:" + receive);
            }

            @Override
            public void onEmit(HashMap<String, Object> emitted) {
                Log.d(Config.TAG, "emitted:" + emitted);
            }
        });
        */
    }


    /*
     * GUIの初期化
     * activity_main.xmlからViewを作成し、そこにLive2Dを配置する
     */
    void setupGUI()
    {
        setContentView(R.layout.activity_main);

        //  Viewの初期化
        LAppView view = live2DMgr.createView(this) ;

        // activity_main.xmlにLive2DのViewをレイアウトする
        FrameLayout layout=(FrameLayout) findViewById(R.id.live2DLayout);
        layout.addView(view, 0, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // モデル切り替えボタン
        ImageButton iBtn = (ImageButton)findViewById(R.id.imageButton1);
        iBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //if(currentGrabState != Config.GrabState.none) return;
                Config.GrabState[] states = Config.GrabState.values();
                int order = currentGrabState.ordinal();
                int newOrder = (order + 1) % (states.length - 1);
                currentGrabState = states[newOrder];
                live2DMgr.getModel(0).startMotion(currentGrabState.toString(), 0, LAppDefine.PRIORITY_FORCE);
                //live2DMgr.changeModel();//Live2D Event
            }
        });

        //以下、カードボードbの設定
        /*
        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        Log.d(Config.TAG, "view: " + cardboardView);
        // Associate a CardboardView.StereoRenderer with cardboardView.
        cardboardView.setRenderer(this);
        // Associate the cardboardView with this activity.
        setCardboardView(cardboardView);
        */
    }

    /*
     * Activityを再開したときのイベント。
     */
    @Override
    protected void onResume()
    {
        //live2DMgr.onResume() ;
        super.onResume();
    }


    /*
     * Activityを停止したときのイベント。
     */
    @Override
    protected void onPause()
    {
        live2DMgr.onPause() ;
        super.onPause();
    }

    //以下、StereoRendererで必要なもの
    /*
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        Log.d(Config.TAG, "VRNewFrame!!");
    }

    @Override
    public void onDrawEye(Eye eye) {
        Viewport viewport = eye.getViewport();
        Log.d(Config.TAG, "VREye!! x:" + viewport.x + " y:" + viewport.y + " w:" + viewport.width + " h:" + viewport.height);
        FieldOfView fv = eye.getFov();
        Log.d(Config.TAG, "VREye!! l:" + fv.getLeft() + " t:" + fv.getTop() + " r:" + fv.getRight() + " b:" + fv.getBottom());
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
        Log.d(Config.TAG, "VRFinishFrame!! x:" + viewport.x + " y:" + viewport.y + " w:" + viewport.width + " h:" + viewport.height);
    }

    @Override
    public void onSurfaceChanged(int i, int i1) {
        Log.d(Config.TAG, "VRChanged!! i:" + i + " i1:" + i1);
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        Log.d(Config.TAG, "VRCreated!!" + eglConfig);
    }

    @Override
    public void onRendererShutdown() {
        Log.d(Config.TAG, "VRshutdown!!");
    }
    */
}
