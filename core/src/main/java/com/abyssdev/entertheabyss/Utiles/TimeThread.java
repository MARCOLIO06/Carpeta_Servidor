package com.abyssdev.entertheabyss.Utiles;

import com.abyssdev.entertheabyss.interfaces.GameController;

public class TimeThread extends Thread {

    private GameController gameController;

    public TimeThread(GameController gameController) {
        this.gameController = gameController;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(2000);
            gameController.timeOut();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
