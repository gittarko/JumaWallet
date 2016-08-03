package com.juma.walletpay.web;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import java.io.File;

/**
 * Created by Administrator on 2016/6/7 0007.
 */
public class HDWebChromeClient extends WebChromeClient {

    public final static int FILECHOOSER_RESULTCODE = 1;
    private Activity activity;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mFilePathCallback;

    public HDWebChromeClient(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        callback.invoke(origin, true, false);
        super.onGeolocationPermissionsShowPrompt(origin, callback);
    }

    // For Android 3.0+
    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        if (mUploadMessage != null) {
            mUploadMessage.onReceiveValue(null);
        }
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        activity.startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
    }

    // For Android 3.0+
    public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
        if (mUploadMessage != null) {
            mUploadMessage.onReceiveValue(null);
        }
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        String type = TextUtils.isEmpty(acceptType) ? "*/*" : acceptType;
        i.setType(type);
        activity.startActivityForResult(Intent.createChooser(i, "File Chooser"),
                FILECHOOSER_RESULTCODE);
    }

    // For Android 4.1
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        if (mUploadMessage != null) {
            mUploadMessage.onReceiveValue(null);
        }
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        String type = TextUtils.isEmpty(acceptType) ? "*/*" : acceptType;
        i.setType(type);
        activity.startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
    }


    //Android 5.0+
    @Override
    @SuppressLint("NewApi")
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        if (mFilePathCallback != null) {
            mFilePathCallback.onReceiveValue(null);
        }
        mFilePathCallback = filePathCallback;

        Intent intent = null;

        if (fileChooserParams != null && fileChooserParams.getAcceptTypes() != null
                && fileChooserParams.getAcceptTypes().length > 0) {
            String acceptType = fileChooserParams.getAcceptTypes()[0];
            if (acceptType != null && acceptType.contains("image")) {
                intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType(acceptType);
            } else {
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(acceptType);
            }
        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
        }

        try {
            activity.startActivityForResult(intent, FILECHOOSER_RESULTCODE);
        } catch (Exception e) {
            activity.startActivityForResult(Intent.createChooser(intent, "File Chooser"), FILECHOOSER_RESULTCODE);
        }
        return true;
    }

//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == FILECHOOSER_RESULTCODE) {
//            Uri result = data == null || resultCode != Activity.RESULT_OK ? null : data.getData();
//            if (mUploadMessage != null) {
//                if (result == null) {
//                    mUploadMessage.onReceiveValue(null);
//                    mUploadMessage = null;
//                    return;
//                }
//                String path = FileUtils.getPath(activity, result);
//                if (TextUtils.isEmpty(path)) {
//                    mUploadMessage.onReceiveValue(null);
//                    mUploadMessage = null;
//                    return;
//                }
//                Uri uri = Uri.fromFile(new File(path));
//                mUploadMessage.onReceiveValue(uri);
//                mUploadMessage = null;
//            }
//            if (mFilePathCallback != null) {
//                if (result == null) {
//                    mFilePathCallback.onReceiveValue(null);
//                    mFilePathCallback = null;
//                    return;
//                }
//                String path = FileUtils.getPath(activity, result);
//                if (TextUtils.isEmpty(path)) {
//                    mFilePathCallback.onReceiveValue(null);
//                    mFilePathCallback = null;
//                    return;
//                }
//                Uri uri = Uri.fromFile(new File(path));
//                mFilePathCallback.onReceiveValue(new Uri[]{uri});
//                mFilePathCallback = null;
//            }
//        }
//    }
}
