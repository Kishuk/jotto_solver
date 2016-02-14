package jotto.solver;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class FixedLenDocument extends PlainDocument {

	private static final long serialVersionUID = 1L;
	private int max;
	
	public FixedLenDocument(int max) {
		this.max = max;
	}
	
	@Override
	public void insertString(int offs, String str, AttributeSet a)
		throws BadLocationException {
		if(getLength() + str.length() > max) {
			str = str.substring(0,max-getLength());
		}
		super.insertString(offs, str.toUpperCase(), a);
	}
}
