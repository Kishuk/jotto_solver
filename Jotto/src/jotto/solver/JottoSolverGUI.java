// Note to self: I hate java UI development
package jotto.solver;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jotto.solver.FixedLenNumberDocument;

public class JottoSolverGUI extends JFrame {
	
	private static final long serialVersionUID = -648693989071663199L;

	// Shared vars among threads
	private static Object guessMonitor = new Object();
	private static JottoSolver js = new JottoSolver(0);
	private static boolean guessComplete = false;
	private static boolean killCalc = false;
	private static int guessId = 0;
	
	// UI elements
	JProgressBar pb = new JProgressBar(0, 100);
	JButton wordLengthButton = new JButton("OK");
	JTextField wordLengthField = new JTextField();
	JButton resetButton = new JButton("Reset");
	JButton guessButton = new JButton("Find best guess!");
	JLabel guessDisplay = new JLabel();
	JButton updateDictButton = new JButton("Update with results!");
	JTextField wordGuessField = new JTextField();
	JTextField matchesField = new JTextField();
	JLabel possibleCountLabel = new JLabel();
	JList possWordsList = new JList();
	JScrollPane possWordsPane = new JScrollPane(possWordsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	
	@SuppressWarnings("unused")
	public static void main(String args[]) {
		// Run the GUI
		JottoSolverGUI gui = new JottoSolverGUI();
	}
	
	public JottoSolverGUI() {
		super("Jotto Guesser v1.0 by Justin E. Churchill");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel left = new JPanel();
		left.setLayout(new BoxLayout(left, BoxLayout.PAGE_AXIS));
		left.add(Box.createVerticalStrut(30));
		left.add(makeWordLengthPanel());
		left.add(Box.createVerticalStrut(15));
		left.add(makeWordGuessPanel());
		left.add(Box.createVerticalStrut(30));
		
		JPanel right = new JPanel();
		right.setLayout(new BoxLayout(right, BoxLayout.PAGE_AXIS));
		right.add(Box.createVerticalStrut(30));
		right.add(new JLabel("Possible Words"));
		right.add(Box.createVerticalStrut(15));
		possWordsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		possWordsList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				wordGuessField.setText((String) possWordsList.getSelectedValue());
			}
		});
		possWordsPane.setPreferredSize(new Dimension(250, 250));
		right.add(possWordsPane);
		right.add(Box.createVerticalStrut(30));
		
		JPanel everything = new JPanel();
		everything.setLayout(new BoxLayout(everything, BoxLayout.LINE_AXIS));
		everything.add(left);
		everything.add(Box.createHorizontalStrut(15));
		everything.add(right);
		everything.add(Box.createHorizontalStrut(30));
		getContentPane().add(everything);
		
		// Add listener for cache-saving when closing
		this.addWindowListener(new WindowListener() {
			@Override
			public void windowActivated(WindowEvent arg0) {}
			@Override
			public void windowClosed(WindowEvent arg0) {}
			@Override
			public void windowClosing(WindowEvent arg0) {
				js.saveCache();
			}
			@Override
			public void windowDeactivated(WindowEvent arg0) {}
			@Override
			public void windowDeiconified(WindowEvent arg0) {}
			@Override
			public void windowIconified(WindowEvent arg0) {}
			@Override
			public void windowOpened(WindowEvent arg0) {}
		});
		
		// Load cache
		js.loadCache();
		
		// Init UI state
		possibleCountLabel.setText("Please choose a word length to begin.");
		enableInteraction(false);
		enableResults(false);
		
		// Make the window appear once all is finished
		setResizable(false);
		pack();
		setVisible(true);
	}
	
	private JPanel makeWordLengthPanel() {
		JPanel result = new JPanel();
		result.setLayout(new BoxLayout(result, BoxLayout.LINE_AXIS));
		result.add(Box.createHorizontalStrut(30));
		
		JLabel lab1 = new JLabel("Word Length:");
		lab1.setFont(lab1.getFont().deriveFont(Font.BOLD, 14));
		result.add(lab1);
		result.add(Box.createHorizontalStrut(5));
		
		result.add(wordLengthField);
		result.add(Box.createHorizontalStrut(30));
		
		result.add(wordLengthButton);
		result.add(Box.createHorizontalStrut(30));
		
		result.add(resetButton);
		result.add(Box.createHorizontalStrut(30));
		
		wordLengthField.setHorizontalAlignment(JTextField.CENTER);
		wordLengthField.setFont(wordLengthField.getFont().deriveFont(Font.BOLD, 20));
		wordLengthField.setDocument(new FixedLenNumberDocument(2));
		wordLengthField.setPreferredSize(new Dimension(40, 30));
		
		wordLengthButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String text = wordLengthField.getText();
				int wlen = 0;
				boolean valid = false;
				try {
					wlen = Integer.parseInt(text);
					valid = wlen > 0;
				}
				catch (NumberFormatException e) {}
				if(!valid) {
					wordLengthField.setSelectionStart(0);
					wordLengthField.setSelectionEnd(text.length());
					wordLengthField.setSelectionColor(Color.RED);
					wordLengthField.requestFocus();
				}
				else {
					wordLengthField.setSelectionColor(new Color(184, 207, 229));
					enableInteraction(true);
					String[] possWords;
					synchronized(guessMonitor) {
						js.reset(wlen);
						possWords = js.getPossibleWords();
					}
					possWordsList.setListData(possWords);
					possibleCountLabel.setText(possWords.length + " possible words remain.");
					wordGuessField.setDocument(new FixedLenDocument(wlen));
				}
			}
		});
		
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				resetButtonHit();
			}
		});
		
		return result;
	}
	
	private JPanel makeWordGuessPanel() {
		JPanel result = new JPanel();
		result.setLayout(new BoxLayout(result, BoxLayout.PAGE_AXIS));
		
		JPanel p0 = new JPanel();
		p0.setLayout(new BoxLayout(p0, BoxLayout.LINE_AXIS));
		p0.add(Box.createHorizontalStrut(30));
		
		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1, BoxLayout.LINE_AXIS));
		p1.add(Box.createHorizontalStrut(30));
		
		JPanel p2 = new JPanel();
		p2.setLayout(new BoxLayout(p2, BoxLayout.LINE_AXIS));
		p2.add(Box.createHorizontalStrut(30));
		
		p0.add(guessButton);
		p0.add(Box.createHorizontalStrut(15));
		
		guessDisplay.setPreferredSize(new Dimension(300, 30));
		guessDisplay.setFont(guessDisplay.getFont().deriveFont(Font.BOLD, 20));
		guessDisplay.setHorizontalAlignment(JLabel.CENTER);
		p0.add(guessDisplay);
		p0.add(Box.createHorizontalGlue());
		
		result.add(p0);
		result.add(Box.createHorizontalStrut(30));
		/////////
		pb.setPreferredSize(new Dimension(240, 40));
		pb.setStringPainted(true);
		p1.add(pb);
		p1.add(Box.createHorizontalStrut(30));
		
		result.add(p1);
		result.add(Box.createVerticalStrut(30));
		/////////
		
		JLabel lab1 = new JLabel("I Guessed:");
		lab1.setFont(lab1.getFont().deriveFont(Font.BOLD, 14));
		p2.add(lab1);
		p2.add(Box.createHorizontalStrut(15));
		
		wordGuessField.setHorizontalAlignment(JTextField.CENTER);
		wordGuessField.setFont(wordGuessField.getFont().deriveFont(Font.BOLD, 20));
		wordGuessField.setPreferredSize(new Dimension(100, 30));
		p2.add(wordGuessField);
		p2.add(Box.createHorizontalStrut(15));
		
		JLabel lab2 = new JLabel("Matches:");
		lab2.setFont(lab2.getFont().deriveFont(Font.BOLD, 14));
		p2.add(lab2);
		p2.add(Box.createHorizontalStrut(15));
		
		matchesField.setHorizontalAlignment(JTextField.CENTER);
		matchesField.setFont(matchesField.getFont().deriveFont(Font.BOLD, 20));
		matchesField.setPreferredSize(new Dimension(40, 30));
		matchesField.setDocument(new FixedLenNumberDocument(2));
		p2.add(matchesField);
		p2.add(Box.createHorizontalStrut(30));
		
		result.add(p2);
		result.add(Box.createVerticalStrut(30));
		
		result.add(updateDictButton);
		result.add(Box.createVerticalStrut(30));
		
		result.add(possibleCountLabel);
		result.add(Box.createVerticalStrut(30));
		
		guessButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				guessWordInBgThread();
			}
		});
		
		updateDictButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String guess = wordGuessField.getText();
				String matchesString = matchesField.getText();
				int matches = 0;
				boolean valid1 = false, valid2 = false;
				int wordLen = 0;
				synchronized(guessMonitor) {
					wordLen = js.wordLength();
				}
				try {
					valid2 = guess.length() == wordLen;
					
					for(int i=0; i<guess.length(); i++) {
						valid2 = valid2 && guess.charAt(i) >= 'A' && guess.charAt(i) <= 'Z';
					}
					
					matches = Integer.parseInt(matchesString);
					valid1 = matches >= 0 && matches <= wordLen;
				}
				catch (NumberFormatException e) {}
				if(!valid2) {
					wordGuessField.setSelectionStart(0);
					wordGuessField.setSelectionEnd(guess.length());
					wordGuessField.setSelectionColor(Color.RED);
					wordGuessField.requestFocus();
				}
				if(!valid1) {
					matchesField.setSelectionStart(0);
					matchesField.setSelectionEnd(matchesString.length());
					matchesField.setSelectionColor(Color.RED);
					matchesField.requestFocus();
				}
				if(valid1 && valid2) {
					wordGuessField.setSelectionColor(new Color(184, 207, 229));
					matchesField.setSelectionColor(new Color(184, 207, 229));
					resetButton.setEnabled(false);
					guessButton.setEnabled(false);
					enableResults(false);
					String[] possWords;
					synchronized(guessMonitor) {
						js.tellResults(guess, matches);
						possWords = js.getPossibleWords();
					}
					possibleCountLabel.setText(possWords.length + " possible words remain.");
					possWordsList.setListData(possWords);
					enableResults(true);
					resetButton.setEnabled(true);
					guessButton.setEnabled(true);
				}
			}
		});
		
		return result;
	}
	
	private void resetButtonHit() {
		// Stop current calculation.
		killCurrentlyRunningGuess();
		// Clear all result areas
		possWordsList.setListData(new String[] {});
		wordGuessField.setText("");
		matchesField.setText("");
		guessDisplay.setText("");
		pb.setString("Ready.");
		
		possibleCountLabel.setText("Please choose a word length to begin.");
		// Enable word length area
		// Disable all other areas.
		enableInteraction(false);
		enableResults(false);
	}
	
	private void enableInteraction(boolean enabled) {
		// Word length area
		wordLengthButton.setEnabled(!enabled);
		wordLengthField.setEnabled(!enabled);
		resetButton.setEnabled(enabled);
		
		guessButton.setEnabled(enabled);
		
		enableResults(enabled);
	}
	
	private void enableResults(boolean enabled) {
		updateDictButton.setEnabled(enabled);
		wordGuessField.setEnabled(enabled);
		matchesField.setEnabled(enabled);
	}
	
	private void guessWordInBgThread() {
		(new Thread() {
			public void run() {
				int myGuessId;
				killCalc = false;
				guessButton.setEnabled(false);
				enableResults(false);
				synchronized(guessMonitor) {
					guessComplete = false;
					loadingBarInBgThread();
					myGuessId = ++guessId;
				}
				String result = js.guessWord();
				synchronized(guessMonitor) {
					// check if this was the last guess request sent out
					if(myGuessId == guessId && !killCalc) {
						guessDisplay.setText(result);
						wordGuessField.setText(result);
						guessComplete = true;
						guessButton.setEnabled(true);
						enableResults(true);
					}
				}
			}
		}).start();
	}
	
	private void loadingBarInBgThread() {
		(new Thread() {
			public void run() {
				boolean done = false;
				long starttime = System.currentTimeMillis();
				while(!done) {
					synchronized(guessMonitor) {
						long now = System.currentTimeMillis();
						int p = js.getProgress();
						int t = js.getTotalWords();
						double msLeft = (double)(now-starttime)*(t-p)/(p+1);
						pb.setValue((int)((double)p/t*100));
						pb.setString(p + "/" + t + " guesses evaluated; " + (int)msLeft/1000 + " secs remaining");
						done = guessComplete;
					}
					Thread.yield();
				}
			}
		}).start();
	}
	
	private void killCurrentlyRunningGuess() {
		synchronized(guessMonitor) {
			if(!guessComplete) {
				killCalc = true;
				js.killCurrentGuessCalc();
			}
		}
	}
}
