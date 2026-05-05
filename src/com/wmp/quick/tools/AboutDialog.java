package com.wmp.quick.tools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

public class AboutDialog extends JDialog {
    public AboutDialog() {
        super();
        setTitle("关于");
        setModal(true);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.CENTER;

        int row = 0;

        JLabel titleLabel = new JLabel("快捷工具");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 24));
        titleLabel.setForeground(UIManager.getColor("Actions.Blue"));
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = row++;
        gbc.insets = new Insets(15, 5, 5, 5);
        mainPanel.add(Box.createVerticalStrut(10), gbc);

        JLabel appLabel = createStyledLabel("应用名称：", "快捷工具");
        gbc.gridy = row++;
        gbc.insets = new Insets(5, 5, 5, 5);
        mainPanel.add(appLabel, gbc);

        JLabel authorLabel = createStyledLabel("作者：", "wmp666");
        gbc.gridy = row++;
        mainPanel.add(authorLabel, gbc);

        JLabel versionLabel = createStyledLabel("版本：", "1.0.0");
        gbc.gridy = row++;
        mainPanel.add(versionLabel, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(15, 5, 5, 5);
        mainPanel.add(Box.createVerticalStrut(5), gbc);

        JLabel repoLabel = new JLabel("项目仓库：");
        repoLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        repoLabel.setForeground(UIManager.getColor("Label.foreground"));
        gbc.gridy = row++;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(repoLabel, gbc);

        String githubUrl = "https://github.com/wmp666/quick_tools";
        JLabel linkLabel = new JLabel("<html><a href=\"" + githubUrl + "\">" + githubUrl + "</a></html>");
        linkLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        linkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(githubUrl));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AboutDialog.this,
                            "无法打开浏览器，请手动访问：\n" + githubUrl,
                            "提示",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                linkLabel.setForeground(UIManager.getColor("Actions.Blue"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                linkLabel.setForeground(UIManager.getColor("Link.activeForeground"));
            }
        });
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 20, 5, 5);
        mainPanel.add(linkLabel, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(15, 5, 5, 5);
        mainPanel.add(Box.createVerticalStrut(10), gbc);

        JTextArea descArea = new JTextArea("一个简洁高效的快捷工具集合平台");
        descArea.setFont(new Font("Microsoft YaHei", Font.ITALIC, 12));
        descArea.setForeground(UIManager.getColor("Label.disabledForeground"));
        descArea.setEditable(false);
        descArea.setBackground(mainPanel.getBackground());
        descArea.setOpaque(false);
        descArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        gbc.gridy = row++;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(descArea, gbc);

        add(mainPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setAlwaysOnTop(true);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    private JLabel createStyledLabel(String prefix, String value) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setOpaque(false);

        JLabel prefixLabel = new JLabel(prefix);
        prefixLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        prefixLabel.setForeground(UIManager.getColor("Label.disabledForeground"));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        valueLabel.setForeground(UIManager.getColor("Label.foreground"));

        panel.add(prefixLabel);
        panel.add(valueLabel);

        return new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                panel.setBounds(0, 0, panel.getPreferredSize().width, panel.getPreferredSize().height);
                panel.paint(g);
            }

            @Override
            public Dimension getPreferredSize() {
                return panel.getPreferredSize();
            }
        };
    }
}
