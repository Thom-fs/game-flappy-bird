import javax.swing.JFrame;

public class App {

    public static void main(String[] args) {
        JFrame frame = createGameWindow();
        startGame(frame);
    }

    private static JFrame createGameWindow() {
        JFrame frame = new JFrame("Flappy Bird");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        return frame;
    }

    private static void startGame(JFrame frame) {
        FlappyBird flappyBird = new FlappyBird();
        frame.add(flappyBird);
        frame.pack(); // Adjust frame size to fit its contents neatly
        flappyBird.requestFocusInWindow();
        frame.setVisible(true);
    }
}
