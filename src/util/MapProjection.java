package util;

public final class MapProjection {
    public static final double X_MIN = -180, X_MAX = 180, Y_MIN = -90, Y_MAX = 90;
    private final int width, height, padding;

    public MapProjection(int width, int height, int padding) {
        this.width = width; this.height = height; this.padding = padding;
    }

    public int screenX(double worldX) {
        double t = (worldX - X_MIN) / (X_MAX - X_MIN);
        return (int) Math.round(padding + t * (width - 2 * padding));
    }

    public int screenY(double worldY) {
        double t = (Y_MAX - worldY) / (Y_MAX - Y_MIN);   // obrnuta y osa
        return (int) Math.round(padding + t * (height - 2 * padding));
    }
}