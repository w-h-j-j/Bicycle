package com.example.bicycle.model;

/**
 * 桌面应用模型
 */
public class DesktopApp {
    private String name;        // 应用名称
    private int iconResId;      // 图标资源 ID
    private String packageName; // 包名（可选）

    public DesktopApp(String name, int iconResId) {
        this.name = name;
        this.iconResId = iconResId;
    }

    public DesktopApp(String name, int iconResId, String packageName) {
        this.name = name;
        this.iconResId = iconResId;
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}