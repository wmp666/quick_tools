package com.wmp.quick.tools;

import com.wmp.quick.tools.apptools.GetPath;
import org.tinylog.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ControlDialog extends JDialog {

    private String choiceToolName = "";

    private final JLabel nameLabel = new JLabel("名称：未选择");
    private final JLabel versionLabel = new JLabel("版本：未选择");

    public ControlDialog() {
        super();
        setTitle("管理页");
        setLayout(new BorderLayout());

        JPanel infoPanel = new JPanel(new GridLayout(0,1));
        infoPanel.setBorder(BorderFactory.createTitledBorder("工具信息"));
        nameLabel.setFont(UIManager.getFont("h2.font"));
        versionLabel.setFont(UIManager.getFont("h2.font"));
        infoPanel.add(nameLabel);
        infoPanel.add(versionLabel);
        this.add(infoPanel, BorderLayout.WEST);

        JPanel toolsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        MainFrame.tools.forEach(tools -> {
            choiceToolName = tools.name;
            JButton button = new JButton(tools.name);
            button.setFont(UIManager.getFont("h2.font"));
            button.addActionListener(e -> {
                nameLabel.setText("名称：" + tools.name);
                versionLabel.setText("版本：" + tools.version);
            });
            toolsPanel.add(button, gbc);
            gbc.gridy++;
        });

        JButton addButton = new JButton("+ 添加工具");
        addButton.setFont(UIManager.getFont("h2.font"));
        addButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(
                        new File(GetPath.getAppPath(GetPath.SOURCE_FILE_PATH), "tools"));
            } catch (Exception ex) {
                Logger.error(ex, "打开工具文件夹失败");
                JOptionPane.showMessageDialog(null, "打开工具文件夹失败\n" + ex, "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        toolsPanel.add(addButton, gbc);

        this.add(toolsPanel, BorderLayout.CENTER);


        initOpenFilePanel();

        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void initOpenFilePanel() {
        JPanel openFilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        openFilePanel.setBorder(BorderFactory.createTitledBorder("相关文件夹"));

        JButton openToolsFileButton = new JButton("工具文件夹");
        openToolsFileButton.setFont(UIManager.getFont("h2.font"));
        openToolsFileButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(
                        new File(GetPath.getAppPath(GetPath.SOURCE_FILE_PATH), "tools"));
            } catch (Exception ex) {
                Logger.error(ex, "打开工具文件夹失败");
                JOptionPane.showMessageDialog(null, "打开工具文件夹失败\n" + ex, "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        openFilePanel.add(openToolsFileButton);

        JButton openInfoFileButton = new JButton("数据文件夹");
        openInfoFileButton.setFont(UIManager.getFont("h2.font"));
        openInfoFileButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(
                        new File(GetPath.getAppPath(GetPath.SOURCE_FILE_PATH), "inf"));
            } catch (Exception ex) {
                Logger.error(ex, "打开数据文件夹失败");
                JOptionPane.showMessageDialog(null, "打开数据文件夹失败\n" + ex, "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        openFilePanel.add(openInfoFileButton);

        this.add(openFilePanel, BorderLayout.SOUTH);
    }
}
