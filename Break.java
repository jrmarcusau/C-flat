/** Represents a break */
public class Break extends RuntimeException {
    public int nascar;

    /**
     * Constructor.
     * 
     * @param nascar how many loops to break out of
     */
    public Break(int nascar) {
        super("too many break statements");
        this.nascar = nascar;
    }

    /**
     * Decrements the break number and returns it
     * 
     * @return the number of loops to break out of
     */
    public int decrement() {
        return --nascar;
    }
}
