/**
 * Created by max on 17.11.16.
 */
public class Foo {
    private int bar1 =0;
    public int bar2 =1;
    public static double bar3 =Math.PI;
    public static final int thenumber =42;
    public int foo1(int n){
        return n*thenumber;
    }
    private int foo2(int n){
        int thenumber=43;
        return n*thenumber;
    }
    public Foo(){
        this.bar1=7;
        this.bar2=8;
    }
    public Foo(int a113){
        this();
        this.bar1=a113;
    }
    public Foo(double b77){
        this.bar2=5;
    }
    public int getBar1(){
        return this.bar1;
    }
    public void setBar1(int value){
        this.bar1=value;
        System.out.println("changed bar1 to "+bar1);
    }

    public static void main(String[] args) {
        Foo var1 = new Foo();
    }
}
