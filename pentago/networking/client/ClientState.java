package pentago.networking.client;

/**
 * Client state enum used in Client class
 * to ensure the correctness of functioning.
 */
public enum ClientState {
    INITIAL,
    HELLO_AWAITING,
    LOGIN_AWAITING,
    DECISION_STAGE,
    NEWGAME_AWAITING,
    MOVE_AWAITING
}
