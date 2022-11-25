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
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RegistActivity extends AppCompatActivity {

    HttpUtils httpUtil;

//    String url_commit = "http://59.110.10.33:9999/regist";//提交注册的后端路径
//    String url_active = "http://59.110.10.33:9999/active";//激活的后端路径

    String url_commit = "http://192.168.43.232:9999/regist";//提交注册的后端路径
    String url_active = "http://192.168.43.232:9999/active";//激活的后端路径

    OkHttpClient mOkHttpClient = new OkHttpClient();


    String return_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);

        Button commit_btn = findViewById(R.id.commit);
        Button active_btn = findViewById(R.id.active);

        commit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread() {
                    @Override
                    public void run() {

                        //发送输入的账号密码到后端

                        EditText id_text = findViewById(R.id.id);
                        EditText email_text = findViewById(R.id.email);
                        EditText name_text = findViewById(R.id.name);
                        EditText passwd_text = findViewById(R.id.passwd);

                        String id = id_text.getText().toString();
                        String email = email_text.getText().toString();
                        String name = name_text.getText().toString();
                        String passwd = passwd_text.getText().toString();


                        HttpUrl.Builder httpBuilder = HttpUrl.parse(url_commit).newBuilder();
                        httpBuilder.addQueryParameter("rid", id);
                        httpBuilder.addQueryParameter("remail", email);
                        httpBuilder.addQueryParameter("rname", name);
                        httpBuilder.addQueryParameter("rpass", passwd);


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


                            if (id.equals("") && email.equals("") && name.equals("") && passwd.equals("")) {
                                return_code = "4500";
                            }

                            if (return_code.equals("4500")) {
                                Looper.prepare();
                                Toast.makeText(RegistActivity.this, "填写信息不能为空!👀", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else if (return_code.equals("4501")) {
                                Looper.prepare();
                                Toast.makeText(RegistActivity.this, "该ID已被占用👀", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else if (return_code.equals("6501")) {
                                Looper.prepare();
                                Toast.makeText(RegistActivity.this, "已发送激活码到您的邮箱！📫", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else {
                                Looper.prepare();
                                Toast.makeText(RegistActivity.this, "发生系统错误", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });


        active_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread() {
                    @Override
                    public void run() {

                        //发送输入的账号密码到后端

                        EditText acode_text = findViewById(R.id.active_code);


                        String acode = acode_text.getText().toString();


                        HttpUrl.Builder httpBuilder = HttpUrl.parse(url_active).newBuilder();
                        httpBuilder.addQueryParameter("acode", acode);


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

                            if (return_code.equals("4502")) {
                                Looper.prepare();
                                Toast.makeText(RegistActivity.this, "激活码错误!👀", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            } else if (return_code.equals("6566")) {
                                Looper.prepare();
                                Toast.makeText(RegistActivity.this, "注册成功!👍", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegistActivity.this, MainActivity.class);
                                startActivity(intent);
                                Looper.loop();
                            } else {
                                Looper.prepare();
                                Toast.makeText(RegistActivity.this, "发生系统错误", Toast.LENGTH_SHORT).show();
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