package main.panes;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import main.AppTextArea;
import main.TextInput;

@SuppressWarnings("serial")
public class ReplacePane extends FindBasePane {
	
	final JLabel replaceLabel =  new JLabel("Replace with:");
	final JButton[] replaceButtons = new JButton[2];
	
	// because the replace button is supposed to be enabled at all times, it's not passed to replaceField as the associated button
	final TextInput replaceField = new TextInput(AppTextArea.getInstance().getReplacementText()) {
		@Override
		public void onEdit(boolean addedText) {
			AppTextArea.getInstance().setReplacementText(replaceField.getText());
		}
	};
	
	public ReplacePane(JFrame owner) {
		super(owner, "Replace");
		replaceLabel.setLabelFor(replaceField);
		replaceLabel.setDisplayedMnemonic(KeyEvent.VK_P);
		
		for (int i = 0; i < replaceButtons.length; i++) {
			replaceButtons[i] = new JButton();
			replaceButtons[i].setMinimumSize(buttonSize);
			replaceButtons[i].setMnemonic(i == 0 ? KeyEvent.VK_R : KeyEvent.VK_A);
		}
		initReplaceActions();
		setLayoutGroups();
		start(owner);
	}
	
	private void initReplaceActions() {
		// note that AbstractAction can take name value which replaces the label of the JButton it gets assigned to; it can also be assigned other
		// values, such as mnemonic key, icon, and a tool tip
		final AbstractAction replace = new AbstractAction("Replace") {
			@Override
			public void actionPerformed(ActionEvent e) { AppTextArea.REPLACE_NEXT.run(); }
		};
		final AbstractAction replaceAll = new AbstractAction("Replace All") {
			@Override
			public void actionPerformed(ActionEvent e) { AppTextArea.REPLACE_ALL.run(); }
		};
		replaceButtons[0].setAction(replace);
		replaceButtons[1].setAction(replaceAll);
	}
	
	@Override
	protected final void setLayoutGroups() {
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.LEADING)
				.addComponent(findLabel)
	        	.addComponent(replaceLabel)
	        	.addComponent(checkBoxes[0])
	        	.addComponent(checkBoxes[1]))
	        .addGroup(layout.createParallelGroup(Alignment.CENTER)
	        	.addComponent(findField)
	            .addComponent(replaceField))
	        .addGroup(layout.createParallelGroup(Alignment.TRAILING)
	        	.addComponent(primaryButton)
	        	.addComponent(replaceButtons[0])
	        	.addComponent(replaceButtons[1])
	        	.addComponent(cancelButton))
	        .addContainerGap());
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(findLabel)
				.addComponent(findField)
				.addComponent(primaryButton))
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(replaceLabel)
				.addComponent(replaceField)
				.addComponent(replaceButtons[0]))
			.addGroup(layout.createParallelGroup(Alignment.LEADING)
				.addComponent(checkBoxes[0])
				.addComponent(replaceButtons[1]))
			.addGroup(layout.createParallelGroup(Alignment.LEADING)
				.addComponent(checkBoxes[1])
				.addComponent(cancelButton))
			.addContainerGap());
	}
	
	@Override
	protected void onClose() {}
}
