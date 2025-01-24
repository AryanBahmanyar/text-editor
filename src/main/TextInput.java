package main;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

@SuppressWarnings("serial")
public class TextInput extends JTextField {
	
	// this class improves on JTextField by implementing an UndoManager that will store edits (UndoableEdit instances) made to the text; edits can be
	// undone and redone by using their respective shortcuts; the (Ctrl / Command) + H key stroke is also disabled for OpenJDK; the button represents
	// the associated button that will be disabled whenever the text becomes empty, and enabled in the opposite scenario; button can be null if no
	// buttons are to be associated with an instance of this class
	
	private final UndoManager undoManager = new UndoManager();
	private final JButton button;
	
	public TextInput(String text, JButton button) {
		super(text);
		this.button = button;
		init();
	}
	
	// note that Java does not support default argument values, meaning that overloading (constructor overloading, in this case) must be used to
	// provide flexibility for the list of parameters
	public TextInput(String text) {
		super(text);
		button = null;
		init();
	}
	
	public TextInput() {
		button = null;
		init();
	}
	
	private void init() {
		final int[] keyCodes = { KeyEvent.VK_Z, KeyEvent.VK_Y, KeyEvent.VK_H };
		final String[] actionKeys = { "undo", "redo", "none" };
		
		getDocument().addUndoableEditListener(new UndoableEditListener() {
			@Override
			public void undoableEditHappened(UndoableEditEvent e) {
				UndoableEdit edit = e.getEdit();
				boolean addedText = edit.getPresentationName().equals("addition");
				
				undoManager.addEdit(edit);
				onEdit(addedText);
			}
		});
		getActionMap().put("undo", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (undoManager.canUndo()) {
					if (button != null) revert(true);
					else undoManager.undo();
				}
			}
		});
		getActionMap().put("redo", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (undoManager.canRedo()) {
					if (button != null) revert(false);
					else undoManager.redo();
				}
			}
		});
		for (int i = 0; i < keyCodes.length; i++)
			getInputMap().put(KeyStroke.getKeyStroke(keyCodes[i], Main.SHORTCUT_KEY), actionKeys[i]);
	}
	
	private void revert(boolean undo) {
		boolean wasEmpty = getText().isEmpty();
		
		if (undo) undoManager.undo();
		else undoManager.redo();
		
		if (getText().isEmpty()) button.setEnabled(false);
		else if (wasEmpty) button.setEnabled(true);
	}
	
	protected void onEdit(boolean addedText) {}
}
