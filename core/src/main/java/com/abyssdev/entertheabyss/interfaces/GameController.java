package com.abyssdev.entertheabyss.interfaces;

public interface GameController {
    void startGame();
    void move(int numPlayer, float x, float y);
    void attack(int numPlayer);
    void enemyKilled(int numPlayer, int enemyId);
    void bossKilled(int numPlayer);
    void changeRoom(int numPlayer, String roomId);
    void timeOut();
}
