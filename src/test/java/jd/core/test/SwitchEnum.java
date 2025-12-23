package jd.core.test;

public class SwitchEnum {

    public enum ColourEnum {
        BLUE, GREEN, RED // TODO FIXME does not work when enum constants are unordered
    }

    static class ColourObject {
        public ColourEnum getType() {
            return ColourEnum.BLUE;
        }
    }
    public static void main(String[] args) {
        ColourObject colourObject = new ColourObject();
        print(colourObject);
    }

    private static void print(ColourObject colourObject) {
        switch (colourObject.getType()) {
            case GREEN:
                System.out.println("Green");
                break;
            case BLUE:
                System.out.println("Blue");
                break;
            case RED:
                System.out.println("Red");
                break;
            default:
                System.out.println("Default (colour)");
                break;
        }
    }
}