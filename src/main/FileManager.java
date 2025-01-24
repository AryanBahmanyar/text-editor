package main;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class FileManager {
	
	private static FileManager instance;
	private final JFileChooser fileChooser = new JFileChooser(new File(System.getProperty("user.home"), "Documents"));
	
	private File file = null;
	private boolean saved = true;
	
	public static final Runnable START_NEW = () -> getInstance().startNew();
	public static final Runnable OPEN = () -> getInstance().open();
	public static final Runnable SAVE = () -> getInstance().save();
	public static final Runnable SAVE_AS = () -> getInstance().saveAs();
	public static final Runnable EXIT = () -> getInstance().exit();
	
	public FileManager() {
		fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));        // set default file name extension filter
		//fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("All Files", "*"));  // any additional filter
	}
	
	public static FileManager getInstance() {
		if (instance == null) instance = new FileManager();
		return instance;
	}
	
	public void startNew() {
		// cancel the operation if changes are not saved and user cancelled when prompted with the save dialog
		if (!saved && !promptSave()) return;
		
		AppTextArea.getInstance().clearText();
		AppMenuBar.getInstance().setItemEnabled(0, 0, false);  // disable "New" menu item
		
		// disable all edit menu items
		for (int i = 0; i < AppMenuBar.getInstance().getNumMenuItems(1); i++)
			AppMenuBar.getInstance().setItemEnabled(1, i, false);
		
		file = null;
		setSaved(true);
	}
	
	public void open() {
		if (!saved && !promptSave()) return;
		
		if (fileChooser.showOpenDialog(Main.getFrame()) == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			
			// cancel the operation if the same file that's already open is selected
			if (file != null && selectedFile.getAbsolutePath().equals(file.getAbsolutePath())) return;
			file = selectedFile;
			
			// read all bytes directly, rather than using a Stream object or using the "readAllLines" method of the "Files" class, so as to preserve the
			// original line separator characters; this is to ensure that there's no extra line separator at the end of the text of the text area after
			// opening a file; however, because this method preserves and uses the original line separators, they must be normalized before the text is
			// set, so as to ensure consistent display across different platforms (namely, Unix-based systems, including Mac, which use linefeed, \n, and
			// Windows, which uses carriage return and line feed together, \r\n)
			try {
				byte[] fileContent = Files.readAllBytes(file.toPath());
				String text = new String(fileContent, "UTF-8");
				
				// normalize line separators before setting text; note that strings in Java are immutable, meaning that the "loadFileContents" method gets
				// passed a new string whose value is a modified version of the original string variable; "text" itself doesn't get passed to the method
				AppTextArea.getInstance().setTextContent(text.replace("\r\n", "\n"));
				
				// disable undo and redo menu items
				for (int i = 0; i < 2; i++)
					AppMenuBar.getInstance().setItemEnabled(1, i, false);
				
				// enable / disable edit menu items related to finding and replacing text, depending on whether the new file contains any text or not
				for (int i = 2; i < AppMenuBar.getInstance().getNumMenuItems(1); i++)
					AppMenuBar.getInstance().setItemEnabled(1, i, !text.isEmpty());
				
				setSaved(true);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void save() {
		if (file == null) saveAs();
		else writeFile();
	}
	
	public void saveAs() {
		int selection = fileChooser.showSaveDialog(Main.getFrame());
		
		if (selection == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			
			if (selectedFile.exists()) {
				int option = JOptionPane.showConfirmDialog(fileChooser, "File already exists. Do you want to replace it?", "Confirm Save As",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				
				if (option == JOptionPane.YES_OPTION) {
					file = selectedFile;
					writeFile();
				}
				else saveAs();
			} else {
				file = selectedFile;
				writeFile();
			}
		}
	}
	
	// note that for closing a window or a pane (JFrame, JWindow, or JDialog) and marking it as eligible for garbage collection (to ensure proper cleanup
	// from memory), the dispose method should be used; exiting the application without disposing of the frame does not guarantee proper cleanup
	public void exit() {
		if (!saved && !promptSave()) {
			return;
		}
		Main.getFrames()[0].dispose();
		System.exit(0);
	}
	
	// returns a boolean value, indicating whether the calling method should proceed with its operation(s) or not
	private boolean promptSave() {
		final String[] options = { "Save", "Don't Save", "Cancel" };
		final String fileName = file == null ? getFileName() : ("\"" + getFileName() + "\"");
		
		int selection = JOptionPane.showOptionDialog(Main.getFrame(), "Save " + fileName + "?", "Save",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		
		// returns true for the first 2 options and false if user chose the last option, or closed the option pane
		switch (selection) {
			case 0: save(); return true;
			case 1: return true;
			default: return false;
		}
	}
	
	private void writeFile() {
		try {
			// note that character encoding should be specified explicitly; also, note that the last argument specifies that the file should be
			// overwritten if it already exists
			Files.write(Paths.get(file.getAbsolutePath()), AppTextArea.getInstance().getText().getBytes(StandardCharsets.UTF_8),
					StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			setSaved(true);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean getFileLoaded() {
		return file != null;
	}
	
	public boolean getSaved() {
		return saved;
	}
	
	public void setSaved(boolean saved) {
		this.saved = saved;
		if (saved) AppTextArea.getInstance().saveChanges();
		updateFrameTitle();
	}
	
	public void updateFrameTitle() {
		Main.getFrames()[0].setTitle((FileManager.getInstance().getSaved() ? "" : "*") +
				FileManager.getInstance().getFileName() + " - Text Editor");
	}
	
	public String getFileName() {
		return file == null ? "Untitled" : file.getName();
	}
}
