package util;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.util.Timer;
import java.util.TimerTask;

public class InactivityTimer {

	public interface Listener {
		void onCountdown(int secondsLeft);
		void onExpire();
		void onContinue();
	}
	
	private int timeoutSeconds;
	private int warningSeconds;
	private Listener listener;
	
	private Timer timer;
	private AWTEventListener awtListener;
	private int remaining;
	private boolean warning;
	private boolean paused;				
	
	public InactivityTimer(int timeoutSeconds, int warningSeconds, Listener listener) {
        this.timeoutSeconds = timeoutSeconds;
        this.warningSeconds = warningSeconds;
        this.listener = listener;
    }

	public void start() {
        remaining = timeoutSeconds;
        warning = false;

        // 1) Osluskuj SVE dogadjaje misa i tastature u celoj aplikaciji.
        awtListener = new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent event) {
                notifyActivity();
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(awtListener,
                AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);

        // 2) Sat: jedan otkucaj svake sekunde (na GUI niti).
        timer = new Timer("inactivity-timer", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                EventQueue.invokeLater(() -> tick());
            }
        }, 1000, 1000);
    }
	
	public void stop() {
        if (timer != null) timer.cancel();
        if (awtListener != null) Toolkit.getDefaultToolkit().removeAWTEventListener(awtListener);
    }

    public void notifyActivity() {
        if (!paused && !warning) {  
            remaining = timeoutSeconds;
        }
    }

    public void continueWorking() {
        warning = false;
        remaining = timeoutSeconds;
        listener.onContinue();
    }

    private void tick() {
    	if (paused) return;          
        remaining--;
        if (remaining <= 0) {
            stop();
            listener.onExpire();
            return;
        }
        if (remaining <= warningSeconds) {
            warning = true;
            listener.onCountdown(remaining);
        }
    }
    
  
    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
        warning = false;
        remaining = timeoutSeconds;
    }

}
