package com.wmp.quick.tools;

import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.wmp.develop.tool.QToolUnit;
import com.wmp.quick.tools.apptools.GetPath;
import org.tinylog.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;

public class MainFrame extends JDialog{
    private static final ArrayList<MainFrame> oldClass = new ArrayList<>();
    public static final ArrayList<QToolUnit> tools = new ArrayList<>();


    public MainFrame() throws URISyntaxException {
        oldClass.forEach(MainFrame::dispose);
        oldClass.clear();
        new MainFrame(0);
        new MainFrame(1);
    }

    /**
     *
     * @param style 0-右 1-左
     */
    private MainFrame(int style) throws URISyntaxException {

        oldClass.add(this);

        initFrame();

        loadTools();


        JButton openButton = new JButton(style == 0 ? "<" : ">");
        openButton.setBorder(null);
        openButton.setFont(UIManager.getFont("h0.font"));
        openButton.addActionListener(e -> showDialog(style));
        this.add(openButton, BorderLayout.CENTER);


        this.pack();
        if (style == 0) {
            this.setShape(new RoundRectangle2D.Double(0, 0, this.getWidth() + 15, this.getHeight(), 15, 15));
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            this.setLocation(screenSize.width - this.getWidth(), screenSize.height * 3 / 5);
        } else {
            this.setShape(new RoundRectangle2D.Double(-15, 0, this.getWidth() + 15, this.getHeight(), 15, 15));
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            this.setLocation(0, screenSize.height * 3 / 5);
        }
        this.setVisible(true);
    }

    private static void loadTools() throws URISyntaxException {
        Logger.info("正在加载外部导入的工具...");

        tools.clear();

        File file = new File(GetPath.getAppPath(GetPath.SOURCE_FILE_PATH), "tools");
        file.mkdirs();
        File[] toolsFile = file.listFiles(path -> path.getName().endsWith(".jar"));
        if (toolsFile != null) {
            Logger.info("工具列表：{null}", Arrays.asList(toolsFile));
            for (File toolFile : toolsFile) {
                try {
                    URLClassLoader classLoader = new URLClassLoader(new URL[]{toolFile.toURI().toURL()});
                    Class<?> toolClass = classLoader.loadClass("com.wmp.tool.Main");

                    QToolUnit tool = (QToolUnit) toolClass.getDeclaredConstructor().newInstance();
                    tools.add(tool);
                } catch (Exception e) {
                    Logger.error(e, "加载工具{}失败", toolFile.getName());
                    JOptionPane.showMessageDialog(null, "加载工具" + toolFile.getName() + "失败\n" + e, "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }else Logger.warn("没有找到工具");
    }

    /**
     *
     * @param style 0-右 1-左
     */
    public static void showDialog(int style) {

        JPopupMenu popupMenu = new JPopupMenu();

        tools.forEach(tool -> {
            JMenuItem menuItem = new JMenuItem(tool.name, tool.icon);
            menuItem.setFont(UIManager.getFont("h0.font"));
            menuItem.addActionListener(_ -> {
                Logger.info("正在打开工具：{null}", tool.name);
                tool.showTool();
            });
            popupMenu.add(menuItem);
        });

        JMenu moreMenu = new JMenu("更多");
        moreMenu.setFont(UIManager.getFont("h0.font"));

        JMenuItem controlToolsItem = new JMenuItem("工具管理");
        controlToolsItem.setFont(UIManager.getFont("h1.font"));
        controlToolsItem.addActionListener(e -> new ControlDialog());
        moreMenu.add(controlToolsItem);

        JMenuItem refreshMenuItem = new JMenuItem("刷新");
        refreshMenuItem.setFont(UIManager.getFont("h1.font"));
        refreshMenuItem.addActionListener(e -> {
            try {
                loadTools();
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        });
        moreMenu.add(refreshMenuItem);

        JMenuItem aboutMenuItem = new JMenuItem("关于");
        aboutMenuItem.setFont(UIManager.getFont("h1.font"));
        aboutMenuItem.addActionListener(e -> new AboutDialog());
        moreMenu.add(aboutMenuItem);

        popupMenu.add(moreMenu);

        if(style == 0) popupMenu.show(oldClass.get(0), -popupMenu.getWidth() - oldClass.get(0).getWidth(), 0);
        else if (style == 1)popupMenu.show(oldClass.get(1), oldClass.get(1).getWidth(), 0);
    }

    private void initFrame() {
        this.setTitle("快捷工具");
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        this.setUndecorated(true);
        this.setOpacity(0.7f);
        this.setAlwaysOnTop(true);
        this.setSize(500, 500);
        this.setLayout(new BorderLayout());

        this.addWindowListener( new WindowAdapter(){
            @Override
            public void windowOpened(WindowEvent e) {
                MainFrame.this.repaint();
            }
        });
    }


}
