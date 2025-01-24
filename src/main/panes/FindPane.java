package main.panes;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import main.AppTextArea;

@SuppressWarnings("serial")
public final class FindPane extends FindBasePane {
	
	private final JPanel dirPanel = new JPanel();
	private final JPanel checkBoxPanel = new JPanel();  // the replace pane does not use a panel for its checkboxes due its layout
	
	public FindPane(JFrame owner) {
		super(owner, "Find");
		checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
		
		for (int i = 0; i < checkBoxes.length; i++)
			checkBoxPanel.add(checkBoxes[i]);
		
		initRadioButtons();
		setLayoutGroups();
		start(owner);
	}
	
	private void initRadioButtons() {
		final ButtonGroup buttonGroup = new ButtonGroup();
		final JRadioButton[] radioButtons = { new JRadioButton("Up"), new JRadioButton("Down") };
		
		dirPanel.setMaximumSize(new Dimension(100, 30));
		dirPanel.setLayout(new BorderLayout());
		dirPanel.add(new JLabel("Direction:"), BorderLayout.NORTH);
		
		// note that ButtonGroup is used for grouping buttons (in this case, radio buttons) logically, so that only one of them can remain selected at
		// a time; for grouping buttons visually, they should still be added to a JPanel
		for (int i = 0; i < radioButtons.length; i++) {
			buttonGroup.add(radioButtons[i]);
			radioButtons[i].setMnemonic(i == 0 ? KeyEvent.VK_U : KeyEvent.VK_D);
			dirPanel.add(radioButtons[i], i == 0 ? BorderLayout.WEST : BorderLayout.EAST);
		}
		// because there are only 2 radio buttons, it's slightly more efficient to have just 1 ChangeListener that listens for both selections and
		// deselections on 1 of the radio buttons, rather than having a ChangeListener for both radio buttons that only listens for selections
		radioButtons[0].addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// both the "getSelectionStart" and "getSelectionEnd" methods return 0 if there's no text selected
				final int selectionStart = AppTextArea.getInstance().getSelectionStart();
				final int selectionEnd = AppTextArea.getInstance().getSelectionEnd();
				final boolean textSelected = selectionStart != selectionEnd;
				final int textLength = AppTextArea.getInstance().getText().length();
				
				// change the range of indices to search for matching text if the 2 radio buttons have changed in selection
				if (((JRadioButton) e.getSource()).isSelected()) {
					AppTextArea.getInstance().setFindDown(false);
					AppTextArea.getInstance().setFindFromIndex(0);
					AppTextArea.getInstance().setFindToIndex(textSelected ? selectionEnd - 1 : textLength);
				} else {
					AppTextArea.getInstance().setFindDown(true);
					AppTextArea.getInstance().setFindFromIndex(textSelected ? selectionStart + 1 : 0);
					AppTextArea.getInstance().setFindToIndex(textLength);
				}
			}
		}); radioButtons[AppTextArea.getInstance().getFindDown() ? 1 : 0].setSelected(true);
	}
	
	// note that GroupLayout requires components to be added twice (once for horizontal and once for vertical); however, this is required only for the
    // layout creation process, and the end result is not a duplicate of each component; note that if there are mismatches between the components in
	// the horizontal and vertical groups, or if any components are null, an IllegalStateException will be thrown
	@Override
	protected final void setLayoutGroups() {
		// set horizontal group
		layout.setHorizontalGroup(layout.createSequentialGroup()
        	.addGroup(layout.createParallelGroup(Alignment.LEADING)  // horizontally align group components at their left edges
        		.addComponent(findLabel)
        		.addComponent(checkBoxPanel))
        	.addGroup(layout.createParallelGroup(Alignment.CENTER)  // horizontall align group components at their center
        		.addComponent(findField)
        		.addComponent(dirPanel))
        	.addGroup(layout.createParallelGroup(Alignment.TRAILING)  // horizontally align group components at their right edges
        		.addComponent(primaryButton)
        		.addComponent(cancelButton))
        	.addContainerGap());
		
        // set vertical group
        layout.setVerticalGroup(layout.createSequentialGroup()
        	.addGroup(layout.createParallelGroup(Alignment.BASELINE)  // vertically align group components at their baselines
        		.addComponent(findLabel)
        		.addComponent(findField)
        		.addComponent(primaryButton))
        	.addGroup(layout.createParallelGroup(Alignment.LEADING)  // vertically align group components at their top edges
        		.addComponent(checkBoxPanel)
        		.addComponent(dirPanel)
        		.addComponent(cancelButton))
    		.addContainerGap());
	}
	
	@Override
	protected final void onClose() {}
}
