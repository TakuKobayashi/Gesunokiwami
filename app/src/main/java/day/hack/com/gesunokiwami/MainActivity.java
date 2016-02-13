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

import java.util.HashMap;

import jp.live2d.sample.LAppDefine;
import jp.live2d.sample.LAppLive2DManager;
import jp.live2d.sample.LAppView;
import jp.live2d.utils.android.FileManager;
import jp.live2d.utils.android.SoundManager;

public class MainActivity extends Activity {

    //  Live2Dの管理
    private LAppLive2DManager live2DMgr ;
    static private Activity instance;

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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // GUIを初期化
        setupGUI();
        FileManager.init(this.getApplicationContext());
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
        ClickListener listener = new ClickListener();
        iBtn.setOnClickListener(listener);
    }


    // ボタンを押した時のイベント
    class ClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Toast.makeText(getApplicationContext(), "change model", Toast.LENGTH_SHORT).show();
            live2DMgr.changeModel();//Live2D Event
        }
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
}
