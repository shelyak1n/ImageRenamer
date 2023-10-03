import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class ImageProcessor {
    private JFrame frame;
    private JPanel dropPanel;
    private JLabel imageView;
    private JComboBox<String> select1;
    private JComboBox<String> select2;
    private JTextField textField;
    private JButton applyButton;
    private File currentImageFile;

    public ImageProcessor() {
        frame = new JFrame("Image Processor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        dropPanel = new JPanel();
        dropPanel.setLayout(new BorderLayout());
        dropPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        dropPanel.setDropTarget(new DropTarget(dropPanel, new ImageDropTargetListener()));

        imageView = new JLabel();
        imageView.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(2, 2));

        select1 = new JComboBox<>(new String[]{
                "ОФ-2",
                "ОФ-3",
                "ОФ-7",
                "ОФ-8"
        });
        select2 = new JComboBox<>(new String[]{
                "ПБиДО",
                "Галерея 1 на БРУ ",
                "Галерея 2 на БРУ",
                "БРУ",
                "Галерея 1 на главный корпус",
                "Галерея 2 на главный корпус",
                "Главный корпус",
                "Галерея 1 на пункт перегрузки",
                "Галерея 2 на пункт перегрузки",
                "Пункт перегрузки",
                "Галерея 1 разгрузки товарной продукции",
                "Гарелея 2 разгрузки товарной продукции"
        });
        textField = new JTextField();
        applyButton = new JButton("Переименовать");
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedOption1 = (String) select1.getSelectedItem();
                String selectedOption2 = (String) select2.getSelectedItem();
                String enteredText = textField.getText();

                // Переименование файла по шаблону
                renameFile(selectedOption1, selectedOption2, enteredText);
            }
        });

        controlPanel.add(select1);
        controlPanel.add(select2);
        controlPanel.add(textField);
        controlPanel.add(applyButton);

        frame.add(dropPanel, BorderLayout.CENTER);
        frame.add(imageView, BorderLayout.NORTH);
        frame.add(controlPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void renameFile(String option1, String option2, String text) {
        if (currentImageFile != null) {
            String originalFileName = currentImageFile.getName();
            String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String newFileName = option1 + ". " + option2 + ". " + text + extension;

            // Проверяем наличие файла с таким именем в директории
            File newFile = new File(currentImageFile.getParentFile(), newFileName);
            int suffix = 1;
            while (newFile.exists()) {
                String suffixStr = "(" + suffix + ")";
                newFileName = option1 + ". " + option2 + ". " + text + suffixStr + extension;
                newFile = new File(currentImageFile.getParentFile(), newFileName);
                suffix++;
            }

            if (currentImageFile.renameTo(newFile)) {
                JOptionPane.showMessageDialog(frame, "Файл успешно переименован.");
                clearImage();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(frame, "Не удалось переименовать файл.");
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Изображение не загружено.");
        }
    }


    private void clearImage() {
        imageView.setIcon(null);
        currentImageFile = null;
    }

    private void clearFields() {
        select1.setSelectedIndex(0);
        select2.setSelectedIndex(0);
        textField.setText("");
    }

    private class ImageDropTargetListener extends DropTargetAdapter {
        @Override
        public void drop(DropTargetDropEvent event) {
            try {
                Transferable transferable = event.getTransferable();
                if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    event.acceptDrop(DnDConstants.ACTION_COPY);
                    java.util.List<File> files = (java.util.List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);

                    if (files.size() > 0) {
                        File imageFile = files.get(0);
                        ImageIcon icon = new ImageIcon(imageFile.getPath());
                        imageView.setIcon(icon);
                        currentImageFile = imageFile;
                        scaleImage(imageFile);
                    }
                } else {
                    event.rejectDrop();
                }
            } catch (Exception e) {
                e.printStackTrace();
                event.rejectDrop();
            }
        }
    }

    private void scaleImage(File imageFile) throws IOException {
        ImageIcon icon = (ImageIcon) imageView.getIcon();
        if (icon != null) {
            int maxWidth = frame.getWidth();
            int maxHeight = frame.getHeight();
            Image image = icon.getImage();

            // Масштабируем изображение под размер окна
            int newWidth = maxWidth;
            int newHeight = maxHeight;
            image = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            icon = new ImageIcon(image);
            imageView.setIcon(icon);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ImageProcessor();
            }
        });
    }
}