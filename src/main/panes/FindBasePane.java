package main.panes;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.UndoManager;

import main.AppTextArea;
import main.TextInput;

// due to the many similarities between the find and replace panes, this abstract class has been created to serve as the base class for both of them
@SuppressWarnings("serial")
public abstract class FindBasePane extends Pane {
	
	protected final JLabel findLabel = new JLabel("Find what:");
	protected final JCheckBox[] checkBoxes = { new JCheckBox("Match case"), new JCheckBox("Wrap around") };
	protected final GroupLayout layout;
	
	// anonymous classes in Java provide a way to override methods of an existing class or interface without explicitly creating a new class, making
	// it useful for providing instance-specific implementation
	protected final TextInput findField = new TextInput(AppTextArea.getInstance().getTextToFind(), primaryButton) {
		@Override
		public void onEdit(boolean addedText) {
			String text = findField.getText();
			
			// update the text to find and the indices for the range to search in
			AppTextArea.getInstance().setTextToFind(text);
			AppTextArea.getInstance().setFindFromIndex(0);
			AppTextArea.getInstance().setFindToIndex(0);
			
			// enable or disable the find button, depending on whether the text field's text was or is empty
			if (addedText) {
				if (!primaryButton.isEnabled())
					primaryButton.setEnabled(true);
			}
			else if (text.isEmpty()) primaryButton.setEnabled(false);
		}
	};
	
	protected FindBasePane(JFrame owner, String title) {
		super(owner, title, new Dimension(400, 175), new Dimension(84, 20));
		
		final UndoManager undoManager = new UndoManager();
		undoManager.setLimit(100);
		
		// having label associated with another component will select that other component when the label's mnemonic is pressed, since label itself
		// cannot be selected; note that a mnemonic cannot be assigned to JTextField
		findLabel.setLabelFor(findField);
		findLabel.setDisplayedMnemonic(KeyEvent.VK_N);
		
		primaryButton.setText("Find Next");
		primaryButton.setMnemonic(KeyEvent.VK_F);
		
		if (findField.getText().isEmpty())
			primaryButton.setEnabled(false);
		
		initFindActions();
		initCheckBoxes();
		
		layout = new GroupLayout(getContentPane());
		layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        setLayout(layout);
	}
	
	private void initFindActions() {
		// attempt to find text occurrence only if the find button is enabled; note that because the behaviour of the find actions here are different
		// from those of the JFrame, they are created as separate AbstractAction objects
		final AbstractAction findNext = new AbstractAction("Find Next") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (primaryButton.isEnabled()) find(true);
			}
		};
		final AbstractAction findPrev = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (primaryButton.isEnabled()) find(false);
			}
		};
		primaryButton.addActionListener(findNext);
		
		for (int i = 0; i < 2; i++) {
			final String actionKey = (i == 0 ? "findNext" : "findPrev");
			final int modifiers = (i == 0 ? 0 : InputEvent.SHIFT_DOWN_MASK);
			
			getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, modifiers), actionKey);
			getRootPane().getActionMap().put(actionKey, i == 0 ? findNext : findPrev);
		}
	}
	
	private void find(boolean next) {
		// reset the text find range if the text to search for has changed
		if (!AppTextArea.getInstance().getTextToFind().equals(findField.getText())) {
			AppTextArea.getInstance().setTextToFind(findField.getText());
			
			if (AppTextArea.getInstance().getFindDown() && next)
				AppTextArea.getInstance().setFindToIndex(AppTextArea.getInstance().getText().length());
			else
				AppTextArea.getInstance().setFindFromIndex(0);
		}
		if (next) AppTextArea.FIND_NEXT.run();
		else AppTextArea.FIND_PREV.run();
	}
	
	// note that similar to the value of the text field, the check box selections are also based off of variables from the "AppTextArea", meaning
	// that changing any one of these 3 values in either the find or replace pane will change it for the other pane as well
	private void initCheckBoxes() {
		for (int i = 0; i < checkBoxes.length; i++) {
			checkBoxes[i].setMnemonic(i == 0 ? KeyEvent.VK_C : KeyEvent.VK_R);
			checkBoxes[i].setSelected(i == 0 ? AppTextArea.getInstance().getFindMatchCase() : AppTextArea.getInstance().getFindWrapAround());
		}
		checkBoxes[0].addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				AppTextArea.getInstance().setFindMatchCase(checkBoxes[0].isSelected());
			}
		});
		checkBoxes[1].addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				AppTextArea.getInstance().setFindWrapAround(checkBoxes[1].isSelected());
			}
		});
	}
	
	protected abstract void setLayoutGroups();
}
