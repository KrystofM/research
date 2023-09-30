package pentago.networking.mappers;

import pentago.networking.dto.MoveDto;
import pentago.game.models.Direction;
import pentago.game.models.Move;
import pentago.game.models.Quadrant;
import pentago.game.models.Rotation;
import pentago.networking.dto.MoveDtoException;

/**
 * Class used to transfer between objects.
 * Specifically between the Data Transfer Objects (DTO)
 * Move and classic game model Move. Has two functions
 * in order to transfer both ways.
 */
public class MoveMapper {
    /**
     * Transfers DTO -> Model.
     *
     * @param moveDto move data transfer object
     * @return move normal game model
     */
    public static Move toMove(MoveDto moveDto) {
        Rotation rotation = null;

        switch (moveDto.getB()) {
            case 0:
                rotation = new Rotation(Quadrant.TOP_LEFT, Direction.COUNTER_CLOCKWISE);
                break;
            case 1:
                rotation = new Rotation(Quadrant.TOP_LEFT, Direction.CLOCKWISE);
                break;
            case 2:
                rotation = new Rotation(Quadrant.TOP_RIGHT, Direction.COUNTER_CLOCKWISE);
                break;
            case 3:
                rotation = new Rotation(Quadrant.TOP_RIGHT, Direction.CLOCKWISE);
                break;
            case 4:
                rotation = new Rotation(Quadrant.BOTTOM_LEFT, Direction.COUNTER_CLOCKWISE);
                break;
            case 5:
                rotation = new Rotation(Quadrant.BOTTOM_LEFT, Direction.CLOCKWISE);
                break;
            case 6:
                rotation = new Rotation(Quadrant.BOTTOM_RIGHT, Direction.COUNTER_CLOCKWISE);
                break;
            case 7:
                rotation = new Rotation(Quadrant.BOTTOM_RIGHT, Direction.CLOCKWISE);
                break;
        }

        return new Move(moveDto.getA(), rotation);
    }

    /**
     * Transfers Model -> DTO.
     *
     * @param move normal game model
     * @return moveDto move data transfer object
     * @throws MoveDtoException when transfer move is incorrect
     */
    public static MoveDto toMoveDto(Move move) throws MoveDtoException {
        Quadrant quadrant = move.getRotation().getQuadrant();
        Direction direction = move.getRotation().getDirection();

        int b = -1;
        switch (quadrant) {
            case TOP_LEFT:
                b = direction.equals(Direction.COUNTER_CLOCKWISE) ? 0 : 1;
                break;
            case TOP_RIGHT:
                b = direction.equals(Direction.COUNTER_CLOCKWISE) ? 2 : 3;
                break;
            case BOTTOM_LEFT:
                b = direction.equals(Direction.COUNTER_CLOCKWISE) ? 4 : 5;
                break;
            case BOTTOM_RIGHT:
                b = direction.equals(Direction.COUNTER_CLOCKWISE) ? 6 : 7;
                break;
        }

        return new MoveDto(move.getField(), b);
    }
}
