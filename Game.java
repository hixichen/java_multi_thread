import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * whack hole game for java multi thread programing. 
 * @author Chen Xi
 */
public class Game {
    /**
     * Reference to the start button.
     */
    private JButton startButton;

    /**
     * Reference to the time left text field.
     */
    private static JTextField timeLeftTextField;

    /**
     * Reference to the score text field.
     */
    private static JTextField scoreTextField;

    /**
     * Reference to the buttons.
     */
    private JButton[] buttons;

    /**
     * Reference to the start or not. ! shared by all threads.
     */
    private AtomicInteger isGameStart;

    /**
     * Reference to the total scores. !shared by all threads.
     */
    private AtomicInteger totalScores;

    /**
     * Reference to the row.
     */
    private int row;
    /**
     * Reference to the columns.
     */
    private int columns;

    /**
     * Constructor where JFrame and other components are instantiated.
     * @param row numbers of row
     * @param columns column number
     */
    public Game(int row, int columns) {

        JFrame frame = new JFrame("Whack A Mole Game designed by Chen Xi @02-18-2016");
        frame.setSize(800, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Font font = new Font(Font.MONOSPACED, Font.BOLD, 14);

        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

        /* #######################################pane1 */
        JPanel pane1 = new JPanel();
        startButton = new JButton("Start");
        pane1.add(startButton);

        JLabel timeLeftLable = new JLabel("Time Left:");
        pane1.add(timeLeftLable);

        timeLeftTextField = new JTextField(5);
        pane1.add(timeLeftTextField);

        JLabel scoreLabel = new JLabel("Scores:");
        pane1.add(scoreLabel);

        scoreTextField = new JTextField(5);
        pane1.add(scoreTextField);

        pane.add(pane1);

        /* #######################################pane2 */
        JPanel pane2 = new JPanel();
        pane2.setLayout(new GridLayout(row, columns));

        buttons = new JButton[row * columns];
        this.row = row;
        this.columns = columns;

        ActionListener buttonListener = new ButtonActionListener();

        int i = 0;
        while (i < (row * columns)) {
            buttons[i] = new JButton(" ");
            buttons[i].setBackground(Color.LIGHT_GRAY);
            buttons[i].setFont(font);
            buttons[i].setOpaque(true);
            pane2.add(buttons[i]);
            buttons[i].addActionListener(buttonListener);

            i++;
        }

        pane.add(pane2);

        /* #######################################pane3 */
        JPanel pane3 = new JPanel();
        JLabel funLabel = new JLabel(" Have fun~");
        pane3.add(funLabel);
        pane.add(pane3);

        frame.setContentPane(pane);
        frame.setVisible(true);

        /* ############ action listener for start button */
        ActionListener listener = new MyActionListener();
        startButton.addActionListener(listener);

        // initial the atomic variable.
        isGameStart = new AtomicInteger(0);
        totalScores = new AtomicInteger(0);
    }

    /**
     * Main method that instantiates GUI Object.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        new Game(5, 8);
    }

    /**
     * Timer Thread that show the left time value to user.
     */
    private class TimerThread extends Thread {
        /**
         * Reference to the current seconds.
         */
        private int currentSeconds = 20;

        /**
         * Constructor for TimerThread.
         */
        public TimerThread() {
        }

        @Override
        public void run() {

            scoreTextField.setText(String.valueOf(totalScores.get()));
            while (isGameStart.compareAndSet(1, 1)) {

                timeLeftTextField.setText(String.valueOf(currentSeconds));

                if (0 == currentSeconds) {
                    isGameStart.set(0);
                }

                if (currentSeconds > 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new AssertionError(e);
                    }
                }

                currentSeconds--;
            }
        }
    }

    /**
     * Show Thread that for every button.
     */

    private class ShowThread extends Thread {
        /**
         * Reference to the myButton.
         */
        private JButton myButton;
        /**
         * Reference to color option.
         */
        private Color myColor;
        /**
         * Reference to the string text.
         */
        private String myText;
        /**
         * Reference to random number.
         */
        private Random random = new Random();

        /**
         * Constructor.
         * @param button array of buttons.
         * @param color color.
         * @param text text.
         */
        public ShowThread(JButton button, Color color, String text) {
            myButton = button;
            myColor = color;
            myText = text;

        }

        /*
         * Implement run method of Thread class.
         */
        @Override
        public void run() {
            try {
                int max = 4000;
                int min = 500;
                int mid = 1000;

                while (isGameStart.compareAndSet(1, 1)) {
                    /* sleep time: 500 ~4000 ms */
                    int randomWaitNum = random.nextInt(max) % (max - min + 1) + min;
                    Thread.sleep(randomWaitNum);
                    if (1 == isGameStart.get()) {
                        synchronized (this) {
                            myButton.setText(":-(");
                            myButton.setBackground(Color.RED);
                        }
                    }

                    /* show time: 500 ~1000 ms */
                    int randomUpNum = random.nextInt(mid) % (mid - min + 1) + min;
                    Thread.sleep(randomUpNum);
                    if (1 == isGameStart.get()) {
                        synchronized (this) {
                            myButton.setText(myText);
                            myButton.setBackground(myColor);
                        }
                    }

                }
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            } finally {
                myButton.setText(myText);
                myButton.setBackground(myColor);
            }
        }
    }

    /**
     * Implementation of actions.
     * @param event ActionEvent object
     */
    private class ButtonActionListener implements ActionListener {
        /**
         * actionPerformed() method to Swing.
         * @param event for action perform
         */
        public void actionPerformed(ActionEvent event) {

            // Check button status.
            JButton button = (JButton) event.getSource();

            synchronized (this) {
                if (isGameStart.compareAndSet(1, 1)) {
                    if (button.getBackground() == Color.RED && button.getText() == ":-(") {
                        scoreTextField.setText(String.valueOf(totalScores.getAndAdd(1)));
                    }
                }
            }
        }

    }

    /**
     * Private nested class used to provide actionPerformed() method to Swing.
     * @author Chen Xi
     */
    private class MyActionListener implements ActionListener {
        /**
         * actionPerformed() method to Swing.
         * @param event for action perform
         */
        public void actionPerformed(ActionEvent event) {

            if (event.getSource() == startButton && isGameStart.compareAndSet(0, 1)) {

                int i = 0;
                while (i < (row * columns)) {
                    Thread show = new ShowThread(buttons[i], Color.LIGHT_GRAY, ":-)");
                    show.start();
                    i++;
                }

                Thread myTimer = new TimerThread();
                myTimer.start();
            }

        }
    }

}

