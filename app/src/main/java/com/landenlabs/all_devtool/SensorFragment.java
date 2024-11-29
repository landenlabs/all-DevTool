/*
 * Copyright (c) 2020 Dennis Lang (LanDen Labs) landenlabs@gmail.com
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * @author Dennis Lang
 * @see https://LanDenLabs.com/
 */

package com.landenlabs.all_devtool;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.androidplot.Plot;
import com.androidplot.Series;
import com.androidplot.util.Redrawer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.landenlabs.all_devtool.shortcuts.util.SoundMeter;
import com.landenlabs.all_devtool.shortcuts.util.Ui;
import com.landenlabs.all_devtool.shortcuts.util.Utils;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.androidplot.xy.BoundaryMode.AUTO;
import static com.androidplot.xy.BoundaryMode.FIXED;
import static com.androidplot.xy.BoundaryMode.GROW;
import static com.landenlabs.all_devtool.R.id.plot;

/**
 * Display "Sensor" information.
 *
 * @author Dennis Lang
 *
 */
@SuppressWarnings({"UnnecessaryLocalVariable", "PointlessArithmeticExpression"})
public class SensorFragment extends DevFragment
        implements
        SensorEventListener
        , View.OnLayoutChangeListener
        , AdapterView.OnItemSelectedListener {

    View m_rootView;
    TextView m_valueTv1;

    // ---- Plot
    private static final int HISTORY_SIZE = 300;            // number of points to plot in history
    private XYPlot m_plot = null;
    private SimpleXYSeries[] m_series;
    private Redrawer m_redrawer;

    // ----- Sensor selector
    private SensorManager m_sensorMgr;
    final List<Sensor> m_sensorList = new ArrayList<>();
    private final Map<String, SensorEvent> m_sensorEvents = new HashMap<>();
    final Map<String, String> m_plotValues = new HashMap<>();

    Spinner m_consoleSpinner;
    SoundMeter m_soundMeter;

    private static final String WIFI_STR = "WiFi Level";
    private static final String BATTERY_STR = "Battery";
    private static final String ORIENTATION_STR = "Orientation";
    private static final String LIGHT_STR = "Light";
    private static final String AUDIO_STR = "Audio Lvl";
    private static final String PROCESSES_STR = "#Processes";
    private static final String MEMORY_STR = "Free Memory";


    private static final String MAGNETOMETER_STR = "Magnet";
    private static final String PRESSURE_STR = "Pressure";
    private static final String GRAVITY_STR = "Gravity";
    // private static final String ACCELEROMETER_STR = "Accelerometer";
    // private static final String GYROSCOPE_STR = "Gyroscope";
    // private static final String STEP_COUNTER_STR = "Step Counter";

    private static final String[] SENSOR_NAMES =
            {
                    WIFI_STR, BATTERY_STR, AUDIO_STR, ORIENTATION_STR, LIGHT_STR, PROCESSES_STR, MEMORY_STR
                    // , ACCELEROMETER_STR
                    , MAGNETOMETER_STR
                    // , GYROSCOPE_STR
                    , PRESSURE_STR
                    , GRAVITY_STR
                    // , STEP_COUNTER_STR
            };
    String m_sensorName = WIFI_STR;

    /*
    final int ORIENTATION_AZ_SERIES = 0;
    final int ORIENTATION_PITCH_SERIES = 1;
    final int ORIENTATION_ROLL_SERIES = 2;
    final int SINGLE_SERIES = 0;
    */

    static SimpleXYSeries m_seriesChg;  // Shared by all graphs.

    static SimpleXYSeries m_seriesWifi;
    static SimpleXYSeries m_seriesBatteryPercent;
    static SimpleXYSeries m_seriesBatteryCharge;
    static SimpleXYSeries m_seriesBatteryDrain;
    static SimpleXYSeries m_seriesAudio;
    static SimpleXYSeries m_seriesAudioAvg;

    static SimpleXYSeries m_seriesOrientation_az;
    static SimpleXYSeries m_seriesOrientation_pitch;
    static SimpleXYSeries m_seriesOrientation_roll;
    static SimpleXYSeries m_seriesLight;
    static SimpleXYSeries m_seriesProcCnt;
    static SimpleXYSeries m_seriesFreeMem;

    // static SimpleXYSeries m_seriesAccelerometer;
    static SimpleXYSeries m_seriesMagnetometer;
    // static SimpleXYSeries m_seriesGyroscope;
    static SimpleXYSeries m_seriesPressure;
    static SimpleXYSeries m_seriesGravity;
    // static SimpleXYSeries m_seriesStepCounter;


    static final Format mLineFmt = new Format() {
        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, @NonNull FieldPosition pos) {
            // obj contains the raw Number value representing the position of the label being drawn.
            // customize the labeling however you want here:
            int iValue = Math.round(((Number) obj).floatValue());
            return toAppendTo.append(iValue);
        }

        @Override
        public Object parseObject(String source, @NonNull ParsePosition pos) {
            // unused
            return null;
        }
    };

    long m_maxAudio = 1000;
    final int MAX_LIGHT = 250;
    final int MAX_PROC_CNT = 200;

    long m_sleepMsec = 100;
    int m_menuSelected = R.id.sensor_menu_100_msec;
    String m_domainLabel = "1/100 sec";
    static final SparseIntArray m_menuIdToMsec;

    static {
        m_menuIdToMsec = new SparseIntArray();
        m_menuIdToMsec.put(R.id.sensor_menu_100_msec, 100);
        m_menuIdToMsec.put(R.id.sensor_menu_1_second, 1000 * 1);
        m_menuIdToMsec.put(R.id.sensor_menu_5_seconds, 1000 * 5);
        m_menuIdToMsec.put(R.id.sensor_menu_1_minute, 1000 * 60 * 1);
        m_menuIdToMsec.put(R.id.sensor_menu_10_minutes, 1000 * 60 * 10);
    }

    static final SparseArray<String> m_menuIdToLbl;

    static {
        m_menuIdToLbl = new SparseArray<>();
        m_menuIdToLbl.put(R.id.sensor_menu_100_msec, "1/100 Sec");
        m_menuIdToLbl.put(R.id.sensor_menu_1_second, "Seconds");
        m_menuIdToLbl.put(R.id.sensor_menu_5_seconds, "5 Sec");
        m_menuIdToLbl.put(R.id.sensor_menu_1_minute, "Minutes");
        m_menuIdToLbl.put(R.id.sensor_menu_10_minutes, "10 Min");
    }

    // ---- Timer
    private final Handler m_handler = new Handler();
    private final Runnable m_updateElapsedTimeTask = new Runnable() {
        public void run() {
            updatePlot();
            m_handler.postDelayed(this, m_sleepMsec);   // Re-execute after 1000 ms.
        }
    };

    // -----
    public static final String s_name = "Sensor";

    public SensorFragment() {
    }

    public static DevFragment create() {
        return new SensorFragment();
    }

    // ============================================================================================
    // DevFragment methods

    @Override
    public String getName() {
        return s_name;
    }

    @Override
    public List<Bitmap> getBitmaps(int maxHeight) {
        List<Bitmap> bitmapList = new ArrayList<>();
        bitmapList.add(Utils.grabScreen(getActivitySafe()));
        return bitmapList;
    }

    @Override
    public List<String> getListAsCsv() {
        return null;
    }

    @Override
    public void onSelected() {
        GlobalInfo.s_globalInfo.mainFragActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        GlobalInfo.s_globalInfo.mainFragActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //  getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        m_sensorMgr = getServiceSafe(Context.SENSOR_SERVICE);
        m_rootView = inflater.inflate(R.layout.sensor_tab, container, false);
        m_valueTv1 = Ui.viewById(m_rootView, R.id.sensor_value);

        m_sensorList.add(m_sensorMgr.getDefaultSensor(Sensor.TYPE_ORIENTATION));
        m_sensorList.add(m_sensorMgr.getDefaultSensor(Sensor.TYPE_LIGHT));

        m_soundMeter = null; //  = new SoundMeter();

        // createGraph(m_rootView, savedInstanceState);
        XYPlot plot = Ui.viewById(m_rootView, R.id.plot);
        m_redrawer = new Redrawer(
                Arrays.asList(new Plot[]{plot /* ,m_plot2 */}), 100, false);

        m_consoleSpinner = Ui.viewById(m_rootView, R.id.sensor_spinner);
        List<String> sensorNames = new ArrayList<>(Arrays.asList(SENSOR_NAMES));
        // TODO - add, if non-null add to menu, create series, etc
        m_sensorList.add(m_sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        m_sensorList.add(m_sensorMgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
        m_sensorList.add(m_sensorMgr.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        m_sensorList.add(m_sensorMgr.getDefaultSensor(Sensor.TYPE_PRESSURE));
        m_sensorList.add(m_sensorMgr.getDefaultSensor(Sensor.TYPE_GRAVITY));
        m_sensorList.add(m_sensorMgr.getDefaultSensor(Sensor.TYPE_STEP_COUNTER));

        //  ToDo - add multi-checkboxes instead of single.
        ArrayAdapter<String> spinnerArrayAdapter =
                new ArrayAdapter<>(m_rootView.getContext(), android.R.layout.simple_spinner_item,
                        sensorNames);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_consoleSpinner.setAdapter(spinnerArrayAdapter);
        m_consoleSpinner.addOnLayoutChangeListener(this);

        return m_rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        m_redrawer.start();
        m_handler.removeCallbacks(m_updateElapsedTimeTask);
        m_handler.postDelayed(m_updateElapsedTimeTask, 0);
        m_plot = null;
    }

    @Override
    public void onPause() {
        m_redrawer.pause();
        m_handler.removeCallbacks(m_updateElapsedTimeTask);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        // m_redrawer.finish();
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (m_soundMeter != null)
            m_soundMeter.start();
        for (Sensor sensor : m_sensorList) {
            if (sensor != null) {
                m_sensorMgr.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
            }
        }
    }

    @Override
    public void onStop() {
        detachSeries();
        if (m_soundMeter != null)
            m_soundMeter.stop();

        if (m_sensorMgr != null)
            m_sensorMgr.unregisterListener(this);
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        int itemId = item.getItemId();
        if (itemId == R.id.sensor_menu_100_msec
                || itemId == R.id.sensor_menu_1_second
                || itemId == R.id.sensor_menu_5_seconds
                || itemId == R.id.sensor_menu_1_minute
                || itemId == R.id.sensor_menu_10_minutes) {
            m_menuSelected = id;
            m_sleepMsec = m_menuIdToMsec.get(id);
            m_domainLabel = m_menuIdToLbl.get(id);
            m_plot.setDomainLabel(m_domainLabel);
        } else if (itemId == R.id.sensor_menu_data_clear) {
            clearSeries();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(m_menuSelected).setChecked(true);
    }

    SubMenu m_menu;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        m_menu = menu.addSubMenu("Sensor");
        inflater.inflate(R.menu.sensor_menu, m_menu);
    }

    // ============================================================================================
    // implement OnLayoutChangeListener
    @Override
    public void onLayoutChange(
            View v, int left, int top, int right,
            int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (v == m_consoleSpinner)
            m_consoleSpinner.setOnItemSelectedListener(this);
    }

    // ============================================================================================
    // implement onItemSelectedListener
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String itemStr = parent.getItemAtPosition(pos).toString();
        if (parent == m_consoleSpinner) {
            if (!itemStr.equals(m_sensorName)) {
                m_sensorName = itemStr;
                // createGraph(m_rootView, null);
                m_plot = null;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    // ============================================================================================
    // Permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (permissions[0].equals(Manifest.permission.RECORD_AUDIO)) {
                        m_soundMeter = new SoundMeter();
                        m_soundMeter.start();
                    }
                }
            }
        }
    }

    // ============================================================================================
    // Graph Stuff

    public void detachSeries() {
        if (m_plot != null) {
            m_plot.clear();
            /*
            for (Series series : m_plot.getSeriesSet()) {
                SimpleXYSeries xySeries = (SimpleXYSeries) series;
                m_plot.removeSeries(xySeries);
            }
            */
        }
    }

    private void clearSeries() {
        for (Series series : m_plot.getRegistry().getSeriesList()) {
            SimpleXYSeries xySeries = (SimpleXYSeries) series;
            xySeries.clear();
        }
    }

    public void createGraph(View rootView, Bundle savedInstanceState) {

        final int lineOnlyWidth = 6;
        final int lineFillWidth = 6;

        // setup the APR History plot:
        m_plot = Ui.viewById(rootView, plot);

        // Clear plot and change slope series so X-bounds is computed correctly.
        m_plot.clear();
        if (m_seriesChg != null) {
            m_seriesChg.clear();
        }

        detachSeries();

        m_sensorName = m_consoleSpinner.getSelectedItem().toString();
        m_plot.setTitle(m_sensorName);

        // Set the plot background (do once)
        XYGraphWidget graph = m_plot.getGraph();
        // Paint bg = wid.getBackgroundPaint();
        RectF rectF = graph.getGridRect();
        float width = (rectF != null) ? rectF.right : 250;
        float height = (rectF != null) ? rectF.height() : 250;
        Shader colorShader = new LinearGradient(0, 0, width, 0, Color.BLACK, Color.rgb(0, 64, 0), Shader.TileMode.MIRROR);
        graph.getGridBackgroundPaint().setShader(colorShader);

        m_plot.setRangeStep(StepMode.SUBDIVIDE, 10 + 1);
        //2 m_plot.setTicksPerRangeLabel(2);
        m_plot.setDomainLabel(m_domainLabel);


        final boolean LINE_MODE = false;
        final boolean FILL_MODE = true;

        switch (m_sensorName) {
            case ORIENTATION_STR:
                m_plot.setRangeBoundaries(-180, 359, FIXED);
                m_plot.setRangeLabel("Angle (Degs)");

                if (m_seriesOrientation_az == null) {
                    m_seriesOrientation_az = createSeries("Az.");
                    m_seriesOrientation_pitch = createSeries("Pitch");
                    m_seriesOrientation_roll = createSeries("Roll");
                }

                m_plot.addSeries(m_seriesOrientation_az,
                        makeFormatter(lineOnlyWidth, Color.rgb(40, 40, 255), Color.BLUE, 0,
                                height / 3, LINE_MODE));
                m_plot.addSeries(m_seriesOrientation_pitch,
                        makeFormatter(lineOnlyWidth, Color.rgb(40, 255, 40), Color.GREEN, 0,
                                height / 3, LINE_MODE));
                m_plot.addSeries(m_seriesOrientation_roll,
                        makeFormatter(lineOnlyWidth, Color.rgb(255, 40, 40), Color.RED, 0,
                                height / 3, LINE_MODE));
                break;
            case LIGHT_STR:

                if (m_seriesLight == null) {
                    m_seriesLight = createSeries("Lux");
                    m_seriesChg = createChgSeries();
                }

                m_plot.setRangeBoundaries(0, MAX_LIGHT, FIXED);
                m_plot.setRangeLabel("LUX");

                m_plot.addSeries(m_seriesLight,
                        makeFormatter(lineFillWidth, Color.rgb(128, 0, 0), Color.WHITE, 0, height,
                                FILL_MODE));
                m_plot.addSeries(m_seriesChg,
                        makeFormatter(4, Color.GREEN, Color.GREEN, 0, height, LINE_MODE));
                break;
            case AUDIO_STR:

                if (m_seriesAudio == null) {
                    m_seriesAudio = createSeries("Sound");
                    m_seriesAudioAvg = createSeries("Avg");
                    m_seriesChg = createChgSeries();
                }

                if (m_soundMeter == null && checkPermissions(Manifest.permission.RECORD_AUDIO)) {
                    m_soundMeter = new SoundMeter();
                    m_soundMeter.start();
                }

                m_plot.setRangeBoundaries(0, m_maxAudio, FIXED);
                m_plot.setRangeLabel("Sound");

                m_plot.addSeries(m_seriesAudio,
                        makeFormatter(2, Color.WHITE, Color.RED, 0, height, LINE_MODE));
                m_plot.addSeries(m_seriesAudioAvg,
                        makeFormatter(1, Color.BLUE, Color.BLUE, 0, height, FILL_MODE));
                m_plot.addSeries(m_seriesChg,
                        makeFormatter(4, Color.GREEN, Color.GREEN, 0, height, LINE_MODE));
                break;
            case PROCESSES_STR:
                if (m_seriesProcCnt == null) {
                    m_seriesProcCnt = createSeries("#Running");
                }

                m_plot.setRangeBoundaries(0, 10, AUTO);
                m_plot.setRangeLabel("#Process Running");

                m_plot.addSeries(m_seriesProcCnt,
                        makeFormatter(lineFillWidth, Color.YELLOW, Color.RED, 0, height,
                                FILL_MODE));
                m_plot.addSeries(m_seriesChg,
                        makeFormatter(4, Color.RED, Color.RED, 0, height, LINE_MODE));
                break;
            case MEMORY_STR:

                if (m_seriesFreeMem == null) {
                    m_seriesFreeMem = createSeries("Mem");
                    m_seriesChg = createChgSeries();
                }

                m_plot.setRangeBoundaries(0, 100, FIXED);
                m_plot.setRangeLabel("Memory Free %");

                m_plot.addSeries(m_seriesFreeMem,
                        makeFormatter(lineFillWidth, Color.RED, Color.YELLOW, 0, height,
                                FILL_MODE));
                m_plot.addSeries(m_seriesChg,
                        makeFormatter(4, Color.WHITE, Color.WHITE, 0, height, LINE_MODE));
                break;
            case BATTERY_STR:
                if (m_seriesBatteryPercent == null) {
                    m_seriesBatteryPercent = createSeries("Percent");
                    m_seriesBatteryCharge = createSeries("Charge");
                    m_seriesBatteryDrain = createSeries("Drain");
                }

                m_plot.setRangeBoundaries(0, 350, GROW);
                m_plot.setRangeStep(StepMode.INCREMENT_BY_VAL, 10);
                //2 m_plot.setTicksPerRangeLabel(2);
                m_plot.setRangeLabel("Battery % and Amps");

                m_plot.addSeries(m_seriesBatteryCharge,
                        makeFormatter(lineFillWidth, Color.argb(128, 100, 255, 100), Color.GREEN, 0,
                                height, FILL_MODE));
                m_plot.addSeries(m_seriesBatteryDrain,
                        makeFormatter(lineFillWidth, Color.argb(128, 255, 100, 100), Color.RED, 0,
                                height, FILL_MODE));
                m_plot.addSeries(m_seriesBatteryPercent,
                        makeFormatter(lineFillWidth, Color.rgb(255, 128, 0), Color.BLUE, 0, height,
                                LINE_MODE));

                break;
            case WIFI_STR:
                if (m_seriesWifi == null) {
                    m_seriesWifi = createSeries("WiFi");
                    m_seriesChg = createChgSeries();
                }

                m_plot.setRangeBoundaries(0, 110, FIXED);
                m_plot.setRangeStep(StepMode.SUBDIVIDE, 11 + 1);
                //2 m_plot.setTicksPerRangeLabel(2);

                m_plot.setRangeLabel("WiFi Signal%");
                m_plot.addSeries(m_seriesWifi,
                        makeFormatter(lineFillWidth, Color.BLUE, Color.WHITE, 0, height,
                                FILL_MODE));
                m_plot.addSeries(m_seriesChg,
                        makeFormatter(4, Color.RED, Color.RED, 0, height, LINE_MODE));
                break;
            case PRESSURE_STR:
                if (m_seriesPressure == null) {
                    m_seriesPressure = createSeries("Pressure");
                    m_seriesChg = createChgSeries();
                }

                //noinspection ConstantIfStatement,ConstantConditions
                if (true) {
                    m_plot.setRangeBoundaries(990, 1020, BoundaryMode.AUTO);
                } else {
                    // Lowest tornadoe 870mb,  strong high press 1030mb,  highest ever 1086mb.
                    m_plot.setRangeBoundaries(850, 1030, FIXED);
                    m_plot.setRangeStep(StepMode.SUBDIVIDE, 9 + 1);  // 1030 - 850 = 180 / 9 = 20
                    //2 m_plot.setTicksPerRangeLabel(1);
                }

                m_plot.setRangeLabel("Pressure mB");
                m_plot.addSeries(m_seriesPressure,
                        makeFormatter(lineFillWidth, Color.BLUE, Color.WHITE, 0, height,
                                FILL_MODE));
                m_plot.addSeries(m_seriesChg,
                        makeFormatter(4, Color.RED, Color.RED, 0, height, LINE_MODE));
                break;
            case MAGNETOMETER_STR:
                if (m_seriesMagnetometer == null) {
                    m_seriesMagnetometer = createSeries("Magnetometer");
                    m_seriesChg = createChgSeries();
                }

                m_plot.setRangeLabel("Magnetic");
                m_plot.setRangeBoundaries(-10, 10, AUTO);
                m_plot.addSeries(m_seriesMagnetometer,
                        makeFormatter(lineFillWidth, Color.BLUE, Color.WHITE, 0, height,
                                FILL_MODE));
                m_plot.addSeries(m_seriesChg,
                        makeFormatter(4, Color.RED, Color.RED, 0, height, LINE_MODE));
                break;
            case GRAVITY_STR:
                if (m_seriesGravity == null) {
                    m_seriesGravity = createSeries("Gravity");
                    m_seriesChg = createChgSeries();
                }

                m_plot.setRangeLabel("Gavity");
                m_plot.setRangeBoundaries(-10, 10, AUTO);
                m_plot.addSeries(m_seriesGravity,
                        makeFormatter(lineFillWidth, Color.BLUE, Color.WHITE, 0, height,
                                FILL_MODE));
                m_plot.addSeries(m_seriesChg,
                        makeFormatter(4, Color.RED, Color.RED, 0, height, LINE_MODE));
                break;
        }

        /*
            TODO  - add these graphs
             m_seriesAccelerometer;
             m_seriesGyroscope;
             m_seriesStepCounter;
         */

        m_plot.setDomainBoundaries(0, HISTORY_SIZE, FIXED);
        m_plot.setDomainStepMode(StepMode.INCREMENT_BY_VAL);
        m_plot.setDomainStepValue(HISTORY_SIZE / 10);

        /*
        m_plot.getDomainLabelWidget().pack();
        m_plot.getRangeLabelWidget().pack();

        m_plot.setRangeValueFormat(new DecimalFormat("#"));
        m_plot.setDomainValueFormat(new DecimalFormat("#"));
        */
        m_plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(mLineFmt);
        m_plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(mLineFmt);

        //     final PlotStatistics histStats = new PlotStatistics(1000, false);
        //     m_plot.addListener(histStats);

        // Software or Hardware acceleration
        //    m_plot.setLayerType(View.LAYER_TYPE_NONE, null);
    }

    @SuppressWarnings("SameParameterValue")
    LineAndPointFormatter makeFormatter(int width, int color1, int color2, float x2, float y2, boolean fill) {
        //2 LineAndPointFormatter formatter = new FastLineAndPointRenderer.Formatter(color1, null, color2, null);
        LineAndPointFormatter formatter = new LineAndPointFormatter(color1, null, color2, null);
        Paint linePaint = formatter.getLinePaint();
        linePaint.setStrokeWidth(width);
        linePaint.setShadowLayer(width, 1.0f, 1.0f, Color.argb(128, 0, 0, 0));

        if (fill) {
            Paint lineFill = new Paint();
            lineFill.setShader(new LinearGradient(0, 0, x2, y2, color2, color1, Shader.TileMode.MIRROR));
            lineFill.setAlpha(128 + 32);
            formatter.setFillPaint(lineFill);
            formatter.setLinePaint(linePaint);
        } else {
            formatter.setFillPaint(null);
            linePaint.setShader(new LinearGradient(0, 0, x2, y2, color2, color1, Shader.TileMode.MIRROR));
            formatter.setLinePaint(linePaint);
        }

        return formatter;
    }

    // ============================================================================================
    // SensorEventListener methods

    // Called whenever a new orSensor reading is taken.
    @Override
    public synchronized void onSensorChanged(SensorEvent sensorEvent) {
        m_sensorEvents.put(sensorEvent.sensor.getName(), sensorEvent);
    }

    @SuppressWarnings("SameParameterValue")
    private void add(SimpleXYSeries series, Number x, Number y) {
        if (series != null) {
            if (series.size() > HISTORY_SIZE)
                series.removeFirst();

            series.addLast(x, y);
        }
    }

    private void updatePlot() {

        if (m_plot == null) {
            createGraph(m_rootView, null);
            // Add  sensor sample to series.
        }
        final int NO_LIGHT = 0;
        final int MaxAgeMsec = 5000;

        for (String sensorName : m_sensorEvents.keySet()) {
            SensorEvent sensorEvent = m_sensorEvents.get(sensorName);
            if (sensorEvent != null) {
                long nowMsec = SystemClock.uptimeMillis(); //  not System.currentTimeMillis() nor  SystemClock.elapsedRealtime();
                long eventMsec = sensorEvent.timestamp / 1000000;
                long deltaMsec = nowMsec - eventMsec;

                if (isSensor(sensorName, ORIENTATION_STR) && m_seriesOrientation_az != null) {
                    add(m_seriesOrientation_az, null, sensorEvent.values[0]);
                    add(m_seriesOrientation_pitch, null, sensorEvent.values[1]);
                    add(m_seriesOrientation_roll, null, sensorEvent.values[2]);
                    m_plotValues.put(ORIENTATION_STR, String.format("%.0f, %.0f, %.0f",
                            sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]));
                } else if (isSensor(sensorName, LIGHT_STR) && m_seriesLight != null) {
                    int lux = (deltaMsec < MaxAgeMsec) ? (int) sensorEvent.values[0] : NO_LIGHT;
                    lux = Math.min(lux, MAX_LIGHT);
                    add(m_seriesLight, null, lux);

                    if (deltaMsec > MaxAgeMsec) {
                        m_plotValues.put(LIGHT_STR, "Light N/A");
                    } else {
                        m_plotValues.put(LIGHT_STR, String.format("Lux %d", lux));
                    }
                    if (m_sensorName.equals(LIGHT_STR)) {
                        setChangeSeries(m_seriesLight);
                        setRange(m_plot, m_seriesLight);
                    }
                } else if (isSensor(sensorName, PRESSURE_STR) && m_seriesPressure != null) {
                    float value = sensorEvent.values[0];
                    add(m_seriesPressure, null, value);
                    m_plotValues.put(PRESSURE_STR, String.valueOf(value));
                    if (m_sensorName.equals(PRESSURE_STR)) {
                        setChangeSeries(m_seriesPressure);
                        setRange(m_plot, m_seriesPressure);
                    }
                } else if (isSensor(sensorName, MAGNETOMETER_STR) && m_seriesMagnetometer != null) {
                    float value = sensorEvent.values[0];
                    add(m_seriesMagnetometer, null, value);
                    m_plotValues.put(MAGNETOMETER_STR, String.valueOf(value));
                    if (m_sensorName.equals(MAGNETOMETER_STR)) {
                        setChangeSeries(m_seriesMagnetometer);
                        setRange(m_plot, m_seriesMagnetometer);
                    }
                } else if (isSensor(sensorName, GRAVITY_STR) && m_seriesGravity != null) {
                    float value = sensorEvent.values[0] * 100;
                    add(m_seriesGravity, null, value);
                    m_plotValues.put(GRAVITY_STR, String.valueOf(value));
                    if (m_sensorName.equals(GRAVITY_STR)) {
                        setChangeSeries(m_seriesGravity);
                        setRange(m_plot, m_seriesGravity);
                    }
                } else {
                    Log.d("test", "sensor name=" + sensorName);
                }
                /* TODO
                        Gravity
                        Step counter
                 */
            }
        }

        if (m_soundMeter != null) {
            double dbValue = m_soundMeter.getAmplitude();
            double avgDb = 0;
            double maxDb = 0;
            add(m_seriesAudio, null, dbValue);

            int lastIdx = m_seriesAudioAvg.size();
            if (lastIdx == 0) {
                add(m_seriesAudioAvg, null, dbValue);
            } else {
                final int avgSpan = 20;
                int avgCnt = 0;
                for (int idx = Math.max(0, lastIdx - avgSpan); idx < lastIdx; idx++) {
                    avgDb += m_seriesAudio.getY(idx).doubleValue();
                    avgCnt++;
                }
                avgDb = avgDb / avgCnt;
                add(m_seriesAudioAvg, null, avgDb);

                final int maxSpan = 20;
                for (int idx = Math.max(0, lastIdx - maxSpan); idx < lastIdx; idx++) {
                    maxDb = Math.max(maxDb, m_seriesAudio.getY(idx).doubleValue());
                }

                if (m_sensorName.equals(AUDIO_STR)) {
                    setChangeSeries(m_seriesAudioAvg);
                }

                if (m_sensorName.equals(AUDIO_STR)) {
                    // Adjust graph Range to match data.
                    double totAvgDb = 0;
                    maxDb = 0;
                    for (int idx = 0; idx < lastIdx; idx++) {
                        totAvgDb += m_seriesAudio.getY(idx).doubleValue();
                        maxDb = Math.max(maxDb, m_seriesAudio.getY(idx).doubleValue());
                    }
                    totAvgDb /= lastIdx;
                    maxDb = (maxDb * 6 + totAvgDb * 4) / 10;
                    final long AUDIO_STEP = 100;
                    if (maxDb > m_maxAudio) {
                        m_maxAudio = (long) ((maxDb + AUDIO_STEP - 1) / AUDIO_STEP) * AUDIO_STEP;
                        m_plot.setRangeBoundaries(0, m_maxAudio, FIXED);
                    } else if (maxDb <= m_maxAudio * 0.5) {
                        long maxAudio = Math.max((long) (maxDb / AUDIO_STEP) * AUDIO_STEP, AUDIO_STEP);
                        m_maxAudio = maxAudio;
                        m_plot.setRangeBoundaries(0, m_maxAudio, FIXED);
                    }
                }
            }
            m_plotValues.put(AUDIO_STR, String.format("%.0f Avg:%.0f", dbValue, avgDb));
        }

        WifiManager wifiMgr = (WifiManager) getContextSafe().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiMgr != null && wifiMgr.isWifiEnabled() && wifiMgr.getDhcpInfo() != null && m_seriesWifi != null) {
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            int numberOfLevels = 10;
            int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels + 1);
            int wifiLevel = 100 * level / numberOfLevels;
            add(m_seriesWifi, null, wifiLevel);
            m_plotValues.put(WIFI_STR, String.valueOf(wifiLevel));
            if (m_sensorName.equals(WIFI_STR)) {
                setChangeSeries(m_seriesWifi);
            }
        }

        Intent batteryIntent = getActivitySafe().getApplicationContext()
                .registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryIntent != null && m_seriesBatteryPercent != null) {
            int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);

            // Bundle[{misc_event=0, technology=Li-ion, icon-small=17303838, max_charging_voltage=0,
            // health=2, max_charging_current=0, online=4, status=2, plugged=2, present=true,
            // pogo_plugged=0, capacity=280000, seq=19, charge_counter=2552000, level=83,
            // scale=100, temperature=264, current_now=593, voltage=4196, charge_type=1,
            // self_discharging=false, hv_charger=false, power_sharing=false, invalid_charger=0}]

            int batteryLevel = level * 100 / scale;
            add(m_seriesBatteryPercent, null, batteryLevel);
            m_plotValues.put(BATTERY_STR, String.valueOf(batteryLevel));
            if (m_sensorName.equals(BATTERY_STR)) {
                setChangeSeries(m_seriesBatteryPercent);
            }

            final int NO_VALUE = 0;
            double currentAmps = batteryIntent.getIntExtra("current_now", NO_VALUE);
            // int voltage = batteryIntent.getIntExtra("voltage", NO_VALUE);
            // int chargeCounter = batteryIntent.getIntExtra("charge_counter", -1);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                BatteryManager batteryManager = getServiceSafe(Context.BATTERY_SERVICE);
                Integer currentNow =
                        batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);

                if (currentAmps == NO_VALUE && currentNow != null) {
                    currentAmps = currentNow / 1e3;
                }
            }

            if (currentAmps != NO_VALUE) {
                if (currentAmps > 0) {
                    add(m_seriesBatteryCharge, null, currentAmps);
                    add(m_seriesBatteryDrain, null, 0);
                } else {
                    add(m_seriesBatteryCharge, null, 0);
                    add(m_seriesBatteryDrain, null, -currentAmps);
                }
                if (Math.abs(currentAmps) > 200) {
                    m_plot.setRangeStep(StepMode.INCREMENT_BY_VAL, 50);
                }
                m_plotValues.put(BATTERY_STR, String.format("Level=%d%% Amps=%.1f", batteryLevel, currentAmps));
            }
            /*
                    listStr.put("Current (now)", String.format("%.3f mA", currentNow/1e3));

                    // Are we charging / charged?

                    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    float batteryPct = level / (float)scale;
                    listStr.put("Percent", String.format("%.1f%%", batteryPct*100));

                    int voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
                    listStr.put("Voltage", String.format("%d mV", voltage));
            */

        }

        ActivityManager actMgr = getServiceSafe(Context.ACTIVITY_SERVICE);
        if (m_seriesProcCnt != null) {
            try {
                int processCnt = actMgr.getRunningAppProcesses().size();
                add(m_seriesProcCnt, null, Math.min(processCnt, MAX_PROC_CNT));
                m_plotValues.put(PROCESSES_STR, String.valueOf(processCnt));

                if (m_sensorName.equals(PROCESSES_STR)) {
                    // m_plot.setRangeBoundaries(0, Math.max(10, processCnt), BoundaryMode.GROW);
                    setChangeSeries(m_seriesProcCnt);
                    setRange(m_plot, m_seriesProcCnt);
                }
            } catch (Exception ignore) {
            }
        }

        if (m_seriesFreeMem != null) {
            //   Total Memory API >= 16
            try {
                ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                ActivityManager activityManager = getServiceSafe(Context.ACTIVITY_SERVICE);
                activityManager.getMemoryInfo(mi);
                // long heapUsing = Debug.getNativeHeapSize();
                long freeMem = 100 * mi.availMem / mi.totalMem;
                add(m_seriesFreeMem, null, freeMem);
                m_plotValues.put(MEMORY_STR, String.valueOf(freeMem));
                if (m_sensorName.equals(MEMORY_STR)) {
                    setChangeSeries(m_seriesFreeMem);
                    setRange(m_plot, m_seriesFreeMem);
                }
            } catch (Exception ignore) {
            }
        }

        if (m_plotValues.get(m_sensorName) != null)
            m_valueTv1.setText(m_plotValues.get(m_sensorName));
    }

    private boolean isSensor(String sensor, String name) {
        return sensor.toLowerCase().contains(name.toLowerCase());
    }

    public void setChangeSeries(SimpleXYSeries dataSeries) {
        if (m_seriesChg != null && dataSeries.size() > 2) {
            m_seriesChg.clear();
            m_seriesChg.addLast(0, dataSeries.getY(0));
            int lastIdx = dataSeries.size() - 1;
            m_seriesChg.addLast(lastIdx, dataSeries.getY(lastIdx));
            // Log.d("fxx", dataSeries.getTitle() + " " + lastIdx + " min=" + dataSeries.getY(0) + " max=" + dataSeries.getY(lastIdx));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Not interested in this event
    }

    // ============================================================================================
    // Internal methods

    SimpleXYSeries createSeries(String name) {
        SimpleXYSeries series = new SimpleXYSeries(name);
        series.useImplicitXVals();
        return series;
    }
    SimpleXYSeries createChgSeries() {
        SimpleXYSeries series = new SimpleXYSeries("Chg");
        return series;
    }

    void setRange(XYPlot plot, XYSeries series) {
    }
}