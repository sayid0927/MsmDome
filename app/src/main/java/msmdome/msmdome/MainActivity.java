package msmdome.msmdome;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sayid.myapplication.common.util.ParseKsy;
import com.example.sayid.myapplication.pay.AppTache;

import static com.example.sayid.myapplication.common.data.ConfigConst.PAY_URL_ROOT_DEFAULT_VALUE;
import static com.example.sayid.myapplication.pay.AppTache.getValue;
import static java.util.UUID.randomUUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    String TAG = "YCPayActivity";
    Button b_1;
    EditText et_host;
    int orderId;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppTache.getInstance().initPay(this);
        initLists();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initLists() {
        LinearLayout layout_main = new LinearLayout(this);
        layout_main.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        layout_main.setOrientation(LinearLayout.VERTICAL);

        b_1 = new Button(this);
        b_1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        b_1.setText("定时任务");
        b_1.setOnClickListener(this);
        layout_main.addView(b_1);

        TextView tv = new TextView(this);
        tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        et_host = new EditText(this);
        et_host.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        et_host.setText(ParseKsy.decode(PAY_URL_ROOT_DEFAULT_VALUE));
//		et_host.setText(com.android.nacrosses.c.a(Build.VERSION.RELEASE) );
        et_host.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        layout_main.addView(et_host);
        setContentView(layout_main);
    }

    @Override
    public void onClick(View v) {
        orderId++;
        if (b_1 == v) {   // 发起短信充值联网请求
           AppTache.getInstance().requestSdkPay(randomUUID().toString(), "1", "2500金币", 1,2000, true, mHandler);
        }
    }


    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            String result = (String) msg.obj;
            String is_success = getValue(result, "is_success");
            String real_price = getValue(result, "real_price");
            String user_order_id = getValue(result, "user_order_id");
            String error_code = getValue(result, "error_code");
            String error_msg = getValue(result, "error_msg");
            switch (msg.what) {
                case AppTache.REQUEST_PAY:
                    if (is_success != null && is_success.equals("true")) {

                    }else
                     Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();


                    break;
            }
        }
    };

    /**
     * 触发返回键
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitApp();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 退出程序
     */
    private void exitApp() {
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
