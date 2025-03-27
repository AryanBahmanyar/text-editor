package main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import main.panes.Pane;

@SuppressWarnings("serial")
public class Main extends JFrame {
	
	public static final int SHORTCUT_KEY = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
	public static final boolean ON_MAC = System.getProperty("os.name").toLowerCase().contains("mac");
	public static Pane activePane = null;
	
	public Main() {
		FileManager.getInstance().updateFrameTitle();  // initialize the single FileManager instance while setting the JFrame title
		
		setPreferredSize(new Dimension(800, 500));
		getContentPane().setLayout(new BorderLayout());
		getContentPane().setLayout(new BorderLayout());
		
		// don't immediately close the JFrame by default when exiting (custom behaviour defined with WindowAdapter below)
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		getContentPane().add(AppMenuBar.getInstance(), BorderLayout.NORTH);
		getContentPane().add(AppTextArea.getInstance(), BorderLayout.CENTER);
		getContentPane().add(StatusBar.getInstance(), BorderLayout.SOUTH);
		
		// because the program needs to check if the user wants to save their progress (if it isn't already) before exiting the program, the exit method
		// is a member of the FileManager class
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) { FileManager.getInstance().exit(); }
		});
		// bind a key stroke (shortcut) to an action map key ("close"); note that Alt + f4 (for Windows and Linux) and Command + Q (for Mac) don't need
		// to be explicitly registered since they're both OS-specific shortcuts, rather than application-specific ones; also, note that on Mac,
		// Command + W is also system-specific, whereas on Windows and Linux, Ctrl + W is application-specific
		if (!ON_MAC)
			getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_W, SHORTCUT_KEY), "close");
		
		// bind an action to the action map key defined above
		getRootPane().getActionMap().put("close", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) { FileManager.getInstance().exit(); }
		});
		initFindActions();
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void initFindActions() {
		final AbstractAction findNext = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (AppTextArea.getInstance().getTextToFind().length() == 0) AppTextArea.PROMPT_FIND.run();  // open the find pane if the text to find is empty
				else AppTextArea.FIND_NEXT.run();
			}
		};
		final AbstractAction findPrev = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (AppTextArea.getInstance().getTextToFind().length() == 0) AppTextArea.PROMPT_FIND.run();
				else AppTextArea.FIND_PREV.run();
			}
		};
		for (int i = 0; i < 2; i++) {
			final String actionKey = (i == 0 ? "findNext" : "findPrev");
			final int modifiers = (i == 0 ? 0 : InputEvent.SHIFT_DOWN_MASK);
			
			getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, modifiers), actionKey);
			getRootPane().getActionMap().put(actionKey, i == 0 ? findNext : findPrev);
		}
	}
	
	public static JFrame getFrame() {
		return (JFrame) getFrames()[0];
	}
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());  // adopt the default UI look and feel of the system
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		// the method / constructor reference operator (::) introduced in Java 8 allows a method or constructor to be called as a lambda expression; in the
		// statement below, Main::new is the same as using the lambda expression: () -> new Main; note that this operator is not a replacement for more
		// complex lambda expressions and is only a shorthand for invoking a method or constructor
		SwingUtilities.invokeLater(Main::new);
	}
}
