import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    private static final int BOARD_WIDTH = 360;
    private static final int BOARD_HEIGHT = 640;


    // Images
    private final Image backgroundImg;
    private final Image birdImg;
    private final Image topPipeImg;
    private final Image bottomPipeImg;

    // Bird
    private final int birdX = BOARD_WIDTH/8;
    private final int birdY = BOARD_HEIGHT/2;
    private final int birdWidth = 34;
    private final int birdHeight = 24;

    // Pipes 
    private final int pipeX = BOARD_WIDTH;
    private final int pipeY = 0;
    private final int pipeWidth = 64; // scaled by 1/6
    private final int pipeHeight = 512;

    // game logic
    private final Bird bird;
    private final int velocityX = -4; // move pipes to the left speed (simulates bird moving right)
    private int velocityY = 0; // move bird up/down speed
    private final int gravity = 1;
     
    private final ArrayList<Pipe> pipes;
    private final Random random = new Random();
    
    private final Timer gameLoop;
    private final Timer placePipesTimer;
    private Boolean gameOver = false;
    private boolean gameStarted = false;
    private double score = 0; 
    

    FlappyBird() {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));

        setFocusable(true);
        addKeyListener(this);

        // load images
        // getClass() refer to Class FlappyBird
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        // bird
        bird = new Bird(birdX, birdY, birdWidth, birdHeight, birdImg);

        // pipes
        pipes = new ArrayList<>();

        // place pipes timer 
        placePipesTimer = new Timer(1500, (ActionEvent e) -> {
            // Code to be executed
            if (gameStarted && !gameOver) {
                placePipes();
            }
        });
        placePipesTimer.start();

        // game timer 60fps
        gameLoop = new Timer(1000/60, this);
        gameLoop.start();

    }

    public void placePipes() {
        // (0-1) * pipeHeight/2.
        // 0 -> -128 (pipeHeight/4)
        // 1 -> -128 - 256 (pipeHeight/4 - pipeHeight/2) = -3/4 pipeHeight
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = BOARD_HEIGHT / 4;

        Pipe topPipe = new Pipe(pipeX, randomPipeY, pipeWidth, pipeHeight, topPipeImg);
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(pipeX, topPipe.getY() + pipeHeight + openingSpace, pipeWidth, pipeHeight, bottomPipeImg);
        pipes.add(bottomPipe);

    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // background
        g.drawImage(backgroundImg, 0, 0, BOARD_WIDTH, BOARD_HEIGHT, null);

        // bird
        g.drawImage(bird.getImg(), bird.getX(), bird.getY(), bird.getWidth(), bird.getHeight(), null);

        // pipes
        for (Pipe pipe : pipes) {
            g.drawImage(pipe.getImg(), pipe.getX(), pipe.getY(), pipe.getWidth(), pipe.getHeight(), null);
        }

        // score
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));

        if (!gameStarted) {
            drawCenteredString(g, "Press SPACE to Start", BOARD_HEIGHT / 2);
        } else if (gameOver) {
            drawCenteredString(g, "Game Over: " + (int) score, BOARD_HEIGHT / 2 - 20);
            drawCenteredString(g, "Press SPACE to Restart", BOARD_HEIGHT / 2 + 20);
        } else {
            // Display score
            g.drawString(String.valueOf((int) score), 10, 35);
        }

        
    }

    private void drawCenteredString(Graphics g, String text, int yPosition) {
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int x = (BOARD_WIDTH - metrics.stringWidth(text)) / 2;
        g.drawString(text, x, yPosition);
    }

    public void move() {
        if (!gameStarted || gameOver) return; // Do not move anything if the game hasn't started

        applyGravity();
        movePipes();
        checkCollisions();
        checkGameOver();
    }

    private void applyGravity() {
        if (!gameStarted) return; 

        velocityY += gravity;
        bird.setY(bird.getY() + velocityY);
        bird.setY(Math.max(bird.getY(), 0)); // Prevent bird from moving past the top of the screen
    }

    private void movePipes() {
        for (int i = pipes.size() - 1; i >= 0; i--) {
            Pipe pipe = pipes.get(i);
            pipe.setX(pipe.getX() + velocityX);
    
            // delete the pipe out of the display
            if (pipe.getX() + pipe.getWidth() < 0) {
                pipes.remove(i);
            }
            // if bird passed the right side of the pipe
            if (!pipe.isPassed() && bird.getX() > pipe.getX() + pipe.getWidth()) {
                pipe.setPassed(true);
                score += 0.5; // 0.5 because there are 2 pipes! so 0.5*2 = 1, 1 for each set of pipes
            }
        }
    }

    private void checkCollisions() {
        for (Pipe pipe : pipes) {
            if (collision(bird, pipe)) {
                gameOver = true;
                return; // No need to check further if a collision is detected
            }
        }
    }

    private void checkGameOver() {
        if (bird.getY() > BOARD_HEIGHT) {
            gameOver = true;
        }
    }

    public boolean collision(Bird a, Pipe b) {
        return a.getX() < b.getX() + b.getWidth() &&   //a's top left corner doesn't reach b's top right corner
               a.getX() + a.getWidth() > b.getX() &&   //a's top right corner passes b's top left corner
               a.getY() < b.getY() + b.getHeight() &&  //a's top left corner doesn't reach b's bottom left corner
               a.getY() + a.getHeight() > b.getY();    //a's bottom left corner passes b's top left corner
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        // call paintComponenent
        move(); 

        // request Swing (JPanel) to redraw the game panel with the updated positions
        // and the current state of the game objects
        repaint();

        if (gameOver) {
            placePipesTimer.stop();
            gameLoop.stop(); 
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!gameStarted) {
                gameStarted = true; // Start the game when SPACE is pressed for the first time
                velocityY = -9; // Make the bird jump immediately on start
                placePipesTimer.start();
                gameLoop.start();
            } else if (gameOver) {
                resetGame(); 
            } else {
                velocityY = -9; // Make the bird jump
            }
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
    }


    @Override
    public void keyReleased(KeyEvent e) {
    }
  

    private void resetGame() {
        // restart game by resetting conditions
        bird.setY(birdY);
        velocityY = 0;
        pipes.clear();
        gameOver = false;
        score = 0;
        gameStarted = false; // Reset the gameStarted flag
        placePipesTimer.restart();
        gameLoop.restart();
    }
}
 