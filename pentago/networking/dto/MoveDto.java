package pentago.networking.dto;

/**
 * Move Data Transfer Object serves the function
 * of modelling the data of a move according the
 * networking protocol. It is used extensively
 * while creating and getting moves from the server.
 */
public class MoveDto {
    private final int a;
    private final int b;

    /**
     * Constructor with integers.
     *
     * @param a field index
     * @param b rotation index
     * @throws MoveDtoException Custom exception
     * when parameters are not valid
     */
    public MoveDto(int a, int b) throws MoveDtoException {
        this.a = a;
        this.b = b;
        if (!isValid()) {
            throw new MoveDtoException();
        }
    }

    /**
     * Constructor with strings.
     *
     * @param a field index
     * @param b rotation index
     * @throws MoveDtoException Custom exception
     * when parameters are not valid
     */
    public MoveDto(String a, String b) throws MoveDtoException {
        this(Integer.parseInt(a), Integer.parseInt(b));
    }

    public int getA() {
        return a;
    }

    public int getB() {
        return b;
    }

    /**
     * Function to return whether current values set
     * are indeed correct according to protocol.
     *
     * @return correctness of given values
     */
    private boolean isValid() {
        return !(a < 0 || a > 36 || b < 0 || b > 7);
    }
}

