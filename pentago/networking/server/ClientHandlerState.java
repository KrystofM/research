package pentago.networking.server;

/**
 * Different states of the client used
 * extensively to properly judge the
 * correct response to situation.
 */
public enum ClientHandlerState {
    INITIAL,
    WAITING_LOGIN,
    LOGGED_IN,
    IN_GAME_WAITING,
    IN_GAME_PLAYING,
}
