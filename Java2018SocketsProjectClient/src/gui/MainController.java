package gui;

import java.io.File;
import java.io.IOException;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import monitor.Monitor;
import monitor.ObserverList;
import socketConnection.SocketConnection;
import upload.FileChooserUpload;

/**
 * Main method class for javafx
 * @author valdar
 *
 */
public class MainController {

//------------------------------------//
	@FXML
	MediaView mediaView;

	@FXML
	ListView<String> serverListView;
	
	@FXML
	ListView<String> localListView;
	
	@FXML
	Button downloadButton;
	
	@FXML
	Button uploadButton;
	
	@FXML
	Button playButton;
	
	@FXML
	Button stopButton;
	
	@FXML
	Text currentFileLoaded;
//------------------------------------//

	//The Path for the directories.
	private final String LOCAL_PATH = "sockets\\loca";
	
	private SocketConnection sc;
	
	//Local and sever monitor
	private Monitor localMonitor;
	
	//check if file loaded and check it file playing
	private boolean loaded = false;
	private boolean playing = false;
	
	@SuppressWarnings("unused")
	private Stage stage;
	
	/**
	 * Start program
	 */
	@FXML
	public void initialize() {
		
		createfolder();
		
		addObservers();
		
		sc = new SocketConnection(this);
		
	}
	
	/**
	 * Set stage and add closing rules
	 * @param stage
	 */
	public void setStage(Stage stage) {
			
		this.stage = stage;
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			
			@Override
		    public void handle(WindowEvent t) {
				
				closeProgram();
			}
		});
	}
		
	/*
	* Run below code before program exits
	*/
	public void closeProgram() {		
			
		sc.leave();
		Platform.exit();
		System.exit(0);
			
	}
	
	/**
	 * Creates the folders if they don't exist
	 */
	public void createfolder() {
		
		File f = new File(LOCAL_PATH);
		
		if(!f.exists())
			f.mkdirs();
		
	}
	
	/**
	 * Add multiple observers
	 */
	private void addObservers() {
		
		this.localMonitor = new Monitor(LOCAL_PATH);
		addObservers(localListView, localMonitor);
		
	}
	
	/**
	 * Added observer to monitor and create ObservableList
	 * @param listView
	 * @param monitor
	 */
	private void addObservers(ListView<String> listView, Monitor monitor) {
		
		ObservableList<String> list = FXCollections.observableArrayList();
		
		ObserverList ol = new ObserverList(list);
		
		monitor.addObserver(ol);
		
		new Thread(monitor).start();
		
		listView.setItems(list);
		
	}
	
	/**
	 * Load file to play
	 */
	public void load() {
		
		try {
			
			//Check to see if we have anything in the list
			if(localListView.getItems().size() > 0) {
				
				String choice = localListView.getSelectionModel().getSelectedItem();
				
				//checks to see if choice is null. If so sets choice to first item
				if(choice == null) {
				
					localListView.getSelectionModel().select(0);
					
					choice = localListView.getSelectionModel().getSelectedItem();
				
				}
			
				//Open file
				localMonitor.openFile(LOCAL_PATH + "\\" + choice);
				
				//Get file
				File loading = localMonitor.getFile();
		
				//get file and set it as media
				Media media = new Media(loading.toURI().toURL().toString());
			
				//Close file
				localMonitor.closeFile();
				
				//Create media Player and set media
				MediaPlayer mp = new MediaPlayer(media);
				
				mp.setAutoPlay(false);
				
				//Add media Player to our mediaView
				this.mediaView.setMediaPlayer(mp);
			
				this.loaded = true;
				this.currentFileLoaded.setText(loading.getName());
			
			}
		}
		catch(Exception e) {
			
			e.getMessage();
			
		}
	}
	
	/**
	 * Play the media
	 * @param play
	 */
	public void play(ActionEvent play) {

		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				
				//Load file if not loaded
				if(!loaded) 
					load();

				//Play media if loaded
				if(!playing && loaded) {
				
					mediaView.getMediaPlayer().play();
		            playing = true;
		            
		            playButton.setText("Pause");
		            Color c = Color.web("#8702f4");
		            playButton.setTextFill(c);
					
				}
				//Pause media
				else if(loaded) {
					
					mediaView.getMediaPlayer().pause();
		            playing = false;
		            
		            playButton.setText("Play");
		            Color c = Color.web("#016521");
		            playButton.setTextFill(c);
					
				}
				
			}
		});
		
	}
	
	/**
	 * Stop media playing
	 * @param stop
	 */
	public void stop(ActionEvent stop) {
		
		if(playing) {
			
			mediaView.getMediaPlayer().stop();
			playing = false;
			
			playButton.setText("Play");
			
			loaded = false;
			
			currentFileLoaded.setText("");
			
		}
		
	}
	
	/**
	 * Upload media to server
	 * @param upload
	 */
	public void upload(ActionEvent upload) {
		
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				
				//choose file
				String in = new FileChooserUpload().upload();
				
				if(in == null)
					return;
				
				File file = new File(in);
				
				try {
					
					//open file get bytes
					localMonitor.openFile(in);
					
					//get the files bytes
					byte[] fileBytes = localMonitor.getFileBytes();
					
					//close file 
					localMonitor.closeFile();
					
					//upload to server
					sc.upload(file.getName(), fileBytes);

					
				} catch (IOException e) {
					
					e.printStackTrace();
				}
				
			}
			
		});
		
	}
	
	/**
	 * Download file from sever
	 * @param download
	 */
	public void download(ActionEvent download) {
		
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				
				String choice = serverListView.getSelectionModel().getSelectedItem();
				
				//checks to see if choice is null.
				if(choice == null) 
					return;
				
				//Download from server
				sc.download(choice);
				
			}
			
		});
		
	}

	/**
	 * update ObservableList for server
	 * @param files
	 */
	public void setServerList(File[] files) {
		
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				
				ObservableList<String> ol = FXCollections.observableArrayList();
				
				for(File file: files) {
					
					ol.add(file.getName());
					
				}
				
				serverListView.setItems(ol);
			}
			
		});
		
	}

	/*
	 * downloading song to local folder
	 */
	public void downloadMedia(String name, byte[] fileBytes) {
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				
				try {
					
					localMonitor.upload(name, fileBytes);
					
				} catch (IOException e) {
					
					e.printStackTrace();
				}
				
			}
			
		}).start();
		
	}
	
}
