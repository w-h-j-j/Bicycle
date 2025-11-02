package com.example.bicycle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.modules.ArchiveFileFactory;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.OfflineTileProvider;
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;

import java.io.File;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMapView = findViewById(R.id.map_view);

        initMap();
    }

    private void initMap(){
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        //built in zoom controls
        mMapView.setBuiltInZoomControls(true);
        //needed for pinch zooms
        mMapView.setMultiTouchControls(true);
        //scales tiles to the current screen's DPI, helps with readability of labels
        mMapView.setTilesScaledToDpi(true);
        //初始化离线地图
        mapViewOffline();
        mMapView.setMaxZoomLevel(19.0);
        mMapView.setMinZoomLevel(3.0);
        //设置地图级别
        mMapView.getController().setZoom(6.0);
    }

    public void mapViewOffline2() {
        //在线source
        OnlineTileSourceBase tianDiTuImgTileSource = new OnlineTileSourceBase("Tian Di Tu Img", 1, 22, 256, "",
                new String[]{"https://webrd02.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8"}) {
            @Override
            public String getTileURLString(final long pMapTileIndex) {
                return getBaseUrl() + "&X=" + MapTileIndex.getX(pMapTileIndex) + "&Y=" + MapTileIndex.getY(pMapTileIndex)
                        + "&Z=" + MapTileIndex.getZoom(pMapTileIndex);
            }
        };
        String filePath = "";//离线地图瓦片包位置
        File zipFile = new File(filePath);
        try {
            OfflineTileProvider tileProvider = new OfflineTileProvider(new SimpleRegisterReceiver(this), new File[]{zipFile });
            mMapView.setTileProvider(tileProvider);

            String source = "";
            IArchiveFile[] archives = tileProvider.getArchives();
            if (archives.length > 0) {
                Set<String> tileSources = archives[0].getTileSources();
                if (!tileSources.isEmpty()) {
                    source = tileSources.iterator().next();
                    mMapView.setTileSource(FileBasedTileSource.getSource(source));
                } else {
                    //离线设置失败，则设置在线
                    mMapView.setTileSource(tianDiTuImgTileSource);
                }
            } else {
                //离线设置失败，则设置在线
                mMapView.setTileSource(tianDiTuImgTileSource);
            }
            mMapView.invalidate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 加载离线地图
     */
    public void mapViewOffline() {
        String strFilepath = Environment.getExternalStorageDirectory().getPath() + "/osmdroid/jiang.sqlite";
        File exitFile = new File(strFilepath);
        String fileName = exitFile.getName();
        if (!exitFile.exists()) {
            mMapView.setTileSource(TileSourceFactory.MAPNIK);
        } else {
            fileName = fileName.substring(fileName.lastIndexOf(".") + 1);
            if (ArchiveFileFactory.isFileExtensionRegistered(fileName)) {
                try {
                    OfflineTileProvider tileProvider = new OfflineTileProvider(new SimpleRegisterReceiver(this), new File[]{exitFile});
                    mMapView.setTileProvider(tileProvider);

                    String source = "";
                    IArchiveFile[] archives = tileProvider.getArchives();
                    if (archives.length > 0) {
                        Set<String> tileSources = archives[0].getTileSources();
                        if (!tileSources.isEmpty()) {
                            source = tileSources.iterator().next();
                            mMapView.setTileSource(FileBasedTileSource.getSource(source));
                        } else {
                            mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
                        }

                    } else
                        mMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
                    Toast.makeText(this, "Using " + exitFile.getAbsolutePath() + " " + source, Toast.LENGTH_LONG).show();
                    mMapView.invalidate();
                    return;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                Toast.makeText(this, " did not have any files I can open! Try using MOBAC", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, " dir not found!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onClick(View v) {}

}