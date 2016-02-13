package day.hack.com.gesunokiwami;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;

import jp.live2d.sample.LAppDefine;
import jp.live2d.sample.LAppLive2DManager;
import jp.live2d.sample.LAppView;
import jp.live2d.utils.android.FileManager;
import jp.live2d.utils.android.SoundManager;

public class MainActivity extends CardboardActivity{
    private static int REQUEST_CODE = 1;

    //  Live2Dの管理
    private LAppLive2DManager live2DMgr ;
    static private Activity instance;
    private int grabCount = 0;
    private int currentState = Config.RESET_STATE;
    private Camera mCamera;

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
        SocketIOStreamer.getInstance(SocketIOStreamer.class).setOnReceiveCallback(new SocketIOStreamer.SocketIOEventCallback() {
            @Override
            public void onCall(String receive) {
                Log.d(Config.TAG, "recieve:" + receive);
                try {
                    motionCountUp(Integer.parseInt(receive));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return;
                }
            }

            @Override
            public void onEmit(HashMap<String, Object> emitted) {
                Log.d(Config.TAG, "emitted:" + emitted);
            }
        });
        requestPermission();
    }

    private void requestPermission(){
        if(Build.VERSION.SDK_INT >= 23) {
            ArrayList<String> permissions = ApplicationHelper.getSettingPermissions(this);
            boolean isRequestPermission = false;
            for(String permission : permissions){
                if(!ApplicationHelper.hasSelfPermission(this, permission)){
                    isRequestPermission = true;
                    break;
                }
            }
            if(isRequestPermission) {
                requestPermissions(permissions.toArray(new String[0]), REQUEST_CODE);
            }
        }
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
        layout.setBackgroundColor(Color.argb(0,0,0,0));

        // モデル切り替えボタン
        ImageButton iBtn = (ImageButton)findViewById(R.id.imageButton1);
        iBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                motionCountUp(currentState == Config.OPEN_STATE ? Config.GRAB_STATE : Config.OPEN_STATE);
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

    private void resetState() {
        currentState = Config.RESET_STATE;
        grabCount = 0;
    }

    // TODO モーション再生中はカウントアップさせない
    private void motionCountUp(int state){
        if(state == Config.RESET_STATE) {
            resetState();
            return;
        }
        boolean changeState = (currentState == Config.OPEN_STATE && state == Config.GRAB_STATE) || (currentState == Config.GRAB_STATE && state == Config.OPEN_STATE);
        if(changeState) {
            ++grabCount;
        }
        currentState = state;
        if(!changeState) {
            return;
        }
        int motionNum = grabCount / 2;
        Config.GrabState[] states = Config.GrabState.values();
        if(motionNum >= states.length){
            motionNum = states.length - 1;
        }
        if(grabCount % 2 == 0) return;
        live2DMgr.getModel(0).startMotion(states[motionNum].toString(), 0, LAppDefine.PRIORITY_FORCE);
    }

    /*
     * Activityを再開したときのイベント。
     */
    @Override
    protected void onResume()
    {
        live2DMgr.onResume() ;
        setupCamera();
        super.onResume();
    }

    /*
     * Activityを停止したときのイベント。
     */
    @Override
    protected void onPause()
    {
        live2DMgr.onPause() ;
        releaseCamera();
        super.onPause();
    }

    private void setupCamera(){
        try {
            mCamera = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            return;
        }
        SurfaceView preview = (SurfaceView) findViewById(R.id.camera_preview);
        SurfaceHolder holder = preview.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                releaseCamera();
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (mCamera != null) {
                    try {
                        mCamera.setPreviewDisplay(holder);
                    } catch (IOException exception) {
                        releaseCamera();
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (mCamera != null) {
                    try {
                        mCamera.setPreviewDisplay(holder);
                    } catch (IOException exception) {
                        releaseCamera();
                    }
                }
            }
        });
        if(Build.VERSION.SDK_INT < 11){
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCamera.stopPreview();
        //今回はフロントカメラのみなのでCameraIdは0のみ使う
        mCamera.setDisplayOrientation(ApplicationHelper.getCameraDisplayOrientation(this, 0));
        mCamera.startPreview();
    }


    private void releaseCamera(){
        if (mCamera != null){
            mCamera.cancelAutoFocus();
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        };
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
