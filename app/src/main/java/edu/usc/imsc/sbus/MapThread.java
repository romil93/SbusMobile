package edu.usc.imsc.sbus;

/**
 * Created by danielCantwell on 4/15/15.
 */
public class MapThread extends Thread {

    private static final int SECONDS_PER_FRAME = 1;
    private MainActivity mainActivity;
    private boolean stop;

    public MapThread(MainActivity a) {
        mainActivity = a;
        stop = false;
    }

    @Override
    public void run() {

        while (!stop) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.displayMapVehicles();
                }
            });

            try {
                sleep(SECONDS_PER_FRAME * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopThread() {
        stop = true;
    }
}
