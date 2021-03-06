import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

public class BookScanner implements ActionListener {
    private ArrayList<File> files = new ArrayList<>();// Список в котором будут храниться все файлы с заданным расширением

    private JButton openButton;
    private JButton saveButton;
    private JButton findButton;
    private JFileChooser fc;// Для выбора директории с файлами
    private JFileChooser fs;// Для экспорта файла с путями
    private JTextField textExt;//Здесь будем перечислять расширения
    private File dir;
    private Form1 f1;//Первая форма
    private Form2 f2;//Вторая форма
    private JFrame frame;//Основной фрейм
    private final String[] columnNames = {"Имя файла", "Полный путь"};//Названия колонок в таблице
    private JLabel folder;//Здесь будет отображаться выбранная папка


    private BookScanner() {
        f1 = new Form1();
        f2 = new Form2();
        frame = new JFrame("BookScanner");
        openButton.addActionListener(this);
        saveButton.addActionListener(this);
        findButton.addActionListener(this);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BookScanner bs = new BookScanner();
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            bs.createAndShowGUI();
        });
    }

    //рекурсивно получаем все файлы заданного типа
    private void getFiles(File f, String type) {
        File[] files_list;
        if (f.isDirectory() && (files_list = f.listFiles()) != null) { // если получили директорию и она не пуста, то вызываем функцию рекурсивно
            for (File file : files_list) {
                getFiles(file, type);
            }
        } else {//иначе проверяем расширение файла и если оно совпало с необходимым, то добавляем информацию в списки
            if (f.getName().endsWith("." + type)) files.add(f);
        }
    }

    public void actionPerformed(ActionEvent e) {
        // Обработчик кнопки "Открыть"
        if (e.getSource() == openButton) {
            int returnVal = fc.showOpenDialog(f1);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                dir = fc.getSelectedFile();
            }
            if(dir != null)folder.setText(dir.getPath());//Показать на лэйбле путь к папке, которую выбрали
        }
        //Обработчик кнопки "Искать"
        else if (e.getSource() == findButton) {
            if (dir == null){//если папка не выбрана
                JOptionPane.showMessageDialog(frame, "Пожалуйста, выберите папку!!!");
                return;
            }

            if (textExt.getText().equals("")) {//если не перечислен ни один тип
                JOptionPane.showMessageDialog(frame, "Перечислите хотя бы один тип!!!");
                return;
            }

            String[] types = textExt.getText().split(",");//дробим строку с типами по запятым

            for (String type : types) {
                type = type.trim();//убираем пробелы по краям, если они есть
                getFiles(dir, type);//ищем файлы заданного типа в папке рекурсивно
            }

            int size = files.size();//количество найденных файлов

            if (size == 0) {//если файлов не найдено
                JOptionPane.showMessageDialog(frame, "Файлов не найдено :(");
                return;
            }

            String[][] data = new String[size][2];//массив с именами и полными путями всех файлов для таблицы

            for (int i = 0; i < size; i++) {//заполняем массив
                data[i][0] = files.get(i).getName();
                data[i][1] = files.get(i).getPath();
            }

            frame.remove(f1);//убираем предыдущую форму с фрейма
            frame.add(f2);//добавляем новую
            JTable table = new JTable(data, columnNames);//строим таблицу
            JScrollPane logScrollPane = new JScrollPane(table);
            f2.add(logScrollPane, BorderLayout.CENTER);
            frame.pack();
        }
        //Обработчик кнопки "Сохранить"
        else if (e.getSource() == saveButton) {
            int returnVal = fs.showSaveDialog(f1);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fs.getSelectedFile();
                try {
                    PrintWriter pw = new PrintWriter(new FileOutputStream(file.getPath() + ".txt"));//Создаем файл куда будем сохранять пути
                    for (File f : files) pw.println(f.getPath());//записываем данные в цикле
                    pw.close();//закрываем файл
                } catch (FileNotFoundException ignored) {
                }
            }
        }
    }

    private void createAndShowGUI() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(f1);
        frame.pack();
        frame.setVisible(true);
    }

    private class Form1 extends JPanel {

        private Form1() {
            super(new BorderLayout());
            this.setPreferredSize(new Dimension(640, 480));

            fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            openButton = new JButton("Выбрать папку");
            findButton = new JButton("Искать!");
            textExt = new JTextField(20);
            folder = new JLabel("Папка не выбрана");

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(folder);
            buttonPanel.add(openButton);
            buttonPanel.add(findButton);


            JPanel bottomPanel = new JPanel();
            bottomPanel.add(new JLabel("Перечислите расширения через запятую:"));
            bottomPanel.add(textExt);


            add(buttonPanel, BorderLayout.PAGE_START);
            add(bottomPanel, BorderLayout.PAGE_END);
        }
    }

    private class Form2 extends JPanel {
        private Form2() {
            super(new BorderLayout());
            this.setPreferredSize(new Dimension(640, 480));

            fs = new JFileChooser();
            fs.setFileSelectionMode(JFileChooser.FILES_ONLY);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("TXT-файлы", "txt");
            fs.setFileFilter(filter);

            saveButton = new JButton("Сохранить в файл");
            add(saveButton, BorderLayout.PAGE_END);
        }
    }
}