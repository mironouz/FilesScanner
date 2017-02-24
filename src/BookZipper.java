import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.util.zip.*;
import java.beans.*;

public class BookZipper implements ActionListener, PropertyChangeListener{
    private ArrayList<File> files = new ArrayList<>();//список для хранения файлов
    private JFileChooser fc;//для выбора файла с путями
    private JFileChooser fs;//для сохранения архива
	private JButton openButton;
    private JButton showButton;
    private JButton saveButton;
	private Form1 f1;//первая форма
    private Form2 f2;//вторая форма
	private JFrame frame;//главный фрейм приложения
    private File input;//здесь будет файл с путями
    private JLabel file_path;//метка для отображения выбранного файла с путями
    private JProgressBar progressBar;//загрузка готовности архива

    private final String[] columnNames = {"Имя файла", "Полный путь"};//названия колонок в таблице

    private BookZipper(){
        frame = new JFrame("BookZipper");
       	f1 = new Form1();
        f2 = new Form2();
       	openButton.addActionListener(this);
        showButton.addActionListener(this);
        saveButton.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e){
        //обработчик кнопки открыть
        if(e.getSource() == openButton){
            int returnVal = fc.showOpenDialog(f1);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                input = fc.getSelectedFile();
            }
            if(input != null) file_path.setText(input.getPath());//показать путь к файлу на метке
        }

        //обработчик кнопки просмотреть
        else if (e.getSource() == showButton) {

            //если не выбран файл, то показываем диалоговое окно с ошибкой
            if(input == null) {
                JOptionPane.showMessageDialog(frame, "Пожалуйста, выберите файл!!!");
                return;
            }

            //считываем пути по строчке и создаем для каждого отдельный файл
            try (BufferedReader br = new BufferedReader(new FileReader(input))) {
                String line;
                while ((line = br.readLine()) != null) {
                    files.add(new File(line));
                }
            }

            catch(IOException ignored){}

            int size = files.size();//количество файлов

            String[][] data = new String[size][2];//массив с именами и полными путями всех файлов для таблицы

            for(int i = 0; i < size; i++){//заполняем массив
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
        //обработчик кнопки сохранить
        else if (e.getSource() == saveButton) {
            int returnVal = fs.showSaveDialog(f2);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fs.getSelectedFile(); 
                Task task = new Task(files, file);//создаем новый объект класса "Task"(определен ниже), необходимо для отображения готовности архива в процентах
                task.addPropertyChangeListener(this);//вешаем обработчик
                task.execute();//исполняем нашу задачу
            }
            
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equals(evt.getPropertyName())){
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
        } 
    }

	private void createAndShowGUI() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(f1);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BookZipper bz = new BookZipper();
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            bz.createAndShowGUI();
        });
    }

	private class Form1 extends JPanel{
    	
    	private Form1(){
    		super(new BorderLayout());
            this.setPreferredSize(new Dimension(640, 480));

            fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	        
            JPanel buttonPanel = new JPanel();
            openButton = new JButton("Выбрать файл");
            showButton = new JButton("Просмотреть");
            buttonPanel.add(openButton);
            buttonPanel.add(showButton);

            file_path = new JLabel("Файл не выбран :(");

            add(buttonPanel, BorderLayout.PAGE_START);
            add(file_path, BorderLayout.PAGE_END);
    	}	
    }

    private class Form2 extends JPanel{
        private Form2(){
            super(new BorderLayout());
            this.setPreferredSize(new Dimension(640, 480));

            fs = new JFileChooser();
            fs.setFileSelectionMode(JFileChooser.FILES_ONLY);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("ZIP-файлы", "zip");
            fs.setFileFilter(filter);

            saveButton = new JButton("Сохранить в файл");

            progressBar = new JProgressBar(0, 100);
            progressBar.setValue(0);
            progressBar.setStringPainted(true);

            JPanel progressPanel = new JPanel();
            progressPanel.add(saveButton);
            progressPanel.add(progressBar);

            
            add(progressPanel, BorderLayout.PAGE_END);
        } 
    }
    //класс в котором будет создаваться архив, класс наследуется от SwingWorker для того, чтобы работать с progressBar на котором будет отображаться готовность архива
    class Task extends SwingWorker<Void, Void> {
        ArrayList<File> files;
        File file;

        Task(ArrayList<File> files, File file){
            super();
            this.files = files;
            this.file = file;

        }
        @Override
        public Void doInBackground() {
            int progress = 0;
            setProgress(0);

            try{
                ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file.getPath() + ".zip"));//zip-архив с файлами

                int chunk = Math.round(100 / files.size());//будем прибавлять число к полосе загрузки в зависимости от количества файлов

                for(File f : files){
                    FileInputStream in = new FileInputStream(f);
                    out.putNextEntry(new ZipEntry(f.getName()));//создаем запись в архиве
                    byte[] b = new byte[8192];//буфер
                    int count;

                    while ((count = in.read(b)) > 0) {
                        out.write(b, 0, count);
                    }
                    progress += chunk;//после записи файла увеличиваем процент готовности
                    setProgress(progress);//отображаем
                }
                setProgress(100);
                out.close();
            }
            catch(Exception ignored){}
            
            return null;
        }
    }
}