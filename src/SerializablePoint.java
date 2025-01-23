package src;

import java.awt.Point;
import java.io.Serializable;

public class SerializablePoint extends Point implements Serializable {
    public SerializablePoint(int x, int y) {
        super(x, y);
    }
}