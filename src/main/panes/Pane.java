package main.panes;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import main.AppMenuBar;
import main.Main;

// note that "Dialog" cannot be used as the identifier of this class due to Dialog being the Advanced Window Toolkit (AWT) class that Swing's JDialog
// extends from; while the same identifiers can be used in many cases, they cannot in this case, since it creates ambiguity
@SuppressWarnings("serial")
public abstract class Pane extends JDialog {
	
	protected JButton primaryButton = new JButton();
	protected JButton cancelButton = new JButton();
	protected final Dimension buttonSize;
	
	private boolean closing = false;
	
	protected Pane(JFrame owner, String title, Dimension paneSize, Dimension buttonSize) {
		super(owner, title, false);
		this.buttonSize = buttonSize;
		
		setPreferredSize(paneSize);
		setResizable(false);
		
		// setting a button as the default invokes its assigned action whenever the enter key is pressed, while the JDialog is in focus
		getRootPane().setDefaultButton(primaryButton);
		
		primaryButton.setMinimumSize(buttonSize);
		primaryButton.setPreferredSize(buttonSize);
		cancelButton.setMinimumSize(buttonSize);
		cancelButton.setPreferredSize(buttonSize);
        
        initActions();
		addExternalActions();
	}
	
	private void initActions() {
		// the "dispose" method invokes the "windowClosing" method of the window listener
		final AbstractAction close = new AbstractAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				closing = true;
				onClose();
				dispose();
			}
		};
		final int[] keyCodes = { KeyEvent.VK_ESCAPE, KeyEvent.VK_W };
		final int[] modifiers = { 0, Main.SHORTCUT_KEY };
		
		// note that multiple key strokes can be bound to the same action; the action itself would only need to be registered once
		for (int i = 0; i < keyCodes.length; i++) {
			getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCodes[i], modifiers[i]), "close");
			if (Main.ON_MAC) break;
		}
		getRootPane().getActionMap().put("close", close);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closing = true;
				onClose();
				dispose();
			}
		}); cancelButton.setAction(close);
	}
	
	// add the file and view menu actions along with their key strokes to this pane, so that the keystrokes work regardless of whether the focus is on
	// the JFrame or the pane
	private void addExternalActions() {
		int count = 0;
		
		for (int i = 0; i < 4; i += 3) {
			for (int j = 0; j < AppMenuBar.getInstance().getNumMenuItems(i); j++) {
				final int keyCode = AppMenuBar.getInstance().getKeyCode(i, j);
				final int modifiers = AppMenuBar.getInstance().getModifier(i, j);
				final Runnable action = AppMenuBar.getInstance().getAction(i, j);
				final String actionKey = "action" + (count++);
				
				// note that the action map key does not necessarily need to be descriptive; it only has to match a keystroke with an action
				getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, modifiers), actionKey);
				getRootPane().getActionMap().put(actionKey, new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) { action.run(); }
				});
			}
		}
	}
	
	protected void start(JFrame owner) {
		pack();
		setLocationRelativeTo(owner);
		setVisible(true);
	}
	
	// note that an abstract method cannot be marked as final as it's supposed to be overriden; however, its overriden method in a derived class can be
	protected abstract void onClose();
	
	protected boolean closing() {
		return closing;
	}
}
