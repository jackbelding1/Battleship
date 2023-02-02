package edu.msu.team9.proj1.Cloud;

import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.io.StringWriter;
import java.util.ArrayList;

import edu.msu.team9.proj1.Catalog;
import edu.msu.team9.proj1.Cloud.Models.ConnectionStatus;
import edu.msu.team9.proj1.Cloud.Models.GameResult;
import edu.msu.team9.proj1.Cloud.Models.PlayerTurn;
import edu.msu.team9.proj1.Cloud.Models.SetupCount;
import edu.msu.team9.proj1.Cloud.Models.UserStatus;
import edu.msu.team9.proj1.GameItem;
import edu.msu.team9.proj1.Grid;
import edu.msu.team9.proj1.MainActivity;
import edu.msu.team9.proj1.R;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class Cloud {
    //TODO: fill in the BASE_URL and modify any PATH if necessary
    //TODO: fill in the BASE_URL and modify any PATH if necessary

    //private static final String BASE_URL = "https://webdev.cse.msu.edu/~bosthoma/cse476/project2/";
    private static final String BASE_URL = "https://webdev.cse.msu.edu/~tomelie/cse476/project2/";
    //private static final String BASE_URL = "https://webdev.cse.msu.edu/~huangj78/cse476/project2/";

    public static final String SAVE_PATH = "game-save.php";
    public static final String DELETE_PATH = "delete_game.php";
    public static final String LOAD_PATH = "get_state.php";
    public static final String CATALOG_PATH = "get_games.php";
    public static final String UPDATE_PATH = "upload_state.php";
    public static final String LOGIN_PATH = "login.php";
    public static final String CREATE_PATH = "create_user.php";
    public static final String JOIN_PATH = "join_game.php";
    public static final String TURN_PATH = "check_turn.php";
    public static final String COUNT_PATH = "check_setupcount.php";
    public static final String CONNECTION_PATH = "check_connection.php";
    public static final String CREATE_GAME_PATH = "create_game.php";
    public static final String CHECK_JOINER = "check_joined.php";
    public static final String UPDATE_TURN_PATH = "update_turn.php";
    public static final String GET_COUNT = "check_setupcount.php";
    public static final String INC_COUNT = "increment_setup_count.php";
    private static final String MAGIC = "RFDDJKSPALSD!9RYUDGHQT314";
    private static final String UTF8 = "UTF-8";
    private static final String SERVER_ERROR = "android_getaddrinfo failed: EAI_NODATA (No address associated with hostname)";

    private String user;
    private String pw;

    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build();


    /**
     * An adapter so that list games can display a list of active games from the sql database
     */
    public static class CatalogAdapter extends BaseAdapter {

        private String username;
        private String password;
        private TextView textView;
        private boolean keepRunning = true;

        /**
         * The items we display in the list box. Initially this is
         * null until we get items from the server.
         */
        private Catalog catalog = new Catalog("", new ArrayList(), "");

        /**
         * Constructor
         */
        public CatalogAdapter(final View view) {
            // Create a thread to load the catalog
            new Thread(new Runnable() {

                boolean errorToast = false;
                String msg;
                boolean onEmptied = false;
                public void run() {
                    try {

                        while (keepRunning) {
                            catalog = getCatalog();

                            if (catalog.getStatus().equals("no")) {
                                msg = "Loading catalog returned status 'no'! Message is = '" + catalog.getMessage() + "'";
                                catalog.setItems(new ArrayList<GameItem>());
                            }
                            if (catalog.getItems().isEmpty()) {
                                if (!onEmptied){
                                    msg = "Catalog does not contain any games";
                                    catalog.setItems(new ArrayList<GameItem>());
                                    errorToast = true;
                                    onEmptied = true;
                                }
                            } else {
                                onEmptied = false;
                            }
                            view.post(new Runnable() {

                                @Override
                                public void run() {
                                    // Tell the adapter the data set has been changed
                                    notifyDataSetChanged();
                                    if (errorToast)
                                    {
                                        setText();
                                        String string;
                                        errorToast = false;
                                        Log.e("CatalogAdapter", "Something went wrong when loading the catalog");
                                        if (catalog.getMessage() == null) {
                                            string = msg;
                                        } else {
                                            string = catalog.getMessage();
                                        }
                                        Toast.makeText(view.getContext(), string, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            Thread.sleep(1000);

                        }
                    } catch (Exception e) {
                        // Error condition! Something went wrong
                        Log.e("CatalogAdapter", "Something went wrong when loading the catalog", e);
                    }
                }
            }).start();
        }


        public void stopThread(){
            keepRunning = false;
        }

        // Create a GET query
        public Catalog getCatalog() throws IOException, RuntimeException {

            BattleshipService service = retrofit.create(BattleshipService.class);

            Response response = service.get_games(MAGIC).execute();
            // check if request failed
            if (!response.isSuccessful()) {
                Log.e("getCatalog", "Failed to get catalog, response code is = " + response.code());
                return new Catalog("no", new ArrayList<GameItem>(), "Server error " + response.code());
            }
            Catalog catalog = (Catalog) response.body();
            if (catalog.getStatus().equals("no")) {
                String string = "Failed to get catalog, msg is = " + catalog.getMessage();
                Log.e("getCatalog", string);
                return new Catalog("no", new ArrayList<GameItem>(), string);
            };
            if (catalog.getItems() == null) {
                catalog.setItems(new ArrayList<GameItem>());
            }

            return catalog;

            //ArrayList<GameItem> game_list = new ArrayList<>();
            //GameItem game1 = new GameItem(2, "Prof's Game #1");
            //GameItem game2 = new GameItem(2, "Justin's Game #1");
            //game_list.add(game1);
            //game_list.add(game2);
            //catalog.setItems(game_list);
            //return catalog;
        }


        public int getId(int position)
        {
            return catalog.getItems().get(position).getId();
        }
        @Override
        public int getCount() {
            return catalog.getItems().size();
        }

        @Override
        public Object getItem(int position) {
            return catalog.getItems().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public String getName(int position){
            return catalog.getItems().get(position).getName();
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if(view == null) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.catalog_item, parent, false);
            }

            TextView tv = (TextView)view.findViewById(R.id.textItem);

            String ServerText = String.valueOf(catalog.getItems().get(position).getId()) + ": " + catalog.getItems().get(position).getName();

            tv.setText(ServerText);

            setText();

            return view;
        }

        public void setTextView(TextView view) {
            textView = view;
        }

        public void setText() {
            String text = "Current Active Games: " + String.valueOf(getCount());
            textView.setText(text);
        }
    }



    /**
     * Check if a user is present in the cloud.
     *
     * @param username name of user
     * @param password password of user
     * @return true if successful
     */
    public int checkUser(final String username, final String password) {

        BattleshipService service = retrofit.create(BattleshipService.class);
        try {

            //TODO: Create loginResult/userResult once more information needs to be retrieved

            Response<UserStatus> response = service.login(username, password).execute();

            // check if request failed
            if (!response.isSuccessful()) {
                Log.e("LoginUser", "Failed to login, response code is = " + response.code());
                return -1;
            }

            //check if status == "yes"
            UserStatus result = response.body();
            if (result.getStatus().equals("yes")) {
                user = username;
                pw = password;
                return result.getUserId();
            }

            Log.e("LoginUser", "Failed to login, message is = '" + result.getMessage() + "'");
            return -1;

        } catch (IOException e) {
            Log.e("LoginUser", "Exception occurred while logging in.", e);
            return -1;
        }

    }

    /**
     * Create a user
     *
     * @param username name of user
     * @param password password of user
     * @return true if successful
     */
    public boolean createUser(String username, String password) {
        username = username.trim();
        password = password.trim();
        if (username.length() == 0 || password.length() == 0) {
            return false;
        }

        // Create an XML packet with the information about the current image
        XmlSerializer xml = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            xml.setOutput(writer);
            xml.startDocument("UTF-8", true);
            xml.startTag(null, "battleship");

            xml.attribute(null, "username", username);
            xml.attribute(null, "password", password);

            xml.endTag(null, "battleship");

            xml.endDocument();

        } catch (IOException e) {
            return false;
        }

        BattleshipService service = retrofit.create(BattleshipService.class);
        final String xmlStr = writer.toString();

        try {
            UserStatus result = service.create_user(xmlStr).execute().body();
            if (result.getStatus() == null || !result.getStatus().equals("yes")) {
                Log.e("CreateUser", "Failed to create, message = '" + result.getMessage() + "'");
                return false;
            }
        } catch (IOException e) {
            Log.e("CreateUser", "Exception occurred while trying to create a new user.", e);
            return false;
        }

        user = username;
        pw = password;
        return true;
    }


    public boolean checkConnection(){
        // Create an XML packet with the information about the current image
        XmlSerializer xml = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            xml.setOutput(writer);
            xml.startDocument("UTF-8", true);
            xml.startTag(null, "battleship");
            xml.endTag(null, "battleship");
            xml.endDocument();

        } catch (IOException e) {
            return false;
        }

        BattleshipService service = retrofit.create(BattleshipService.class);
        final String xmlStr = writer.toString();

        try {
            ConnectionStatus result = service.check_connection(xmlStr).execute().body();
            if (result.getStatus() == null || !result.getStatus().equals("yes")) {
                Log.e("Connection", "Failed to check connection, message = '" + result.getMessage() + "'");
                return false;
            }
        } catch (StreamCorruptedException e)
        {
            Log.e("connection", "Failed to reach the server", e);
        }
        catch (IOException e) {
            String s = "";
            if (e.getCause() != null && e.getCause().getMessage() != null)
            {
                if (e.getCause().getMessage().equals(SERVER_ERROR))
                {
                    s = "Failed to connect";
                }
            }
            Log.e("Error", s);
            Log.e("Connection", "Exception occurred while to check connection status.", e);
            return true;
        }

        return true;
    }

    /**
     *
     * @return true if successful
     */
    public boolean joinGame(int gameid, int userid) {
        // Create an XML packet with the information about the current image
        XmlSerializer xml = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            xml.setOutput(writer);
            xml.startDocument("UTF-8", true);
            xml.startTag(null, "battleship");
            xml.attribute(null, "userid", String.valueOf(userid));
            xml.attribute(null, "gameid", String.valueOf(gameid));

            xml.endTag(null, "battleship");

            xml.endDocument();

        } catch (IOException e) {
            return false;
        }

        BattleshipService service = retrofit.create(BattleshipService.class);
        final String xmlStr = writer.toString();

        try {
            UserStatus result = service.join_game(xmlStr).execute().body();
            if (result.getStatus() == null || !result.getStatus().equals("yes")) {
                Log.e("JOIN GAME", "Failed to join, message = '" + result.getMessage() + "'");
                return false;
            }
        } catch (IOException e) {
            Log.e("JOIN GAME", "Exception occurred while trying to join game.", e);
            return false;
        }

        return true;
    }


    public int checkJoiner(final int gameid) {

        BattleshipService service = retrofit.create(BattleshipService.class);
        try {

            //TODO: Create loginResult/userResult once more information needs to be retrieved

            Response<UserStatus> response = service.get_joiner(String.valueOf(gameid)).execute();

            // check if request failed
            if (!response.isSuccessful()) {
                Log.e("CheckUser", "Failed to login, response code is = " + response.code());
                return -1;
            }

            //check if status == "yes"
            UserStatus result = response.body();
            if (result.getStatus().equals("yes")) {
                return result.getUserId();
            }

            Log.e("CheckUser", "Failed to login, message is = '" + result.getMessage() + "'");
            return -1;

        } catch (IOException e) {
            Log.e("CheckUser", "Exception occurred while logging in.", e);
            return -1;
        }

    }

    /**
     *
     * @return true if successful
     */
    public int getJoiner(int gameid) {
        // Create an XML packet with the information about the current image
        XmlSerializer xml = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            xml.setOutput(writer);
            xml.startDocument("UTF-8", true);
            xml.startTag(null, "battleship");
            xml.attribute(null, "gameid", String.valueOf(gameid));
            xml.endTag(null, "battleship");
            xml.endDocument();

        } catch (IOException e) {
            return -1;
        }

        BattleshipService service = retrofit.create(BattleshipService.class);
        final String xmlStr = writer.toString();

        try {
            UserStatus result = service.get_joiner(xmlStr).execute().body();
            if (result.getStatus() == null || !result.getStatus().equals("yes")) {
                Log.e("GET JOIN ID", "Failed, message = '" + result.getMessage() + "'");
                return -1;
            }
            return result.getUserId();
        } catch (IOException e) {
            Log.e("GET JOIN ID", "Exception occurred", e);
            return -1;
        }
    }


    public int incrementSetupCount(int gameid) {
        // Create an XML packet with the information about the current image
        XmlSerializer xml = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            xml.setOutput(writer);
            xml.startDocument("UTF-8", true);
            xml.startTag(null, "battleship");
            xml.attribute(null, "gameid", String.valueOf(gameid));

            xml.endTag(null, "battleship");

            xml.endDocument();

        } catch (IOException e) {
            return -1;
        }

        BattleshipService service = retrofit.create(BattleshipService.class);
        final String xmlStr = writer.toString();

        try {
            SetupCount result = service.incrementSetupCount(xmlStr).execute().body();
            if (result.getStatus() == null || !result.getStatus().equals("yes")) {
                Log.e("INC", "Failed to increment, message = '" + result.getMessage() + "'");
                return -1;
            }
            return result.getPosition();
        } catch (IOException e) {
            Log.e("INC", "Exception occurred while trying to increment.", e);
            return -1;
        }
    }

    public int deleteGame(int gameid) {
        // Create an XML packet with the information about the current image
        XmlSerializer xml = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            xml.setOutput(writer);
            xml.startDocument("UTF-8", true);
            xml.startTag(null, "battleship");
            xml.attribute(null, "gameid", String.valueOf(gameid));

            xml.endTag(null, "battleship");

            xml.endDocument();

        } catch (IOException e) {
            return -1;
        }

        BattleshipService service = retrofit.create(BattleshipService.class);
        final String xmlStr = writer.toString();

        try {
            UserStatus result = service.deleteGame(xmlStr).execute().body();
            if (result.getStatus() == null || !result.getStatus().equals("yes")) {
                Log.e("DELETE GAME", "Failed to delete, message = '" + result.getMessage() + "'");
                return -1;
            }
            return 1;
        } catch (IOException e) {
            Log.e("DELETE GAME", "Exception occurred while trying to delete.", e);
            return -1;
        }
    }

    public boolean update_turn(int gameid) {
        // Create an XML packet with the information about the current image
        XmlSerializer xml = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            xml.setOutput(writer);
            xml.startDocument("UTF-8", true);
            xml.startTag(null, "battleship");
            xml.attribute(null, "gameid", String.valueOf(gameid));

            xml.endTag(null, "battleship");

            xml.endDocument();

        } catch (IOException e) {
            return false;
        }

        BattleshipService service = retrofit.create(BattleshipService.class);
        final String xmlStr = writer.toString();

        try {
            UserStatus result = service.update_turn(xmlStr).execute().body();
            if (result.getStatus() == null || !result.getStatus().equals("yes")) {
                Log.e("INC", "Failed to increment, message = '" + result.getMessage() + "'");
                return false;
            }
        } catch (IOException e) {
            Log.e("INC", "Exception occurred while trying to increment.", e);
            return false;
        }

        return true;
    }


    /**
     *
     * @return true if successful
     */
    public boolean check_count(int gameid) {
        // Create an XML packet with the information about the current image
        XmlSerializer xml = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            xml.setOutput(writer);
            xml.startDocument("UTF-8", true);
            xml.startTag(null, "battleship");
            xml.attribute(null, "gameid", String.valueOf(gameid));
            xml.endTag(null, "battleship");
            xml.endDocument();

        } catch (IOException e) {
            return false;
        }

        BattleshipService service = retrofit.create(BattleshipService.class);
        final String xmlStr = writer.toString();

        try {
            UserStatus result = service.join_game(xmlStr).execute().body();
            if (result.getStatus() == null || !result.getStatus().equals("yes")) {
                Log.e("JOIN GAME", "Failed to join, message = '" + result.getMessage() + "'");
                return false;
            }
        } catch (IOException e) {
            Log.e("JOIN GAME", "Exception occurred while trying to join game.", e);
            return false;
        }

        return true;
    }


    /**
     * Create a game
     * @return true if successful
     */
    public int createGame(int userid) {
        // Create an XML packet with the information about the current image
        XmlSerializer xml = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            xml.setOutput(writer);
            xml.startDocument("UTF-8", true);
            xml.startTag(null, "game");
            xml.attribute(null, "userid", String.valueOf(userid));
            xml.endTag(null, "game");

            xml.endDocument();

        } catch (IOException e) {
            return -1;
        }

        BattleshipService service = retrofit.create(BattleshipService.class);
        final String xmlStr = writer.toString();

        try {
            GameResult result = service.create_game(xmlStr).execute().body();
            if (result.getStatus() == null || !result.getStatus().equals("yes")) {
                Log.e("Create Game", "Failed to create, message = '" + result.getMessage() + "'");
                return -1;
            }
            return result.getGameID();
        } catch (IOException e) {
            Log.e("Create Game", "Exception occurred while trying to create a new user.", e);
            return -1;
        }
    }


    /**
     * Open a connection to a game in the cloud.
     *
     * @param gameid id for the game
     * @return list of Grid information
     */
    public String loadGame(int gameid) {
        BattleshipService service = retrofit.create(BattleshipService.class);
        try {
            Response<GameResult> response = service.get_state(String.valueOf(gameid)).execute();

            if (!response.isSuccessful()) {
                String msg = "Failed to load the game, response code is = " + response.code();
                throw new Exception(msg);
            }

            GameResult result = response.body();
            if (result.getStatus().equals("yes")) {
                return result.getGameState();
            }
            String msg = "Failed to load the game! Message is = " + response.code();
            return "Error";
        } catch (Exception e) {
            // Error condition! Something went wrong
            //Log.e("LoadGame", "Exception occurred while loading the game!", e);
            return "ConnectionError";
        }
    }

    public boolean saveGame(String gamestate, int gameid) {
        // Create an XML packet with the information about the current image
        XmlSerializer xml = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            xml.setOutput(writer);

            xml.startDocument("UTF-8", true);

            xml.startTag(null, "battleship");
            xml.attribute(null, "gamestate", gamestate);
            xml.attribute(null, "id", String.valueOf(gameid));
            xml.endTag(null, "battleship");

            xml.endDocument();
        } catch (IOException e) {
            // This won't occur when writing to a string
            return false;
        }

        BattleshipService service = retrofit.create(BattleshipService.class);
        final String xmlStr = writer.toString();
        try {
            Response<GameResult> response = service.update_state(writer.toString()).execute();
            if (response.isSuccessful()) {
                GameResult result = response.body();
                if (result.getStatus() != null && result.getStatus().equals("yes")) {
                    return true;
                }
                Log.e("SaveGame", "Failed to save, message = '" + result.getMessage() + "'");
                return false;
            }
            Log.e("SaveGame", "Failed to save, message = '" + response.code() + "'");
            return false;
        } catch (IOException e) {
            Log.e("SaveGame", "Exception occurred while trying to save game!", e);
            return false;
        } catch (RuntimeException e) {	// to catch xml errors to help debug step 6
            Log.e("SaveGame", "Runtime Exception: " + e.getMessage());
            return false;
        }
//        return true;
    }


    public int getPlayerTurn(int gameid) {
        // Create an XML packet with the information about the current image
        XmlSerializer xml = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            xml.setOutput(writer);
            xml.startDocument("UTF-8", true);
            xml.startTag(null, "battleship");
            xml.attribute(null, "gameid", String.valueOf(gameid));

            xml.endTag(null, "battleship");

            xml.endDocument();

        } catch (IOException e) {
            return -1;
        }

        BattleshipService service = retrofit.create(BattleshipService.class);
        final String xmlStr = writer.toString();

        PlayerTurn result;

        try {
            result = service.check_turn(xmlStr).execute().body();
            if (result.getStatus() == null || !result.getStatus().equals("yes")) {
                Log.e("Check Turn", "Failed to check turn, message = '" + result.getMessage() + "'");
                return -1;
            }
        } catch (IOException e) {
            Log.e("Check Turn", "Exception occurred while trying to check turn.", e);
            return -1;
        }

        return result.getTurn();
    }

    public int getSetupCount(int gameid) {
        // Create an XML packet with the information about the current image
        XmlSerializer xml = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            xml.setOutput(writer);
            xml.startDocument("UTF-8", true);
            xml.startTag(null, "battleship");
            xml.attribute(null, "gameid", String.valueOf(gameid));

            xml.endTag(null, "battleship");

            xml.endDocument();

        } catch (IOException e) {
            return -1;
        }

        BattleshipService service = retrofit.create(BattleshipService.class);
        final String xmlStr = writer.toString();

        SetupCount result;

        try {
            result = service.check_setupCount(xmlStr).execute().body();
            if (result.getStatus() == null || !result.getStatus().equals("yes")) {
                Log.e("Check Setup Count", "Failed to check setup count, message = '" + result.getMessage() + "'");
                return -1;
            }
        } catch (IOException e) {
            Log.e("Check Setup Count", "Exception occurred while trying to check setup count.", e);
            return -1;
        }

        return result.getSetupCount();
    }

    public static void skipToEndTag(XmlPullParser xml)
            throws IOException, XmlPullParserException {
        int tag;
        do {
            tag = xml.next();
            if (tag == XmlPullParser.START_TAG) {
                // Recurse over any start tag
                skipToEndTag(xml);
            }
        } while (tag != XmlPullParser.END_TAG &&
                tag != XmlPullParser.END_DOCUMENT);
    }


}
