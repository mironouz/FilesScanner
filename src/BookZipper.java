import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.*;
import java.util.zip.*;
import java.beans.*;

public class BookZipper implements ActionListener, PropertyChangeListener{
    ArrayList<File> files = new ArrayList<>();
    JFileChooser fc,fs;
	JButton openButton, showButton, saveButton;
	Form1 f1;
    Form2 f2;
	JFrame frame;
    File input;
    JTable table;
    JPanel buttonPanel, progressPanel;
    JLabel file_path;
    JProgressBar progressBar;

    String[] columnNames = {"Имя файла", "Полный путь"};
    String[][] data;

    public BookZipper(){
        frame = new JFrame("BookZipper");
       	f1 = new Form1();
        f2 = new Form2();
       	openButton.addActionListener(this);
        showButton.addActionListener(this);
        saveButton.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e){
        if(e.getSource() == openButton){
            int returnVal = fc.showOpenDialog(f1);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                input = fc.getSelectedFile();
            }
            if(input != null) file_path.setText(input.getPath());
        }

        else if (e.getSource() == showButton) {
          
            if(input == null) {
                JOptionPane.showMessageDialog(frame, "Пожалуйста, выберите файл!!!");
                return;
            }

            try (BufferedReader br = new BufferedReader(new FileReader(input))) {
                String line;
                while ((line = br.readLine()) != null) {
                    files.add(new File(line));
                }
            }

            catch(IOException err){}     

            int size = files.size();

            data = new String[size][2];

            for(int i = 0; i < size; i++){
                data[i][0] = files.get(i).getName();
                data[i][1] = files.get(i).getPath();
            }

            frame.remove(f1);
            frame.add(f2);
            table = new JTable(data, columnNames);
            JScrollPane logScrollPane = new JScrollPane(table);
            f2.add(logScrollPane, BorderLayout.CENTER);
            frame.pack();             
        }

        else if (e.getSource() == saveButton) {
            int returnVal = fs.showSaveDialog(f2);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fs.getSelectedFile(); 
                Task task = new Task(files, file);
                task.addPropertyChangeListener(this);
                task.execute();
            }
            
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
        } 
    }

	private void createAndShowGUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(f1);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	BookZipper bz = new BookZipper();
                UIManager.put("swing.boldMetal", Boolean.FALSE); 
                bz.createAndShowGUI();
            }
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

            progressPanel = new JPanel();
            progressPanel.add(saveButton);
            progressPanel.add(progressBar);

            
            add(progressPanel, BorderLayout.PAGE_END);
        } 
    }

    class Task extends SwingWorker<Void, Void> {
        ArrayList<File> files;
        File file;

        public Task(ArrayList<File> files, File file){
            super();
            this.files = files;
            this.file = file;

        }
        @Override
        public Void doInBackground() {
            int progress = 0;
            setProgress(0);

            try{
                ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file.getPath() + ".zip"));

                int chunk = Math.round(100 / files.size());
                System.out.println(chunk);

                for(File f : files){
                    FileInputStream in = new FileInputStream(f);
                    out.putNextEntry(new ZipEntry(f.getName()));
                    byte[] b = new byte[8192];
                    int count;

                    while ((count = in.read(b)) > 0) {
                        out.write(b, 0, count);
                    }
                    progress += chunk;
                    setProgress(progress);
                }
                out.close();
                setProgress(100);
            }
            catch(Exception err){}
            
            return null;
        }
    }
}