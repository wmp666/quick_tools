package com.wmp.quick.tools;

import com.formdev.flatlaf.themes.FlatMacLightLaf;
import org.tinylog.Logger;

import javax.swing.*;
import java.net.URISyntaxException;

public class Main {
    static void main(){
        Logger.info("启动快捷工具");

        FlatMacLightLaf.setup();

        SwingUtilities.invokeLater(()-> {
            try {
                new MainFrame();
            } catch (URISyntaxException e) {
                Logger.error(e, "启动快捷工具失败");
            }
        });
    }
}
