package com.wmp.develop.tool;

import javax.swing.*;
import java.net.URL;

public abstract class QToolUnit {

    public final String name = setName();
    public final Icon icon = setIcon();
    public final String version = setVersion();
    protected abstract String setName();

    protected Icon setIcon(){
        URL resource = getClass().getResource("icon.png");
        if (resource == null) return null;
        return new ImageIcon(resource);
    }
    protected abstract String setVersion();

    public abstract void showSetsDialog();

    /**
     * 展示工具
     * @deprecated 建议使用新方法{@code getToolDialog}
     * @see #getToolDialog()
     */
    @Deprecated
    public void showTool(){}

    public abstract JDialog getToolDialog();
}
