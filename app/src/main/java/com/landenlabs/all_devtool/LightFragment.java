package com.landenlabs.all_devtool;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import com.landenlabs.all_devtool.util.LLog;
import com.landenlabs.all_devtool.util.Ui;

import java.util.List;

/**
 * Light page
 *
 * TODO -
 *     o Add color wheel and brightness for screen color
 *     o Add strobe patterns to LED torch light and screen
 *     o Add screen alt color flash and kaloscope pattern, half screen blocks alternating. 
 */

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class LightFragment extends DevFragment
        implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private final LLog m_log = LLog.DBG;
    public static final String s_name = "Light";
    private View m_rootView;


    private ToggleButton m_cameraLightTb;

    // SDK < 23
    private Camera m_camera;

    // SDK >= 23
    private String mCameraId;
    CameraManager mCameraManager;

    private ToggleButton m_screenBrightnessTB;

    public LightFragment() {
    }

    public static DevFragment create() {
        return new LightFragment();
    }


    // ============================================================================================
    // DevFragment methods

    @Override
    public String getName() {
        return s_name;
    }

    @Override
    public List<Bitmap> getBitmaps(int maxHeight) {
        return null;
    }

    @Override
    public List<String> getListAsCsv() {
        return null;
    }

    // ============================================================================================
    // Fragment methods

    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
            m_log.e("onViewCreated savedInstanceState");

        setHasOptionsMenu(true);

        m_rootView = inflater.inflate(R.layout.light_tab, container, false);

        m_cameraLightTb =  Ui.viewById(m_rootView, R.id.lightCameraOnTb);
        if (!getContextSafe().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            m_cameraLightTb.setVisibility(View.GONE);
        }
        m_cameraLightTb.setOnClickListener(this);


        Ui.viewById(m_rootView, R.id.screenSettings).setVisibility(checkSystemWritePermission() ? View.VISIBLE : View.GONE);
        Ui.viewById(m_rootView, R.id.screenOnTB).setOnClickListener(this);
        m_screenBrightnessTB = Ui.viewById(m_rootView, R.id.screenBrightnesTB);
        m_screenBrightnessTB.setOnClickListener(this);

        SeekBar m_screenBrightnessSB = Ui.viewById(m_rootView, R.id.lightCameraSB);
        m_screenBrightnessSB.setOnSeekBarChangeListener(this);

        return m_rootView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                // checkPermissions(Manifest.permission.CAMERA);
                m_camera = Camera.open();
            } catch (Exception ex) {
                m_log.e(ex.getMessage());
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            if (Build.VERSION.SDK_INT < 23) {
                if (checkPermissions(Manifest.permission.CAMERA)) {
                    m_camera = Camera.open();
                }
            } else {
                if (!checkSystemWritePermission()) {
                    requestSystemWritePermission();
                }
                mCameraManager = getServiceSafe(Context.CAMERA_SERVICE);
                String[] cameraIds = mCameraManager.getCameraIdList();
                mCameraId = cameraIds[0];
            }
        } catch (Exception ex) {
            m_log.e(ex.getMessage());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (m_camera != null) {
            m_camera.release();
            m_camera = null;
        }
    }

    // ============================================================================================
    // View OnClickListener

    @Override
    public void onClick(View view) {

        int id = view.getId();
        switch (id) {
            case R.id.lightCameraOnTb:
                updateCameraLight(m_cameraLightTb.isChecked());
                break;

            case R.id.screenBrightnesTB:
                setScreenBrightnessAuto(((ToggleButton)view).isChecked());
                break;
            case R.id.screenOnTB:
                setScreenOnAuto(((ToggleButton)view).isChecked()) ;
                break;
        }
    }

    // ============================================================================================
    // SeekBar OnSeekBarChangeListener

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int id = seekBar.getId();
        switch (id) {
            case R.id.screenBrightnessSB:
                setScreenBrightness(progress / 100.0f);
                break;
            case R.id.lightCameraSB:
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    // ============================================================================================
    // Utility functions

    private void turnFlashlightOn() {
        if (!camera23(true)) {
            Camera.Parameters parameters = m_camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            // parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            m_camera.setParameters(parameters);
            m_camera.startPreview();
        }
    }

    private void turnFlashlightOff() {
        if (!camera23(false)) {
            Camera.Parameters parameters = m_camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            m_camera.setParameters(parameters);
            m_camera.stopPreview();
        }
    }

    private boolean camera23(boolean flashOn) {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                mCameraManager.setTorchMode(mCameraId, flashOn);
                return true;
            }
        } catch (Exception ex) {
            m_log.e(ex.getMessage());
        }
        return false;
    }

    void updateCameraLight(boolean lightOn) {

        try {
            if (lightOn) {
                turnFlashlightOn();
            } else {
                turnFlashlightOff();
            }

        } catch (Exception ex) {
            m_log.e(ex.getMessage());
        }
    }

    // =============================================================================================


    private void setScreenOnAuto(boolean onAuto) {
        if (onAuto) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void setScreenBrightness(float brightnessPercent) {
        if (checkSystemWritePermission()) {
            // setScreenBrightnessAuto(false);
            m_screenBrightnessTB.setChecked(false);
            Window window = getWindow();
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.screenBrightness = brightnessPercent;  // 0 .. 1
            window.setAttributes(layoutParams);
        }
    }

    private void setScreenBrightnessAuto(boolean onAuto) {
        if (checkSystemWritePermission()) {
            Settings.System.putInt(getActivitySafe().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                    onAuto ? Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                            :  Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        }
    }

    private boolean checkSystemWritePermission() {
        boolean retVal = true;
        if (Build.VERSION.SDK_INT >= 23) {
            retVal = Settings.System.canWrite(getActivitySafe());
        }
        return retVal;
    }

    private void requestSystemWritePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + getActivitySafe().getPackageName()));
            getActivitySafe().startActivity(intent);
        }
    }


}
