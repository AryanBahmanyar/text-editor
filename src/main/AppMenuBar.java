package main;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

// in OOP design patterns, a singleton is a class that can have only a single instance; also, note that a final class is one that cannot be inherited /
// extended from
@SuppressWarnings("serial")
public final class AppMenuBar extends JMenuBar {
	
	private static AppMenuBar instance;  // variable to hold the single instance
	
	private final JMenu[] menus = {
		new JMenu("File"),
		new JMenu("Edit"),
		new JMenu("Format"),
		new JMenu("View")
	};
	
	// note that List.of creates an immutable list; also, note that collections in Java cannot be of primitive types; because a raw array is not considered a
	// primitve type in Java, int[] can be used as a collection type; however, if a collection were to be of type int, for example, the type's wrapper class,
	// Integer, would have to be used instead
	private final List<int[]> itemKeyCodes = List.of(
		new int[] { KeyEvent.VK_N, KeyEvent.VK_O, KeyEvent.VK_S, KeyEvent.VK_S, 0 },
		new int[] { KeyEvent.VK_Z, KeyEvent.VK_Y, KeyEvent.VK_F, KeyEvent.VK_F3, KeyEvent.VK_F3, KeyEvent.VK_H },
		new int[] { 0, 0 },
		new int[] { KeyEvent.VK_EQUALS, KeyEvent.VK_MINUS, KeyEvent.VK_0 }
	);
	private final List<int[]> modifiers = List.of(
		new int[] { Main.SHORTCUT_KEY, Main.SHORTCUT_KEY, Main.SHORTCUT_KEY, Main.SHORTCUT_KEY, 0 },
		new int[] { Main.SHORTCUT_KEY, Main.SHORTCUT_KEY, Main.SHORTCUT_KEY, 0, InputEvent.SHIFT_DOWN_MASK, Main.SHORTCUT_KEY },
		new int[] { 0, 0 },
		new int[] { Main.SHORTCUT_KEY, Main.SHORTCUT_KEY, Main.SHORTCUT_KEY }
	);
	private final List<Runnable[]> actions = List.of(
		new Runnable[] { FileManager.START_NEW, FileManager.OPEN, FileManager.SAVE, FileManager.SAVE_AS, FileManager.EXIT },
		new Runnable[] { AppTextArea.UNDO, AppTextArea.REDO, AppTextArea.PROMPT_FIND, AppTextArea.FIND_NEXT, AppTextArea.FIND_PREV, AppTextArea.PROMPT_REPLACE },
		new Runnable[] { AppTextArea.TOGGLE_WRAP, AppTextArea.CHANGE_FONT },
		new Runnable[] { AppTextArea.ZOOM_IN, AppTextArea.ZOOM_OUT, AppTextArea.RESET_ZOOM }
	);
	
	// like private variables and methods, private constructors are only accessible within their own class; this ensures that no instances of the class
	// are created outside of it
	private AppMenuBar() {
		final int[] mnemonicKeyCodes = { KeyEvent.VK_F, KeyEvent.VK_E, KeyEvent.VK_O, KeyEvent.VK_V };
		
		// parallel lists of arrays
		final List<String[]> itemLabels = List.of(
			new String[] { "New", "Open", "Save", "Save As", "Exit" },
			new String[] { "Undo", "Redo", "Find", "Find Next", "Find Previous", "Replace" },
			new String[] { "Text Wrapping", "Font" },
			new String[] { "Zoom In", "Zoom Out", "Restore Default Zoom" }
		);
		for (int i = 0; i < menus.length; i++) {
			menus[i].setMnemonic(mnemonicKeyCodes[i]);
			
			for (int j = 0; j < itemLabels.get(i).length; j++) {
				final JMenuItem menuItem;
				
				// JCheckBoxMenuItem is derived from JMenuItem
				if (i == 2 && j == 0) menuItem = new JCheckBoxMenuItem(itemLabels.get(i)[j]);
				else menuItem = new JMenuItem(itemLabels.get(i)[j]);
				
				final int keyCode = itemKeyCodes.get(i)[j];
				final int modifier = modifiers.get(i)[j];
				final int k = i, l = j;
				
				// assign key strokes (shortcuts)
				if (!(keyCode == 0 && modifier == 0))
					menuItem.setAccelerator(KeyStroke.getKeyStroke(keyCode, modifier));
				
				menuItem.addActionListener(e -> actions.get(k)[l].run());
				menus[i].add(menuItem);
			}
			add(menus[i]);
		}
		setItemEnabled(0, 0, false);  // disble the "New" menu item since the app starts with a new progress
		
		// disable the edit menu items since there is no text or changes made to start with
		for (int i = 0; i < getNumMenuItems(1); i++)
			setItemEnabled(1, i, false);
	}
	
	// initialize single instance if it's not initialized already, before returning it
	public static AppMenuBar getInstance() {
		if (instance == null) instance = new AppMenuBar();
		return instance;
	}
	
	public int getNumMenuItems(int i) {
		return itemKeyCodes.get(i).length;
	}
	
	public boolean getItemEnabled(int i, int j) {
		return menus[i].getItem(j).isEnabled();
	}
	
	public void setItemEnabled(int i, int j, boolean enabled) {
		menus[i].getItem(j).setEnabled(enabled);
	}
	
	public int getKeyCode(int i, int j) {
		return itemKeyCodes.get(i)[j];
	}
	
	public int getModifier(int i, int j) {
		return modifiers.get(i)[j];
	}
	
	public Runnable getAction(int i, int j) {
		return actions.get(i)[j];
	}
}
