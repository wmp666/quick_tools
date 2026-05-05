package com.wmp.quick.tools;

import com.formdev.flatlaf.*;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
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
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.prefs.Preferences;

public class MainFrame extends JDialog{
    private static final ArrayList<MainFrame> oldClass = new ArrayList<>();
    public static final ArrayList<QToolUnit> tools = new ArrayList<>();

    private static final Preferences prefs = Preferences.userNodeForPackage(MainFrame.class);
    private static final String THEME_KEY = "selected_theme";
    private static final String DEFAULT_THEME = "Mac样式浅色";


    public MainFrame() throws URISyntaxException {
        loadThemePreference();

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

    private static void loadThemePreference() {
        String savedTheme = prefs.get(THEME_KEY, DEFAULT_THEME);
        applyThemeByName(savedTheme);
    }

    private static void applyThemeByName(String themeName) {
        try {
            switch (themeName) {
                case "浅色":
                    FlatLightLaf.setup();
                    break;
                case "深色":
                    FlatDarkLaf.setup();
                    break;
                case "IntelliJ":
                    FlatIntelliJLaf.setup();
                    break;
                case "Darcula":
                    FlatDarculaLaf.setup();
                    break;
                case "Mac样式浅色":
                    FlatMacLightLaf.setup();
                    break;
                case "Mac样式深色":
                    FlatMacDarkLaf.setup();
                    break;
                default:
                    FlatMacLightLaf.setup();
            }
        } catch (Exception e) {
            Logger.error(e, "应用主题失败：{}", themeName);
            //加载系统默认
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private static void changeTheme(String themeName, ButtonGroup themeGroup) {
        prefs.put(THEME_KEY, themeName);

        SwingUtilities.invokeLater(() -> {
            applyThemeByName(themeName);
            updateAllFrames(themeGroup, themeName);
        });
    }

    private static void updateAllFrames(ButtonGroup themeGroup, String selectedTheme) {
        oldClass.forEach(frame -> {
            SwingUtilities.updateComponentTreeUI(frame);
            frame.repaint();
        });

        Enumeration<AbstractButton> elements = themeGroup.getElements();
        while (elements.hasMoreElements()) {
            AbstractButton button = elements.nextElement();
            if (button instanceof JRadioButtonMenuItem menuItem) {
                menuItem.setSelected(menuItem.getText().equals(selectedTheme));
            }
        }

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
                    Object tool = toolClass.getDeclaredConstructor().newInstance();
                    QToolUnit toolUnit = new QToolUnit() {
                        @Override
                        protected String setName() {
                            try {
                                return toolClass.getField("name").get(tool).toString();
                            } catch (Exception _) {
                                return "未知";
                            }
                        }

                        @Override
                        protected String setVersion() {
                            try {
                                return toolClass.getField("version").get(tool).toString();
                            } catch (Exception _) {
                                return "0.0";
                            }
                        }

                        @Override
                        public void showSetsDialog() {
                            try {
                                toolClass.getMethod("showSetsDialog").invoke(tool);
                            } catch (Exception e) {
                                Logger.warn(e, "工具{}加载异常，引用的开发库可能存在兼容问题", toolFile.getName());
                                JOptionPane.showMessageDialog(null, "发生错误");
                            }
                        }

                        @Override
                        public void showTool() {
                            try {

                                JDialog toolDialog = (JDialog) toolClass.getMethod("getToolDialog").invoke(tool);
                                boolean isAdd  =false;
                                if (toolDialog.getJMenuBar()!=null) {
                                    JMenuBar jMenuBar = toolDialog.getJMenuBar();
                                    for (Component component : jMenuBar.getComponents()) {
                                        if (component instanceof JMenu menu) {
                                            if (menu.getText().equals("软件")) {
                                                JCheckBoxMenuItem alwaysTop = new JCheckBoxMenuItem("置顶");
                                                alwaysTop.addActionListener(e1 -> {
                                                    toolDialog.setAlwaysOnTop(alwaysTop.isSelected());
                                                });
                                                alwaysTop.setSelected(true);
                                                isAdd = true;
                                            }
                                        }
                                    }
                                    if (!isAdd) {
                                        JMenu appMenu = new JMenu("软件");
                                        JCheckBoxMenuItem alwaysTop = new JCheckBoxMenuItem("置顶");
                                        alwaysTop.addActionListener(e1 -> {
                                            toolDialog.setAlwaysOnTop(alwaysTop.isSelected());
                                        });
                                        alwaysTop.setSelected(true);
                                        appMenu.add(alwaysTop);
                                        jMenuBar.add(appMenu);
                                        isAdd = true;
                                    }
                                }else{
                                    JMenuBar jMenuBar = new JMenuBar();
                                    JMenu appMenu = new JMenu("软件");
                                    JCheckBoxMenuItem alwaysTop = new JCheckBoxMenuItem("置顶");
                                    alwaysTop.addActionListener(e1 -> {
                                        toolDialog.setAlwaysOnTop(alwaysTop.isSelected());
                                    });
                                    alwaysTop.setSelected(true);
                                    appMenu.add(alwaysTop);
                                    jMenuBar.add(appMenu);
                                    toolDialog.setJMenuBar(jMenuBar);
                                    isAdd = true;

                                }
                                Logger.info("置顶添加状态：{}", isAdd);

                                Dimension preferredSize = toolDialog.getPreferredSize();
                                if ((preferredSize.width < 500 || preferredSize.height < 300)) {
                                    toolDialog.setSize(Math.max(preferredSize.width, 500), Math.max(preferredSize.height, 300));
                                }else{
                                    toolDialog.setSize(preferredSize);
                                }

                                toolDialog.setAlwaysOnTop(true);
                                toolDialog.setLocationRelativeTo(null);
                                toolDialog.setVisible(true);
                            } catch (Exception e) {
                                Logger.warn(e, "工具{}加载异常，引用的开发库可能存在兼容问题", toolFile.getName());

                                try {
                                    toolClass.getMethod("showTool").invoke(tool);
                                } catch (Exception ex) {
                                    Logger.warn(ex, "工具{}加载异常，引用的开发库可能存在兼容问题", toolFile.getName());
                                    JOptionPane.showMessageDialog(null, "发生错误");
                                }
                            }

                        }

                        @Override
                        public JDialog getToolDialog() {
                            try {
                                return (JDialog) toolClass.getMethod("getToolDialog").invoke(tool);
                            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                                Logger.warn(e, "工具{}加载异常，引用的开发库可能存在兼容问题", toolFile.getName());
                                JOptionPane.showMessageDialog(null, "发生错误");
                            }
                            JDialog error = new JDialog();
                            error.setTitle("错误");
                            error.add(new JLabel("加载工具" + toolFile.getName() + "失败"));
                            return error;
                        }
                    };

                    tools.add(toolUnit);
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
                oldClass.forEach(Window::dispose);
                new MainFrame();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        moreMenu.add(refreshMenuItem);

        JMenu themeMenu = new JMenu("主题");
        themeMenu.setFont(UIManager.getFont("h1.font"));

        ButtonGroup themeGroup = new ButtonGroup();
        String currentTheme = prefs.get(THEME_KEY, DEFAULT_THEME);

        JRadioButtonMenuItem lightTheme = createThemeMenuItem("浅色", themeGroup, currentTheme);
        lightTheme.addActionListener(e -> changeTheme("浅色", themeGroup));
        themeMenu.add(lightTheme);

        JRadioButtonMenuItem darkTheme = createThemeMenuItem("深色", themeGroup, currentTheme);
        darkTheme.addActionListener(e -> changeTheme("深色", themeGroup));
        themeMenu.add(darkTheme);

        JRadioButtonMenuItem intellijTheme = createThemeMenuItem("IntelliJ", themeGroup, currentTheme);
        intellijTheme.addActionListener(e -> changeTheme("IntelliJ", themeGroup));
        themeMenu.add(intellijTheme);

        JRadioButtonMenuItem darculaTheme = createThemeMenuItem("Darcula", themeGroup, currentTheme);
        darculaTheme.addActionListener(e -> changeTheme("Darcula", themeGroup));
        themeMenu.add(darculaTheme);

        JRadioButtonMenuItem MacLightTheme = createThemeMenuItem("Mac样式浅色", themeGroup, currentTheme);
        MacLightTheme.addActionListener(e -> changeTheme("Mac样式浅色", themeGroup));
        themeMenu.add(MacLightTheme);

        JRadioButtonMenuItem MacDarkTheme = createThemeMenuItem("Mac样式深色", themeGroup, currentTheme);
        MacDarkTheme.addActionListener(e -> changeTheme("Mac样式深色", themeGroup));
        themeMenu.add(MacDarkTheme);

        moreMenu.add(themeMenu);

        JMenuItem aboutMenuItem = new JMenuItem("关于");
        aboutMenuItem.setFont(UIManager.getFont("h1.font"));
        aboutMenuItem.addActionListener(e -> new AboutDialog());
        moreMenu.add(aboutMenuItem);


        popupMenu.add(moreMenu);

        if(style == 0) popupMenu.show(oldClass.get(0), -popupMenu.getWidth() - oldClass.get(0).getWidth(), 0);
        else if (style == 1)popupMenu.show(oldClass.get(1), oldClass.get(1).getWidth(), 0);
    }

    private static JRadioButtonMenuItem createThemeMenuItem(String themeName, ButtonGroup themeGroup, String currentTheme) {
        JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(themeName);
        menuItem.setSelected(themeName.equals(currentTheme));
        themeGroup.add(menuItem);
        return menuItem;
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
