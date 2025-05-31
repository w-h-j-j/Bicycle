package com.example.bicycle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.widget.Toast;

import com.mapfactor.sdk.ActivationListener;
import com.mapfactor.sdk.InitListener;
import com.mapfactor.sdk.MpfcEngine;
import com.mapfactor.sdk.data.AppData;
import com.mapfactor.sdk.data.AppDataListListener;
import com.mapfactor.sdk.data.AppDataManager;
import com.mapfactor.sdk.data.DataDownloadListener;
import com.mapfactor.sdk.map.MapDataProvider;
import com.mapfactor.sdk.map.MapFragment;
import com.mapfactor.sdk.map.MapRenderer;
import com.mapfactor.sdk.utils.Localization;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity地图";

    private MapFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager manager = getSupportFragmentManager();
        fragment = (MapFragment) manager.findFragmentById(R.id.your_fragment_container_id);
        fragment.onCreate(savedInstanceState);

        String sdkDataPath = getExternalFilesDir(null).toString();
        MapRenderer.RendererName hardware = MapRenderer.RendererName.HARDWARE;
        MapDataProvider.ProviderName osm = MapDataProvider.ProviderName.OSM;
        Localization.Language englishUs = Localization.Language.ENGLISH_US;

        MpfcEngine.getInstance().init(this, sdkDataPath, osm, hardware, englishUs, new InitListener() {
            @Override
            public void onLocationPermissionNotGranted() {
                System.out.printf(TAG, "onLocationPermissionNotGranted");
            }

            @Override
            public void onEngineInitStatusChanged(@NonNull MpfcEngine.InitStatus initStatus) {
                System.out.printf(TAG, "onEngineInitStatusChanged");
            }

            @Override
            public void onEngineInitFinished(@NonNull MpfcEngine.InitResult initResult) {
                System.out.printf(TAG, "onEngineInitFinished");
                if (initResult == MpfcEngine.InitResult.SUCCESS) {
                    //HURAYYY, do whatever you want, e.g.
                    //initActiveVehicle()
                    //getAllAvailableVehicles()
                    //setCurrentLanguage()

                    activateDevice();
                } else if (initResult == MpfcEngine.InitResult.FAILED_DEVICE_NOT_ACTIVATED) {
                    //oops, activate device
                    activateDevice();

                }
            }
        });
    }

    private void activateDevice() {
        MpfcEngine.getInstance().activateDevice("7ZCTV-T22C1-52DSC-PQK6E-DUCEH", new ActivationListener() {
            @Override
            public void onActivationFinished(@NonNull MpfcEngine.ActivationResult activationResult) {
                System.out.printf(TAG, "onEngineInitFinished");
                aa();
            }
        });
    }

    private void aa(){
        List<String> list = new ArrayList<>();
        AppDataManager appDataManagerModule = MpfcEngine.getInstance().getAppDataManagerModule();
        appDataManagerModule.getAvailableAppData(new AppDataListListener() {
            @Override
            public void onAppDataListReady(@NonNull List<AppData> list) {
                Toast.makeText(MainActivity.this, "List<AppData> size: " +list.size(), Toast.LENGTH_SHORT).show();
                for (AppData a : list) {
                    System.out.printf("========:    ID: " + a.getId() + "   Name: " + a.getName());
                }
            }

            @Override
            public void onAppDataListFailedToGet(@NonNull AppDataManager.AppDataListErrorCode appDataListErrorCode) {

            }
        });
        appDataManagerModule.downloadAppData(list, new DataDownloadListener() {
            @Override
            public void onDownloadProgress(@NonNull String s, @NonNull String s1, int i, int i1, long l, long l1, long l2, long l3) {
                System.out.printf(TAG, "onDownloadProgress");
            }

            @Override
            public void onDownloadFinished() {
                System.out.printf(TAG, "onDownloadFinished");
            }

            @Override
            public void onDownloadFailed(@NonNull AppDataManager.DownloadErrorCode downloadErrorCode) {
                System.out.printf(TAG, "onDownloadFailed");
            }

            @Override
            public void onEngineRestarted() {
                System.out.printf(TAG, "onEngineRestarted");
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        fragment.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        fragment.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fragment.onDestroy();
    }
}