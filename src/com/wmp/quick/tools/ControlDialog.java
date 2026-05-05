package com.wmp.quick.tools;

import com.wmp.develop.tool.QToolUnit;
import com.wmp.quick.tools.apptools.GetPath;
import org.tinylog.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.io.File;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class ControlDialog extends JDialog {

    private String choiceToolName = "";
    private QToolUnit selectedTool = null;

    private final JLabel nameLabel = new JLabel("名称：未选择");
    private final JLabel versionLabel = new JLabel("版本：未选择");

    private DefaultTableModel tableModel;
    private JTable toolsTable;
    private JTextField searchField;

    public ControlDialog() {
        super();
        setTitle("工具管理");
        setModal(true);
        setSize(900, 600);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        initSearchPanel(mainPanel);

        initToolsTablePanel(mainPanel);

        initInfoPanel(mainPanel);

        initFunctionPanel(mainPanel);

        initOpenFilePanel(mainPanel);

        add(mainPanel, BorderLayout.CENTER);

        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void initSearchPanel(JPanel mainPanel) {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("搜索工具"));

        JLabel searchLabel = new JLabel("搜索：");
        searchLabel.setFont(UIManager.getFont("h2.font"));
        searchPanel.add(searchLabel);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTools();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTools();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterTools();
            }
        });
        searchPanel.add(searchField);

        JButton refreshButton = new JButton("刷新列表");
        refreshButton.setFont(UIManager.getFont("h2.font"));
        refreshButton.addActionListener(e -> refreshToolsList());
        searchPanel.add(refreshButton);

        mainPanel.add(searchPanel, BorderLayout.NORTH);
    }

    private void initToolsTablePanel(JPanel mainPanel) {
        String[] columns = {"工具名称", "版本"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        toolsTable = new JTable(tableModel);
        toolsTable.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        toolsTable.setRowHeight(30);
        toolsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        toolsTable.getTableHeader().setReorderingAllowed(false);

        JTableHeader header = toolsTable.getTableHeader();
        header.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(header.getWidth(), 35));

        toolsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = toolsTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String toolName = (String) tableModel.getValueAt(selectedRow, 0);
                    selectToolByName(toolName);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(toolsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("已安装工具"));
        scrollPane.setPreferredSize(new Dimension(400, 250));

        loadToolsToTable();

        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void initInfoPanel(JPanel mainPanel) {
        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 5, 10));
        infoPanel.setBorder(BorderFactory.createTitledBorder("工具信息"));
        infoPanel.setPreferredSize(new Dimension(250, 0));

        nameLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        versionLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));

        JLabel tipLabel = new JLabel("<html><div style='padding: 5px;'>提示：<br>选择工具查看详情<br>或执行操作</div></html>");
        tipLabel.setFont(new Font("Microsoft YaHei", Font.ITALIC, 12));
        tipLabel.setForeground(UIManager.getColor("Label.disabledForeground"));

        infoPanel.add(nameLabel);
        infoPanel.add(versionLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(tipLabel);

        mainPanel.add(infoPanel, BorderLayout.EAST);
    }

    private void initFunctionPanel(JPanel mainPanel) {
        JPanel functionPanel = new JPanel(new GridLayout(4, 1, 5, 10));
        functionPanel.setBorder(BorderFactory.createTitledBorder("操作"));
        functionPanel.setPreferredSize(new Dimension(150, 0));

        JButton openToolButton = createStyledButton("打开工具");
        openToolButton.addActionListener(e -> openSelectedTool());

        JButton settingsButton = createStyledButton("设置");
        settingsButton.addActionListener(e -> openToolSettings());

        JButton deleteButton = createStyledButton("删除工具");
        deleteButton.setForeground(Color.RED);
        deleteButton.addActionListener(e -> deleteSelectedTool());

        JButton addButton = createStyledButton("添加新工具");
        addButton.addActionListener(e -> showAddToolDialog());

        functionPanel.add(openToolButton);
        functionPanel.add(settingsButton);
        functionPanel.add(deleteButton);
        functionPanel.add(addButton);

        mainPanel.add(functionPanel, BorderLayout.WEST);
    }

    private void initOpenFilePanel(JPanel mainPanel) {
        JPanel openFilePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        openFilePanel.setBorder(BorderFactory.createTitledBorder("快速访问"));

        JButton openToolsFileButton = createStyledButton("工具文件夹");
        openToolsFileButton.addActionListener(e -> openToolsFolder());

        JButton openInfoFileButton = createStyledButton("数据文件夹");
        openInfoFileButton.addActionListener(e -> openDataFolder());

        openFilePanel.add(openToolsFileButton);
        openFilePanel.add(openInfoFileButton);

        mainPanel.add(openFilePanel, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void loadToolsToTable() {
        tableModel.setRowCount(0);
        MainFrame.tools.forEach(tool -> {
            tableModel.addRow(new Object[]{tool.name, tool.version});
        });
    }

    private void filterTools() {
        String searchText = searchField.getText().trim().toLowerCase();
        tableModel.setRowCount(0);

        MainFrame.tools.forEach(tool -> {
            if (searchText.isEmpty() ||
                    tool.name.toLowerCase().contains(searchText) ||
                    tool.version.toLowerCase().contains(searchText)) {
                tableModel.addRow(new Object[]{tool.name, tool.version});
            }
        });
    }

    private void refreshToolsList() {
        try {
            Logger.info("正在刷新工具列表...");
            MainFrame mainFrame = new MainFrame();
            loadToolsToTable();
            searchField.setText("");
            JOptionPane.showMessageDialog(this,
                    "工具列表已刷新！\n当前共有 " + MainFrame.tools.size() + " 个工具",
                    "刷新成功",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            Logger.error(ex, "刷新工具列表失败");
            JOptionPane.showMessageDialog(this,
                    "刷新失败：" + ex.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selectToolByName(String toolName) {
        MainFrame.tools.forEach(tool -> {
            if (tool.name.equals(toolName)) {
                choiceToolName = tool.name;
                selectedTool = tool;
                nameLabel.setText("名称：" + tool.name);
                versionLabel.setText("版本：" + tool.version);
            }
        });
    }

    private void openSelectedTool() {
        if (selectedTool == null) {
            JOptionPane.showMessageDialog(this,
                    "请先选择一个工具！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Logger.info("正在打开工具：{}", selectedTool.name);
        selectedTool.showTool();
    }

    private void openToolSettings() {
        if (selectedTool == null) {
            JOptionPane.showMessageDialog(this,
                    "请先选择一个工具！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Logger.info("正在打开工具设置：{}", selectedTool.name);
        selectedTool.showSetsDialog();
    }

    private void deleteSelectedTool() {
        if (selectedTool == null) {
            JOptionPane.showMessageDialog(this,
                    "请先选择一个工具！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要删除工具 \"" + selectedTool.name + "\" 吗？\n此操作将从文件系统中删除对应的JAR文件。",
                "确认删除",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                File toolsDir = new File(GetPath.getAppPath(GetPath.SOURCE_FILE_PATH), "tools");
                File[] jarFiles = toolsDir.listFiles(f -> f.getName().endsWith(".jar"));

                if (jarFiles != null) {
                    for (File jarFile : jarFiles) {
                        try {
                            URLClassLoader finalLoader = new URLClassLoader(new java.net.URL[]{jarFile.toURI().toURL()});
                            Class<?> clazz = finalLoader.loadClass("com.wmp.tool.Main");
                            Object instance = clazz.getDeclaredConstructor().newInstance();
                            String name = clazz.getField("name").get(instance).toString();

                            if (name.equals(selectedTool.name)) {
                                if (jarFile.delete()) {
                                    Logger.info("成功删除工具文件：{}", jarFile.getName());
                                    JOptionPane.showMessageDialog(this,
                                            "工具已删除！\n请刷新列表以更新显示。",
                                            "删除成功",
                                            JOptionPane.INFORMATION_MESSAGE);
                                    return;
                                } else {
                                    throw new Exception("文件删除失败");
                                }
                            }
                        } catch (Exception e) {
                            continue;
                        }
                    }
                }

                JOptionPane.showMessageDialog(this,
                        "未找到对应的工具文件！",
                        "错误",
                        JOptionPane.ERROR_MESSAGE);

            } catch (Exception ex) {
                Logger.error(ex, "删除工具失败");
                JOptionPane.showMessageDialog(this,
                        "删除失败：" + ex.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAddToolDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择工具文件");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JAR文件", "jar"));
        fileChooser.setMultiSelectionEnabled(true);

        JDialog dropDialog = new JDialog(this, "添加新工具", true);
        dropDialog.setLayout(new BorderLayout(10, 10));
        dropDialog.setSize(500, 300);

        JPanel dropZonePanel = new JPanel(new BorderLayout());
        dropZonePanel.setBackground(new Color(240, 248, 255));
        dropZonePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20),
                BorderFactory.createLineBorder(new Color(74, 144, 226), 2)
        ));

        JLabel dropLabel = new JLabel("<html><div style='text-align: center; padding: 20px;'>" +
                "<b style='font-size: 18px; color: #4A90E2;'>拖拽 JAR 文件到此处</b><br><br>" +
                "<span style='color: #666;'>或者点击下方按钮选择文件</span><br>" +
                "<span style='color: #999; font-size: 12px;'>支持批量添加多个工具</span>" +
                "</div></html>", SwingConstants.CENTER);
        dropLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));

        dropZonePanel.add(dropLabel, BorderLayout.CENTER);

        setupDragAndDropForPanel(dropZonePanel, dropDialog);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton chooseButton = new JButton("选择文件");
        chooseButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        chooseButton.addActionListener(e -> {
            int result = fileChooser.showOpenDialog(dropDialog);
            if (result == JFileChooser.APPROVE_OPTION) {
                File[] selectedFiles = fileChooser.getSelectedFiles();
                processToolInstallation(selectedFiles, dropDialog);
            }
        });

        JButton cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        cancelButton.addActionListener(e -> dropDialog.dispose());

        buttonPanel.add(chooseButton);
        buttonPanel.add(cancelButton);

        dropDialog.add(dropZonePanel, BorderLayout.CENTER);
        dropDialog.add(buttonPanel, BorderLayout.SOUTH);
        dropDialog.setLocationRelativeTo(this);
        dropDialog.setVisible(true);
    }

    private void setupDragAndDropForPanel(JPanel dropZonePanel, JDialog parentDialog) {
        new DropTarget(dropZonePanel, new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                dropZonePanel.setBackground(new Color(173, 216, 230));
                dtde.acceptDrag(DnDConstants.ACTION_COPY);
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                dropZonePanel.setBackground(new Color(240, 248, 255));
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                dropZonePanel.setBackground(new Color(240, 248, 255));

                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable transferable = dtde.getTransferable();

                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        @SuppressWarnings("unchecked")
                        List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);

                        processToolInstallation(files.toArray(new File[0]), parentDialog);

                        dtde.dropComplete(true);
                    }
                } catch (Exception e) {
                    Logger.error(e, "拖拽添加失败");
                    JOptionPane.showMessageDialog(parentDialog,
                            "添加失败：" + e.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                    dtde.dropComplete(false);
                }
            }
        });
    }

    private void processToolInstallation(File[] files, JDialog parentDialog) {
        int successCount = 0;
        int failCount = 0;
        StringBuilder failMessages = new StringBuilder();

        for (File file : files) {
            if (file.getName().toLowerCase().endsWith(".jar")) {
                if (installTool(file)) {
                    successCount++;
                } else {
                    failCount++;
                    failMessages.append("  ").append(file.getName()).append("\n");
                }
            } else {
                failCount++;
                failMessages.append("  ").append(file.getName()).append(" (非JAR文件)\n");
            }
        }

        StringBuilder resultMessage = new StringBuilder();
        if (successCount > 0) {
            resultMessage.append("成功添加 ").append(successCount).append(" 个工具！\n");
        }
        if (failCount > 0) {
            resultMessage.append("\n失败 ").append(failCount).append(" 个：\n")
                    .append(failMessages.toString());
        }

        JOptionPane.showMessageDialog(parentDialog,
                resultMessage.toString(),
                "添加结果",
                successCount > 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);

        if (successCount > 0) {
            parentDialog.dispose();
            refreshToolsList();
        }
    }

    private boolean installTool(File sourceJar) {
        try {
            File toolsDir = new File(GetPath.getAppPath(GetPath.SOURCE_FILE_PATH), "tools");
            toolsDir.mkdirs();

            File destFile = new File(toolsDir, sourceJar.getName());

            if (destFile.exists()) {
                int option = JOptionPane.showConfirmDialog(this,
                        "文件 \"" + sourceJar.getName() + "\" 已存在，是否覆盖？",
                        "文件冲突",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (option != JOptionPane.YES_OPTION) {
                    return false;
                }
            }

            Files.copy(sourceJar.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Logger.info("成功复制工具文件：{} -> {}", sourceJar.getName(), destFile.getAbsolutePath());

            return true;

        } catch (Exception e) {
            Logger.error(e, "安装工具失败：{}", sourceJar.getName());
            return false;
        }
    }

    private void openToolsFolder() {
        try {
            File toolsDir = new File(GetPath.getAppPath(GetPath.SOURCE_FILE_PATH), "tools");
            toolsDir.mkdirs();
            Desktop.getDesktop().open(toolsDir);
            Logger.info("已打开工具文件夹：{}", toolsDir.getAbsolutePath());
        } catch (Exception ex) {
            Logger.error(ex, "打开工具文件夹失败");
            JOptionPane.showMessageDialog(this,
                    "打开工具文件夹失败\n" + ex.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openDataFolder() {
        try {
            File dataDir = new File(GetPath.getAppPath(GetPath.SOURCE_FILE_PATH), "inf");
            dataDir.mkdirs();
            Desktop.getDesktop().open(dataDir);
            Logger.info("已打开数据文件夹：{}", dataDir.getAbsolutePath());
        } catch (Exception ex) {
            Logger.error(ex, "打开数据文件夹失败");
            JOptionPane.showMessageDialog(this,
                    "打开数据文件夹失败\n" + ex.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
