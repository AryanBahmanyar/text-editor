package main;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.text.BadLocationException;

@SuppressWarnings("serial")
public final class StatusBar extends JPanel {
	
	private static StatusBar instance;
	private JLabel[] labels = new JLabel[7];
	
	private StatusBar() {
		super(new GridBagLayout());
		
		final GridBagConstraints gbc = new GridBagConstraints();
		final int halfCount = labels.length / 2;
		final int px = 5;
		
		for (int i = 0; i < labels.length; i++) {
			labels[i] = new JLabel();
			labels[i].setFont(new Font(labels[i].getFont().getFamily(), Font.PLAIN, 12));
		}
		gbc.insets = new Insets(px, px, px, px);
		gbc.gridx = gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;  // set the starting direction
		
		// add the first set of labels to the left
		for (int i = 0; i < halfCount; i++) {
			add(labels[i], gbc);
			gbc.gridx++;
		}
		gbc.gridx = halfCount;  // move to the middle column
		
		// note that the fill variable specifies resize behaviour (how component should be resized), while the weight variables specify how much space
		// a component should take (in this case, horizontally) in relation to its adjacent components (a weight of 0 means none and a wight of 1 means 
		// as much as possible)
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;  // allows component to be resized horizontally
		add(new JLabel(), gbc);                    // add an empty label to fill the space
		gbc.gridx++;                               // move to the column after the empty space (label)
		gbc.weightx = 0.0;                         // reset weightx, specifying that component should not be resized
		gbc.fill = GridBagConstraints.NONE;        // doesn't allow component to be resized
		
		// add the second set of labels to the right
		for (int i = halfCount; i < labels.length; i++) {
			if (i == labels.length - 1) {
				JSeparator separator = new JSeparator(JSeparator.VERTICAL);
				gbc.fill = GridBagConstraints.VERTICAL;
				add(separator, gbc);
				gbc.gridx++;
				gbc.fill = GridBagConstraints.NONE;
			}
			add(labels[i], gbc);
			gbc.gridx++;
		}
		setVisible(true);
		update();
	}
	
	public static StatusBar getInstance() {
		if (instance == null) instance = new StatusBar();
		return instance;
	}
	
	public void update() {
		updateTextInfo();
		updateCaretInfo();
		updateZoomInfo();
	}
	
	public void updateTextInfo() {
		if (isVisible()) {
			final String text = AppTextArea.getInstance().getText();
			final int length = text.length();
			
			// split text content into array of strings with whitespace characters as delimiters (using regex), then get the array length; note that
			// using split on an empty string returns an array with that empty string as the only element, rather than returning an empty array
			int wordCount = (length == 0 ? 0 : text.split("\\s+").length);
			
			labels[0].setText("Length: " + length);
			labels[1].setText("Lines: " + AppTextArea.getInstance().getLineCount());
			labels[2].setText("Words: " + wordCount);
		}
	}
	
	public void updateCaretInfo() {
		if (isVisible()) {
			try {
				int pos = AppTextArea.getInstance().getCaretPosition();
				int line = AppTextArea.getInstance().getLineOfOffset(pos);
				int col = pos - AppTextArea.getInstance().getLineStartOffset(line);
				
				labels[3].setText("Ln: " + (line + 1));
				labels[4].setText("Col: " + (col + 1));
				labels[5].setText("Pos: " + (pos + 1));
			}
			catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void updateZoomInfo() {
		if (isVisible()) labels[6].setText(AppTextArea.getInstance().getZoom() + "%");
	}
}
