package com.example.bicycle.model;

/**
 * 主界面网格菜单项数据模型
 */
public class GridItemInfo {

    private int iconRes;            // 图标资源
    private String title;           // 标题文字
    private int bgColorRes;         // 图标圆形背景颜色资源
    private String fragmentClassName; // 目标 Fragment 全限定类名（反射跳转用）

    public GridItemInfo(int iconRes, String title, int bgColorRes, String fragmentClassName) {
        this.iconRes = iconRes;
        this.title = title;
        this.bgColorRes = bgColorRes;
        this.fragmentClassName = fragmentClassName;
    }

    public int getIconRes() {
        return iconRes;
    }

    public String getTitle() {
        return title;
    }

    public int getBgColorRes() {
        return bgColorRes;
    }

    public String getFragmentClassName() {
        return fragmentClassName;
    }
}
