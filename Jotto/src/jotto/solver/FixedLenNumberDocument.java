package jotto.solver;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class FixedLenNumberDocument extends PlainDocument {

	private static final long serialVersionUID = 1L;
	private int max;
	
	public FixedLenNumberDocument(int max) {
		this.max = max;
	}
	
	@Override
	public void insertString(int offs, String str, AttributeSet a)
		throws BadLocationException {
		if(getLength() + str.length() > max) {
			str = str.substring(0,max-getLength());
		}
		String numbersOnly = "";
		for(int i=0; i<str.length(); i++) {
			if(Character.isDigit(str.charAt(i))) { numbersOnly += str.charAt(i); }
		}
		super.insertString(offs, numbersOnly, a);
	}
}
