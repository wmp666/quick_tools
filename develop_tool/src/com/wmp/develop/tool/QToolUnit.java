package com.wmp.develop.tool;

import javax.swing.*;
import java.net.URL;

public abstract class QToolUnit {

    public final String name = setName();
    public final Icon icon = setIcon();
    protected abstract String setName();
    protected Icon setIcon(){
        URL resource = getClass().getResource("icon.png");
        if (resource == null) return null;
        return new ImageIcon(resource);
    }

    public abstract void showTool();

}
