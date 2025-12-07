package de.zfzfg.triggervolumes.models;

/**
 * Represents the type of action that can be triggered.
 * 
 * @author zfzfg
 */
public enum ActionType {
    /**
     * Player executes a command as themselves.
     */
    PLAYER_COMMAND,
    
    /**
     * Console executes a command.
     */
    CONSOLE_COMMAND,
    
    /**
     * Sends a message to the player.
     */
    MESSAGE,
    
    /**
     * Teleports the player to specified coordinates.
     */
    TELEPORT
}
