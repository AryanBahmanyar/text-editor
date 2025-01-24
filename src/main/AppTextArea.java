package main;

import java.awt.Font;
import java.awt.Insets;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import main.panes.FindPane;
import main.panes.FontPane;
import main.panes.ReplacePane;

@SuppressWarnings("serial")
public final class AppTextArea extends JScrollPane {
	
	private static AppTextArea instance;
	
	// while all members (ie. variables and methods) of a singleton can be static, it's better design practice to promote encapsulation and distinguish
	// between class and instance members; this helps to ensure that the instance is initialized (via the "getInstance" method) before instance dependent
	// members are accessed
	private final JTextArea textArea = new JTextArea();
	private final UndoManager undoManager = new UndoManager();
	private String lastSavedText = "";
	private boolean wasEmpty = true;
	
	private String textToFind = "";  // note that like other complex types, strings are Null by default
	private String replacementText = "";
	private int findFromIndex, findToIndex;
	private boolean findMatchCase = true, findWrapAround = true, findDown = true;
	
	private static final int FONT_SIZE_MIN = 8;
	private static final int FONT_SIZE_MAX = 72;
	
	private static final int ZOOM_DEFAULT = 100;
	private static final int ZOOM_MIN = 10;
	private static final int ZOOM_MAX = 500;
	private static final int ZOOM_AMOUNT = 10;
	
	private int baseFontSize;
	private int zoom = ZOOM_DEFAULT;
	
	// use the getInstance method instead of the instance variable directly to ensure instance is initialized
	public static final Runnable UNDO = () -> getInstance().undo();
	public static final Runnable REDO = () -> getInstance().redo();
	public static final Runnable PROMPT_FIND = () -> getInstance().openFindPane();
	public static final Runnable FIND_NEXT = () -> getInstance().findNext(true);
	public static final Runnable FIND_PREV = () -> getInstance().findPrevious();
	public static final Runnable PROMPT_REPLACE = () -> getInstance().openReplacePane();
	public static final Runnable REPLACE_NEXT = () -> getInstance().replaceNext();
	public static final Runnable REPLACE_ALL = () -> getInstance().replaceAll();
	public static final Runnable TOGGLE_WRAP = () -> getInstance().toggleTextWrapping();
	public static final Runnable CHANGE_FONT = () -> getInstance().openFontPane();
	public static final Runnable ZOOM_IN = () -> getInstance().zoom(true);
	public static final Runnable ZOOM_OUT = () -> getInstance().zoom(false);
	public static final Runnable RESET_ZOOM = () -> getInstance().resetZoom();
	
	private AppTextArea() {
		final int px = 10;
		
		textArea.setMargin(new Insets(px, px, px, px));
		baseFontSize = textArea.getFont().getSize();
		
		// keep track of changes made to the content of the component's document; note that while DocumentListener can also listen for manual typing and
		// removal of characters, it does not listen for pasting events; UndoableEditListener can also be used for creating instances of UndoableEdit which
		// can then be stored in an UndoManager instance for managing undo and redo operations; also, note that UndoableEditListener can only be added
		// directly to a Document, as it's intended to be used only for text-based operations
		textArea.getDocument().addUndoableEditListener(new UndoableEditListener() {
			@Override
			public void undoableEditHappened(UndoableEditEvent e) {
				UndoableEdit edit = e.getEdit();
				String editType = edit.getPresentationName();
				
				// TODO: create custom class that extends AbstractUndoableEdit and override isSignificant() method
				if (edit.isSignificant()) {
					// enable the undo menu item if it was previously disabled, then add the UndoableEdit to the UndoManager
					if (!undoManager.canUndo()) AppMenuBar.getInstance().setItemEnabled(1, 0, true);
					undoManager.addEdit(edit);
				}
				// "addition" includes both typing and pasting
				if (editType.equals("addition")) wasEmpty = !AppMenuBar.getInstance().getItemEnabled(1, 2);
				else wasEmpty = false;
				
				updateText();
			}
		});
		// CaretListener keeps track of changes related to the text caret, such as visibility or focus, and calls the "caretUpdate" method whenever it
		// does detect a change; in this case, it's used for updating the information on the status bar whenever the caret's position changes
		textArea.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) { StatusBar.getInstance().updateCaretInfo(); }
		});
		// by default, (Ctrl / Command) + H acts like backspace in text-based components (JTextArea, JTextField, etc.), deleting the character before the
		// text cursor; this prevents other actions with the same key binding from being performed (in this case, opening the replace dialog); note that
		// this is unique to OpenJDK and is not a feature of the standard JDK
		textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_H, Main.SHORTCUT_KEY), "none");
		getViewport().setView(textArea);
		
		// set a limit for UndoManager to only keep the 100 most recent UndoableEdit instances; any new edits added to either the undo or redo Deque after
		// the limit is reached will result in the last edit being removed from its Deque
		undoManager.setLimit(100);
	}
	
	public static AppTextArea getInstance() {
		if (instance == null) instance = new AppTextArea();
		return instance;
	}
	
	private void updateText() {
		boolean changesMade = !textArea.getText().equals(lastSavedText);
		boolean isEmpty = textArea.getText().isEmpty();
		
		if (changesMade) findFromIndex = findToIndex = 0;
		
		// disable menu items related to finding and replacing text if text has been changed to be empty; otherwise, enable them if text used to be
		// empty; also, disable the "New" menu item if there is no longer any progress at all (no file has been loaded and text is empty); otherwise,
		// enable it if there used to be no progress at all
		if ((wasEmpty && !isEmpty) || (!wasEmpty && isEmpty)) {
			if (!FileManager.getInstance().getFileLoaded())
				AppMenuBar.getInstance().setItemEnabled(0, 0, wasEmpty);
			
			for (int i = 2; i < AppMenuBar.getInstance().getNumMenuItems(1); i++)
				AppMenuBar.getInstance().setItemEnabled(1, i, wasEmpty);
		}
		FileManager.getInstance().setSaved(!changesMade);
		StatusBar.getInstance().updateTextInfo();
	}
	
	private void undo() {
		wasEmpty = textArea.getText().isEmpty();
		
		if (!undoManager.canRedo()) AppMenuBar.getInstance().setItemEnabled(1, 1, true);  // enable redo menu item if it was previosuly disabled
		if (undoManager.canUndo()) undoManager.undo();
		if (!undoManager.canUndo()) AppMenuBar.getInstance().setItemEnabled(1, 0, false);  // disable undo menu item if there aren't any more undos left
		updateText();
	}
	
	private void redo() {
		wasEmpty = textArea.getText().isEmpty();
		
		if (!undoManager.canUndo()) AppMenuBar.getInstance().setItemEnabled(1, 0, true);
		if (undoManager.canRedo()) undoManager.redo();
		if (!undoManager.canRedo()) AppMenuBar.getInstance().setItemEnabled(1, 1, false);
		updateText();
	}
	
	private void openFindPane() {
		// close any panes that may already be open before opening the new one; note that Java does not support implicit boolean expressions, meaning
		// variables that are not of the boolean type must have their values checked explicitly
		if (Main.activePane != null) Main.activePane.dispose();
		Main.activePane = new FindPane(Main.getFrame());
	}
	
	private boolean findNext(boolean showTextNotFound) {
		int resultStart = findDown ? findDown() : findUp();  // get the starting index of an occurrence of the text found (-1 if not found)
		final int textLength = textToFind.length();
		
		if (resultStart == -1) {
			// if the wrapping option is enabled, reset the appropriate index limit and search again for the text before concluding there aren't any
			// occurrences of it
			if (findWrapAround) {
				if (findDown) findFromIndex = 0;
				else findToIndex = textArea.getText().length();
				
				resultStart = findDown ? findDown() : findUp();
				
				if (resultStart != -1) {
					selectTextOccurrence(resultStart, textLength, true);
					return true;
				}
			}
			if (showTextNotFound) showTextNotFound();
			return false;
		}
		else {
			selectTextOccurrence(resultStart, textLength, true);
			return true;
		}
	}
	
	private void findPrevious() {
		if (findFromIndex > findToIndex) {
			int temp = findFromIndex;
			findFromIndex = 0;
			findToIndex = temp;
		}
		int resultStart = findDown ? findUp() : findDown();
		final int textLength = textToFind.length();
		
		System.out.println(findFromIndex + ", " + findToIndex);
		
		if (resultStart == -1) {
			if (findWrapAround) {
				if (findDown) findToIndex = textArea.getText().length();
				else findToIndex = textArea.getText().length();
				
				resultStart = findDown ? findUp() : findDown();
				System.out.println(findFromIndex + ", " + findToIndex);
				
				if (resultStart != -1) {
					selectTextOccurrence(resultStart, textLength, false);
					return;
				}
			} showTextNotFound();
		}
		else selectTextOccurrence(resultStart, textLength, false);
	}
	
	// returns the starting index of the text found, going downwards; returns -1 if no match was found
	private int findDown() {
		if (findMatchCase)
			return textArea.getText().indexOf(textToFind, findFromIndex);  // start search from the specified index value
		else
			return textArea.getText().toLowerCase().indexOf(textToFind.toLowerCase(), findFromIndex);
	}
	
	// note that while the "indexOf" of and "lastIndexOf" methods can take an optional argument to specify the index to start searching from, neither
	// method has a parameter for an optional argument to specify the index to stop the search at
	private int findUp() {
		String srcText = textArea.getText().substring(findFromIndex, findToIndex);
		
		if (findMatchCase)
			return srcText.lastIndexOf(textToFind);
		else
			return srcText.toLowerCase().lastIndexOf(textToFind.toLowerCase());
	}
	
	private void selectTextOccurrence(int startIndex, int textLength, boolean next) {
		int endIndex = startIndex + textLength;
		textArea.select(startIndex, endIndex);
		
		// shift the appropriate index limit, so that the next text found isn't the same one; note that finding the next occurrence with the direction
		// set to down is the same as finding the previous occurrence with the direction set to up, since in both cases, the selection moves down;
		// similarly, finding the previous occurrence with the direction set to down is the same as finding the next occurrence with the direction set
		// to up, since in both cases, the selection moves up
		if ((findDown && next) || (!findDown && !next)) findFromIndex = startIndex + 1;
		else findToIndex = endIndex - 1;
	}
	
	private String getShortQuotedText(String text) {
		return "\"" + (text.length() > 10 ? (text.substring(0, 10) + "...") : text) + "\"";
	}
	
	private void showTextNotFound() {
		final String text = getShortQuotedText(textToFind);
		
		JOptionPane.showOptionDialog(this, "Could not find " + text, "Text Not Found",
				JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, JOptionPane.OK_OPTION);
	}
	
	private void openReplacePane() {
		if (Main.activePane != null) Main.activePane.dispose();
		Main.activePane = new ReplacePane(Main.getFrame());
	}
	
	private void replaceNext() {
		if (findNext(true)) textArea.replaceSelection(replacementText);
	}
	
	private void replaceAll() {
		int count = 0;
		
		while (findNext(false)) {
			textArea.replaceSelection(replacementText);
			count++;
		}
		if (count == 0) showTextNotFound();
		else {
			String text = getShortQuotedText(textToFind);
			
			JOptionPane.showOptionDialog(this, "Replaced " + count + " occurrence" + (count == 1 ? "" : "s") + " of " + text, "Replace All",
					JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, JOptionPane.OK_OPTION);
		}
	}
	
	private void toggleTextWrapping() {
		boolean wrapped = textArea.getLineWrap();
		int scrollPolicy = wrapped ? JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED : JScrollPane.HORIZONTAL_SCROLLBAR_NEVER;
		
		textArea.setLineWrap(!wrapped);
		textArea.setWrapStyleWord(!wrapped);
		setHorizontalScrollBarPolicy(scrollPolicy);
	}
	
	private void openFontPane() {
		new FontPane(Main.getFrame());
	}
	
	private void zoom(boolean in) {
		final int zoomAmount = Math.max(baseFontSize / ZOOM_AMOUNT, 1);  // ensure the font size changes by at least 1 pixel
		
		if (in && zoom < ZOOM_MAX) {
			zoom += ZOOM_AMOUNT;
			setFontSize(textArea.getFont().getSize() + zoomAmount);
		}
		else if (!in && zoom > ZOOM_MIN) {
			zoom -= ZOOM_AMOUNT;
			setFontSize(textArea.getFont().getSize() - zoomAmount);
		}
		StatusBar.getInstance().updateZoomInfo();
	}
	
	private void resetZoom() {
		if (zoom != ZOOM_DEFAULT) {
			zoom = ZOOM_DEFAULT;
			setFontSize(baseFontSize);
			StatusBar.getInstance().updateZoomInfo();
		}
	}
	
	private void setFontSize(int size) {
		Font f = textArea.getFont();
		
		if (size != f.getSize())
			textArea.setFont(new Font(f.getFamily(), f.getStyle(), size));
	}
	
	public String getText() {
		return textArea.getText();
	}
	
	public void setTextContent(String text) {
		textArea.setText(text);
		wasEmpty = text.length() == 0;
		findFromIndex = findToIndex = 0;  // reset the indices for the range to search for text in
		undoManager.discardAllEdits();
		updateText();
	}
	
	public void clearText() {
		setTextContent("");
	}
	
	public void saveChanges() {
		lastSavedText = textArea.getText();
	}
	
	public int getLineCount() {
		return textArea.getLineCount();
	}
	
	public int getCaretPosition() {
		return textArea.getCaretPosition();
	}
	
	public int getLineOfOffset(int offset) throws BadLocationException {
		return textArea.getLineOfOffset(offset);
	}
	
	public int getLineStartOffset(int line) throws BadLocationException {
		return textArea.getLineStartOffset(line);
	}
	
	public String getTextToFind() {
		return textToFind;
	}
	
	public void setTextToFind(String textToFind) {
		this.textToFind = textToFind;
	}
	
	public int getSelectionStart() {
		return textArea.getSelectionStart();
	}
	
	public int getSelectionEnd() {
		return textArea.getSelectionEnd();
	}
	
	public void setFindFromIndex(int i) {
		findFromIndex = i;
	}
	
	public void setFindToIndex(int i) {
		findToIndex = i;
	}
	
	public boolean getFindMatchCase() {
		return findMatchCase;
	}
	
	public void setFindMatchCase(boolean findMatchCase) {
		this.findMatchCase = findMatchCase;
	}
	
	public boolean getFindWrapAround() {
		return findWrapAround;
	}
	
	public void setFindWrapAround(boolean findWrapAround) {
		this.findWrapAround = findWrapAround;
	}
	
	public boolean getFindDown() {
		return findDown;
	}
	
	public void setFindDown(boolean findDown) {
		this.findDown = findDown;
	}
	
	public String getReplacementText() {
		return replacementText;
	}
	
	public void setReplacementText(String replacementText) {
		this.replacementText = replacementText;
	}
	
	public Font getTextFont() {
		return textArea.getFont();
	}
	
	public int getBaseFontSize() {
		return baseFontSize;
	}
	
	public void setTextFont(String family, int style, int size) {
		baseFontSize = Math.clamp(size, FONT_SIZE_MIN, FONT_SIZE_MAX);
		textArea.setFont(new Font(family, style, baseFontSize + ((zoom - ZOOM_DEFAULT) / ZOOM_AMOUNT)));
	}
	
	public int getZoom() {
		return zoom;
	}
}
