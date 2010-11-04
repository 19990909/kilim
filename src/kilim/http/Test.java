package kilim.http;

public class Test {
    public static void main(String[] args) {

        int count = 200000;
        long start1 = System.nanoTime();
        for (int i = 0; i < count; i++) {
            Math.sin(i);
        }
        System.out.println("sin:" + (System.nanoTime() - start1));
        long start2 = System.nanoTime();

        for (int i = 0; i < count; i++) {
            Math.cos(i);
        }

        System.out.println("cos:" + (System.nanoTime() - start2));

    }
}
