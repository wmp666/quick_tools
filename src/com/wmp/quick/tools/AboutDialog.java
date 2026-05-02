package com.wmp.quick.tools;

import javax.swing.*;
import java.awt.*;

public class AboutDialog extends JDialog {
    public AboutDialog() {
        super();
        setTitle("关于");
        setLayout(new BorderLayout());
        JTextArea label = new JTextArea("""
                快捷工具
                作者：wmp666
                项目地址：https://github.com/wmp666/quick_tools
                要了解更多自行打开项目仓库了解"""
        );
        label.setEditable(false);
        label.setFont(UIManager.getFont("h2.font"));
        add(label, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }
}
