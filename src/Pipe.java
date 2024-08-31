import java.awt.Image;

public class Pipe {
    int x;
    int y;
    int width;
    int height;
    Image img;
    boolean passed = false;

    public Pipe(int startX, int startY, int width, int height, Image img) {
        this.x = startX;
        this.y = startY;
        this.width = width;
        this.height = height;
        this.img = img;
    }
}
