package com.zhaoyuntao.dnf;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.WordSimple;
import com.zhaoyuntao.androidutils.camera.CameraView;
import com.zhaoyuntao.androidutils.component.ZButton;
import com.zhaoyuntao.androidutils.tools.S;
import com.zhaoyuntao.androidutils.tools.T;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private CameraView cameraView;
    private ZButton zButton;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OCR.getInstance(this).initAccessToken(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) { // 调用成功，返回AccessToken对象
                String token = result.getAccessToken();
                S.s("OCR.getToken:" + token);
            }

            @Override
            public void onError(OCRError error) { // 调用失败，返回OCRError子类SDKError对象
                S.e("OCR.getToken error:" + error.getMessage());
            }
        }, getApplicationContext());


        zButton = findViewById(R.id.button);
        zButton.setOnClickListener(new View.OnClickListener() {
            boolean pause;

            @Override
            public void onClick(View v) {
                if (pause) {
                    pause = false;
                    cameraView.resume();
                } else {
                    pause = true;
                    cameraView.pause();
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(new File(getFilesDir(), "abc.jpg"));
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    start();
                }
            }
        });
        cameraView = findViewById(R.id.camera);
        cameraView.setAngle(90);
        cameraView.setCallBack(new CameraView.CallBack() {
            @Override
            public void whenGotBitmap(Bitmap bitmapTmp, byte[] data) {
                bitmap = bitmapTmp;
            }

            @Override
            public void whenCameraCreated() {

            }

            @Override
            public void whenNoPermission() {
                T.t(MainActivity.this, "no camera permission");
            }
        });
    }

    private void start() {
        cameraView.pause();
        if (bitmap == null) {
            T.t(this, "no picture");
            return;
        }
        // 通用文字识别参数设置
        GeneralBasicParams param = new GeneralBasicParams();
        param.setDetectDirection(true);
        param.setImageFile(new File(getFilesDir(), "abc.jpg"));

        // 调用通用文字识别服务
        OCR.getInstance(this).recognizeAccurateBasic(param, new OnResultListener<GeneralResult>() {
            @Override
            public void onResult(GeneralResult result) {
                StringBuilder stringBuilder = new StringBuilder();
                // 调用成功，返回GeneralResult对象
                for (WordSimple wordSimple : result.getWordList()) {
                    // wordSimple不包含位置信息
                    WordSimple word = wordSimple;
                    stringBuilder.append(word.getWords());
                    stringBuilder.append("\n");
                }
                // json格式返回字符串
                S.s("result:" + stringBuilder.toString());
            }

            @Override
            public void onError(OCRError error) {
                // 调用失败，返回OCRError对象
                S.e("OCR.recognizeAccurateBasic error:" + error.getMessage());
            }
        });
    }
}
