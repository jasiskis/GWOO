package br.com.gwoo.infra;

import br.com.gwoo.main.Main;

import com.objogate.wl.swing.AWTEventQueueProber;
import com.objogate.wl.swing.driver.JFrameDriver;
import com.objogate.wl.swing.driver.JLabelDriver;
import com.objogate.wl.swing.gesture.GesturePerformer;
import static org.hamcrest.CoreMatchers.equalTo;;


public class AuctionSniperDriver extends JFrameDriver{

	@SuppressWarnings("unchecked")
	public AuctionSniperDriver(int timeoutMillis) {
		super(new GesturePerformer(),
				JFrameDriver.topLevelFrame(
						named(Main.MAIN_WINDOW_NAME),
						showingOnScreen()),
						new AWTEventQueueProber(timeoutMillis, 100));
	}
	
	@SuppressWarnings("unchecked")
	public void showsSniperStatus(String statusText){
		new JLabelDriver(
			this, named(Main.SNIPER_STATUS_NAME)).hasText(equalTo(statusText));
	}


}
