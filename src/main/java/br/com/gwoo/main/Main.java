package br.com.gwoo.main;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;
import static br.com.gwoo.infra.Constants.*;
import javax.swing.SwingUtilities;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import br.com.gwoo.auction.Auction;
import br.com.gwoo.auctionSniper.AuctionEventListener;
import br.com.gwoo.auctionSniper.AuctionMessageTranslator;
import br.com.gwoo.auctionSniper.AuctionSniper;
import br.com.gwoo.auctionSniper.SniperListener;

public class Main implements SniperListener {
	public static MainWindow ui;

	@SuppressWarnings("unused")	private Chat notToBeGCd;

	public static String MAIN_WINDOW_NAME = "Auction Sniper";
	public static final String SNIPER_STATUS_NAME = "sniper status";
	public static final String BID_COMMAND_FORMAT = "SOLVersion: 1.1; Command: BID; Price: %d;";
	public static final String JOIN_COMMAND_FORMAT = "SOLVersion: 1.1; Command: JOIN;";
	public static final String CLOSE_COMMAND_FORMAT = "SOLVersion: 1.1; Event: CLOSE;";
	public static final String PRICE_COMMAND_FORMAT = "SOLVersion: 1.1; Event: PRICE; " +
													   "Curjava.util.LocalerentPrice: %d; Increment: %d; Bidder: %s;";
	
	
	private static final int ARG_HOSTNAME = 0;
	private static final int ARG_USERNAME = 1;
	private static final int ARG_PASSWORD = 2;
	private static final int ARG_ITEM_ID = 3;

	public static final String ITEM_ID_AS_LOGIN = "auction-%s";
	public static final String AUCTION_RESOURCE = "Auction";

	private static final String AUCTION_ID_FORMAT = ITEM_ID_AS_LOGIN + "@%s/" + AUCTION_RESOURCE;

	
	
	public Main() throws Exception{
		startUserInterface();
	}
	
	public static void main(String... args) throws Exception {
		Main main = new Main();
		main.joinAuction(connection(args[ARG_HOSTNAME],args[ARG_USERNAME],args[ARG_PASSWORD]), args[ARG_ITEM_ID]);
	}

	private void joinAuction(XMPPConnection connection, String itemId)
			throws XMPPException {
	
		disconnectWhenUICloses(connection);
		final Chat chat = connection.getChatManager().createChat(auctionId(itemId, connection), null);
		this.notToBeGCd = chat;
		
		Auction auction = new Auction() {			
			@Override
			public void bid(int amount) {
				try {
					chat.sendMessage(String.format(BID_COMMAND_FORMAT, amount));
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		};
		chat.addMessageListener(new AuctionMessageTranslator(new AuctionSniper(auction, this)));
		chat.sendMessage(JOIN_COMMAND_FORMAT);
	}
	
	private void disconnectWhenUICloses(final XMPPConnection connection) {
		ui.addWindowListener(new WindowAdapter(){			
			@Override public void  windowClosed(WindowEvent e){			
				connection.disconnect();
			}
		});	
	}

	private static String auctionId(String itemId, XMPPConnection connection) {
		return String.format(AUCTION_ID_FORMAT,itemId,connection.getServiceName()); 
	}

	private static XMPPConnection connection(String hostname, String username,
			String password) throws XMPPException {		
		XMPPConnection connection = new XMPPConnection(hostname);
		connection.connect();
		connection.login(username,password, AUCTION_RESOURCE);
		
		return connection;
	}

	private void startUserInterface() throws Exception {
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				ui = new MainWindow();				
			}
		});		
	}

	@Override
	public void sniperLost() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ui.showStatus(MainWindow.STATUS_LOST);
			}
		});	
	}

	@Override
	public void sniperBidding() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ui.showStatus(MainWindow.STATUS_BIDDING);
			}
		});	
	}
}
