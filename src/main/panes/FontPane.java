package main.panes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import main.AppTextArea;
import main.Main;
import main.TextInput;

@SuppressWarnings("serial")
public class FontPane extends Pane {
	
	private final JLabel[] labels = { new JLabel("Font:"), new JLabel("Font style:"), new JLabel("Size:"), new JLabel("Sample Text") };
	private final TextInput[] textFields = new TextInput[3];
	private final JScrollPane[] scrollPanes = new JScrollPane[3];
	private final JPanel[] panels = new JPanel[3];
	
	// note that DefaultListModel is used for managing the underlying data that JList displays; while some operations on elements can be done directly with
	// JList, not all operations can, which is why DefaultListModel is used; also, note that because of Java's type system not being fully compatible with
	// generic arrays, using a raw array of generics results in an unchecked conversion warning
	private final List<DefaultListModel<String>> listModels = new ArrayList<>();
	private final List<JList<String>> lists = new ArrayList<>();
	
	private final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	
	// stores font family as key and its style(s) in a list of Integer; LinkedHashMap and LinkedHashSet both maintain the insertion order of elements, unlike
	// their regular counterparts, HashMap and HashSet, respectively
	private final Map<String, LinkedHashSet<String>> fonts = new LinkedHashMap<>();
	
	private boolean searchingFamily;
	
	public FontPane(JFrame owner) {
		super(owner, "Font", new Dimension(400, 400), new Dimension(90, 28));
		
		final int[] mnemonickeyCodes = { KeyEvent.VK_F, KeyEvent.VK_Y, KeyEvent.VK_S };
		final int n = 5;
		
		for (int i = 0; i < textFields.length; i++) {
			listModels.add(new DefaultListModel<>());
			lists.add(new JList<>(listModels.get(i)));
			lists.get(i).setSelectionMode(ListSelectionModel.SINGLE_SELECTION);  // make list only able have a single selection at a time
			
			scrollPanes[i] = new JScrollPane(lists.get(i));
		}
		textFields[0] = new TextInput() {
			@Override
			public void onEdit(boolean addedText) {
				final String input = getText().toLowerCase();
				final ListModel<String> fonts = lists.get(0).getModel();  // gets the DefaultListModel object assigned to it
				
				searchingFamily = true;
				
				for (int i = 0; i < fonts.getSize(); i++) {
					final String font = fonts.getElementAt(i).toLowerCase();
					
					if (font.startsWith(input)) {
						final Rectangle viewRect = scrollPanes[0].getViewport().getViewRect();
						final int itemHeight = lists.get(0).getCellBounds(0, 1).height;
						final int y = lists.get(0).getCellBounds(i, i).y - viewRect.height / 2 + itemHeight / 2;
						
						scrollPanes[0].getViewport().scrollRectToVisible(new Rectangle(0, y, viewRect.width, viewRect.height));
						lists.get(0).ensureIndexIsVisible(i);  // ensures scrollpane scrolls to show element is visible if it's not already
						
						if (font.equals(input)) lists.get(0).setSelectedIndex(i);
						searchingFamily = false;
						return;
					}
				}
			}
		};
		for (int i = 0; i < textFields.length; i++) {
			if (i > 0) textFields[i] = new TextInput();
			textFields[i].setMaximumSize(new Dimension(220, 20));
			textFields[i].getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_H, Main.SHORTCUT_KEY), "none");
			
			labels[i].setLabelFor(textFields[i]);
			labels[i].setDisplayedMnemonic(mnemonickeyCodes[i]);
		}
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		((JComponent) getContentPane()).setBorder(new EmptyBorder(n, n, n, n));
		
		for (int i = 0; i < panels.length; i++) {
			panels[i] = new JPanel();
			getContentPane().add(panels[i]);
		}
		initLists();
		initSampleTextPanel();
		initButtonsPanel();
		start(owner);
	}
	
	private void initLists() {
		final GroupLayout panelLayout = new GroupLayout(panels[0]);
		
		panelLayout.setAutoCreateGaps(true);
		panelLayout.setAutoCreateContainerGaps(true);
		
		initFamilyList();
		initStyleList();
		initSizeList();
		setSelections();
		
		panels[0].setLayout(panelLayout);
		
		panelLayout.setHorizontalGroup(panelLayout.createSequentialGroup()
			.addGroup(panelLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(labels[0])
				.addComponent(textFields[0])
				.addComponent(scrollPanes[0]))
			.addGroup(panelLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(labels[1])
				.addComponent(textFields[1])
				.addComponent(scrollPanes[1]))
			.addGroup(panelLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(labels[2])
				.addComponent(textFields[2])
				.addComponent(scrollPanes[2]))
			.addContainerGap());
		
		panelLayout.setVerticalGroup(panelLayout.createSequentialGroup()
			.addGroup(panelLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(labels[0])
				.addComponent(labels[1])
				.addComponent(labels[2]))
			.addGroup(panelLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(textFields[0])
				.addComponent(textFields[1])
				.addComponent(textFields[2]))
			.addGroup(panelLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(scrollPanes[0])
				.addComponent(scrollPanes[1])
				.addComponent(scrollPanes[2]))
			.addContainerGap());
	}
	
	private void initFamilyList() {
		// get all fonts installed on the system; note that this method treats each distinct combination of font family and style (plain, bold, italic, etc.)
		// as a different font (ex. "Arial Regular" is one Font and "Arial Bold" is another); all font families are guaranteed to at least have the plain style
		final Font[] systemFonts = ge.getAllFonts();
		final String[] fontFamilies = ge.getAvailableFontFamilyNames();
		
		// add all the keys (font families) and values (font styles) to the main font collection
		for (String family : fontFamilies) {
			final LinkedHashSet<String> styles = new LinkedHashSet<>();
			fonts.put(family, styles);
			
			styles.add("Regular");
			
			// filter fonts to get only those of the current font family, then iterate over each of them to get all the available styles for that family
			Arrays.stream(systemFonts)
				.filter(f -> f.getFontName().contains(family))
				.forEach(f -> {
					String name = f.getFontName().toLowerCase();
					boolean bold = name.contains("bold");
					boolean italic = name.contains("italic");
					
					if (bold && italic) styles.add("Bold Italic");
					else if (bold) styles.add("Bold");
					else if (italic) styles.add("Italic");
				});
		}
		listModels.get(0).addAll(fonts.keySet());
		
		// detect changes in selection for JList
		lists.get(0).addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!closing()) {
					final String family = lists.get(0).getSelectedValue();
					
					if (!searchingFamily) textFields[0].setText(family);
					
					listModels.get(1).clear();
					listModels.get(1).addAll(fonts.get(family));
					textFields[1].setText("Regular");
					lists.get(1).setSelectedIndex(0);
					labels[3].setFont(new Font(textFields[0].getText(), Font.PLAIN, labels[3].getFont().getSize()));
				}
			}
		});
	}
	
	private void initStyleList() {
		lists.get(1).addListSelectionListener(new ListSelectionListener() {
			@Override
			public void  valueChanged(ListSelectionEvent e) {
				if (!closing()) {
					textFields[1].setText(lists.get(1).getSelectedValue());
					labels[3].setFont(labels[3].getFont().deriveFont(getSelectedStyleValue()));
				}
			}
		});
	}
	
	private void initSizeList() {
		final List<String> standardSizes = new ArrayList<>();
		
		// note that string concatenation (ie. "" + i) implicitly uses String.valueOf() to create a StringBuilder (or StringBuffer in older versions of Java)
		// to create and return the String result; using String.valueOf() explicitly is slightly more efficient as there's less intermediate objects and
		// operations involved, though for smaller operations, such as this one, the performance is negligible
		for (int i = 8; i < 12; i++) standardSizes.add(String.valueOf(i));
		for (int i = 12; i <= 28; i += 2) standardSizes.add(String.valueOf(i));
		
		standardSizes.addAll(List.of("36", "48", "72"));
		
		listModels.get(2).addAll(standardSizes);
		lists.get(2).addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!closing()) textFields[2].setText(lists.get(2).getSelectedValue());
			}
		});
	}
	
	private void setSelections() {
		final Font font = AppTextArea.getInstance().getTextFont();
		final String[] styles = { "Regular", "Bold", "Italic", "Bold Italic" };  // ordered to align with the Font style values
		final String[] values = new String[3];
		
		values[0] = font.getFamily();
		values[1] = styles[font.getStyle()];
		values[2] = String.valueOf(AppTextArea.getInstance().getBaseFontSize());  // note that font.getSize() will return the scaled font size with zoom
		
		for (int i = 0; i < values.length; i++) {
			textFields[i].setText(values[i]);
			lists.get(i).setSelectedValue(values[i], true);
		}
	}
	
	private void initSampleTextPanel() {
		labels[3].setFont(new Font(textFields[0].getText(), getSelectedStyleValue(), 26));
		labels[3].setHorizontalAlignment(JLabel.CENTER);
		panels[1].setPreferredSize(new Dimension(356, 88));
		panels[1].setMaximumSize(new Dimension(356, 88));
		panels[1].setLayout(new BorderLayout());
		panels[1].setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		panels[1].add(labels[3], BorderLayout.CENTER);
	}
	
	private void initButtonsPanel() {
		final GridBagConstraints gbc = new GridBagConstraints();
		final int n = 5;
		final AbstractAction setFont = new AbstractAction("Ok") {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!getValidFamily()) return;
				if (!getValidStyle()) return;
				
				int size = getFontSize();
				if (size == -1) return;
				
				AppTextArea.getInstance().setTextFont(textFields[0].getText(), getSelectedStyleValue(), size);
				dispose();
			}
		};
		primaryButton.setAction(setFont);
		panels[2].setLayout(new GridBagLayout());
		gbc.gridx = gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(n, n, n, n);
		panels[2].add(primaryButton, gbc);
		gbc.gridx++;
		panels[2].add(cancelButton, gbc);
	}
	
	private int getSelectedStyleValue() {
		switch (textFields[1].getText()) {
			case "Bold": return Font.BOLD;
			case "Italic": return Font.ITALIC;
			case "Bold Italic": return Font.BOLD + Font.ITALIC;
			default: return Font.PLAIN;
		}
	}
	
	private boolean getValidFamily() {
		final String input = textFields[0].getText().toLowerCase();
		
		// note that the isEmpty() method checks to see if the size of the string is 0, while the isBlank() method checks to see if the string is composed of
		// only whitespace characters
		if (!input.isEmpty() && !input.isBlank()) {
			final ListModel<String> families = lists.get(0).getModel();
			
			for (int i = 0; i < families.getSize(); i++) {
				if (input.equals(families.getElementAt(i).toLowerCase()))
					return true;
			}
		}
		JOptionPane.showOptionDialog(this, "There is no font with that name.\nChoose a font from the list of fonts.", "Invalid Font Name",
				JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, JOptionPane.OK_OPTION);
		return false;
	}
	
	private boolean getValidStyle() {
		final String input = textFields[1].getText().toLowerCase();
		
		if (!input.isEmpty() && !input.isBlank()) {
			final ListModel<String> styles = lists.get(1).getModel();
			
			for (int i = 0; i < styles.getSize(); i++) {
				if (input.equals(styles.getElementAt(i).toLowerCase()))
					return true;
			}
		}
		JOptionPane.showOptionDialog(this, "This font is not available in that style.\nChoose a style from the list of styles.", "Invalid Font Style",
				JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, JOptionPane.OK_OPTION);
		return false;
	}
	
	private int getFontSize() {
		try {
			return Integer.parseInt(textFields[2].getText());
		}
		catch (NumberFormatException e) {
			JOptionPane.showOptionDialog(this, "Size must be a number.", "Invalid Font Size",
					JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, JOptionPane.OK_OPTION);
			return -1;
		}
	}
	
	@Override
	protected void onClose() {
		for (var model : listModels) model.clear();
		fonts.clear();
	}
}
