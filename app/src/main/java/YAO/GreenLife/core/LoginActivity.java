package YAO.GreenLife.core;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.greenlife.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import YAO.GreenLife.Utils.HttpUtils;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    HttpUtils httpUtil;

    String url = "http://59.110.10.33:9999/login";//此处改为自己的路径

    OkHttpClient mOkHttpClient = new OkHttpClient();

    Request request = new Request.Builder().url(url).build();

    Call call = mOkHttpClient.newCall(request);

    String return_code;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button login_btn = findViewById(R.id.login);


        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /**
                 * @Description: 检测是否登录成功(账号存在且账号密码匹配)
                 * @Param: [savedInstanceState]
                 * @return: void
                 * @Author: YAO
                 * @Date: 2022/3/6
                 */
                new Thread() {
                    @Override
                    public void run() {

                        //发送输入的账号密码到后端

                        EditText logname_text = findViewById(R.id.logname);
                        EditText logpass_text = findViewById(R.id.logpass);

                        String logname = logname_text.getText().toString();
                        String logpass = logpass_text.getText().toString();


                        HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();
                        httpBuilder.addQueryParameter("logname", logname);
                        httpBuilder.addQueryParameter("logpass", logpass);


                        try {
                            Request request = new Request.Builder()
                                    .url(httpBuilder.build())
                                    .method("post", new FormBody.Builder().build())
                                    .build();
                            OkHttpClient client = new OkHttpClient.Builder()
                                    .readTimeout(20, TimeUnit.SECONDS)
                                    .build();
                            Response response = client.newCall(request).execute();

                            String result = response.body().string();
                            try {
                                JSONObject jsonObject = new JSONObject(result);
                                return_code = jsonObject.getString("code");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            if (logname.equals("") && logpass.equals("")) {
                                return_code = "4401";
                            }

                            if (return_code.equals("4401")) {
                                Looper.prepare();
                                Toast.makeText(LoginActivity.this, "账号或密码不能为空!", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else if (return_code.equals("4402")) {
                                Looper.prepare();
                                Toast.makeText(LoginActivity.this, "账号或密码错误,请重新输入!", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else if (return_code.equals("6666")) {
                                Looper.prepare();
                                Toast.makeText(LoginActivity.this, "登陆成功!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                Looper.loop();
                            } else {
                                Looper.prepare();
                                Toast.makeText(LoginActivity.this, "发生系统错误", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
    }
}