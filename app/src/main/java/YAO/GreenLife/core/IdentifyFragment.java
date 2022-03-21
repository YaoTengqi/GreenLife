package YAO.GreenLife.core;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.greenlife.R;

import java.io.File;
import java.io.IOException;


public class IdentifyFragment extends Fragment {

    public static final int TAKE_PHOTO = 1;//声明一个请求码，用于识别返回的结果
    public static final int CHOOSE_PHOTO = 2;
    private Uri imageUri;
    public String identifyCode = null;
    public String checkCode = null;
    private final String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "output_image.jpg";
    String imagePath = null;
    ImageView imageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = View.inflate(getActivity(), R.layout.fragment_identify, null);
        imageView = view.findViewById(R.id.details_iv);


        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Button garbage_btn = (Button) getActivity().findViewById(R.id.garbage_btn);
        Button insect_btn = (Button) getActivity().findViewById(R.id.insect_btn);
        Button check_btn = (Button) getActivity().findViewById(R.id.check_btn);


        garbage_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("LXX", "success");
                Intent intent = new Intent();
                identifyCode = "garbage";
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity())
                        .setTitle("请选择图片上传方式")
                        .setPositiveButton("拍照", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestPermission_Camera();
                            }
                        })

                        .setNegativeButton("相册", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Intent.ACTION_PICK, null);
                                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                intent.putExtra("requestCode", "2");
                                startActivityForResult(intent, 2);

                            }
                        });

                alertDialog.show();
            }

        });

        insect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("LXX", "success");
                Intent intent = new Intent();
                identifyCode = "insect";
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity())
                        .setTitle("请选择图片上传方式")
                        .setPositiveButton("拍照", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestPermission_Camera();

                            }
                        })

                        .setNegativeButton("相册", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Intent.ACTION_PICK, null);
                                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                startActivityForResult(intent, 2);

                            }
                        });

                alertDialog.show();
            }

        });

        check_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra("identifyCode", identifyCode);
                intent.putExtra("checkCode", "check");
                startActivity(intent);
            }

        });
    }

    //动态请求权限
    private void requestPermission_Camera() {

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
        } else {
            //调用
            requestCamera();
        }
    }


    private void requestCamera() {
        File outputImage = new File(filePath);
                /*
                创建一个File文件对象，用于存放摄像头拍下的图片，我们把这个图片命名为output_image.jpg
                并把它存放在应用关联缓存目录下，调用getExternalCacheDir()可以得到这个目录，为什么要
                用关联缓存目录呢？由于android6.0开始，读写sd卡列为了危险权限，使用的时候必须要有权限，
                应用关联目录则可以跳过这一步
                 */
        try//判断图片是否存在，存在则删除在创建，不存在则直接创建
        {
            if (!outputImage.getParentFile().exists()) {
                outputImage.getParentFile().mkdirs();
            }
            if (outputImage.exists()) {
                outputImage.delete();
            }

            outputImage.createNewFile();

            if (Build.VERSION.SDK_INT >= 24) {
                imageUri = FileProvider.getUriForFile(getActivity(),
                        "com.example.greenlife.fileprovider", outputImage);
            } else {
                imageUri = Uri.fromFile(outputImage);
            }
            //使用隐示的Intent，系统会找到与它对应的活动，即调用摄像头，并把它存储
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            intent.putExtra("requestCode", "1");
            startActivityForResult(intent, TAKE_PHOTO);
            //调用会返回结果的开启方式，返回成功的话，则把它显示出来
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            // 从相册返回的数据
            if (data != null) {
                // 得到图片的全路径
                Uri uri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                    Intent intent = new Intent(getActivity(), DetailsActivity.class);
                    intent.putExtra("imgUri", uri.toString());
                    intent.putExtra("identifyCode", identifyCode);
                    intent.putExtra("requestCode", "Album_Photo");
                    startActivity(intent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}