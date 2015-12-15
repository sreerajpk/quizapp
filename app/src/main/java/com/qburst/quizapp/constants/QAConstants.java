package com.qburst.quizapp.constants;

public class QAConstants {

	// SplashScreen Constant
	public static final int SPLASH_TIME_OUT = 3000;

	// Program Constants
	public static final int QUESTIONS_IN_A_SECTION = 5;
	public static final int MARKS_FOR_A_QUESTION = 5;
	public static final double FRACTION_OF_POINTS_TO_USE_FOR_FREQUENT_PLAYER_INCREMENT = 0.4;
	public static final double FRACTION_OF_POINTS_TO_USE_FOR_DISPLAYING_TROPHY = 0.8;

	// Database Constants
	public static final String DATABASE_NAME = "quizapp.db";
	public static final String DB_PATH = "/data/data/com.qburst.quizapp/databases/";
	public static final int DATABASE_VERSION = 1;
	public static final int DB_EMPTY = 0;
	public static final String FETCH_ALL = "SELECT * FROM ";
	public static final String SECTIONS_TABLE = "Sections";
	public static final String SCORE_TABLE_NAME = "Score";
	public static final String ACHIEVEMENTS_TABLE_NAME = "Achievements";

	// Parse Constants
	public static final String PARSE_APP_ID = "0dHTKMnBERBI4djpMi3iwoyuC5bfLHtDP07gLCRD";
	public static final String PARSE_CLIENT_KEY = "DKbFA41ePxLr1h4cNBxjczR9gGInkpDSqusFxQ7d";

	// Google Play Games Constants
	public static final String PLAY_GAMES_APP_ID = "776236807538";
	public static final int REQUEST_LEADERBOARD = 100;
	public static final int REQUEST_ACHIEVEMENTS = 100;
	public static final String QUIZAPP_SCORE_LEADERBOARD = "CgkI8urG2ssWEAIQAQ";
	public static final String QUIZAPP_FREQUENT_PLAYER_ACHIEVEMENT = "CgkI8urG2ssWEAIQAw";
	public static final String QUIZAPP_AMATEUR_ACHIEVEMENT = "CgkI8urG2ssWEAIQBg";
	public static final String QUIZAPP_EXPERT_ACHIEVEMENT = "CgkI8urG2ssWEAIQBA";
	public static final String QUIZAPP_MASTER_ACHIEVEMENT = "CgkI8urG2ssWEAIQBQ";
	public static final int STEPS_TO_INCREMENT = 2;

	// Facebook Constant
	public static final String FACEBOOK_APP_ID = "1508083336135098";
	public static final String FACEBOOK_APP_ICON_URL = "https://cdn4.iconfinder.com/data/icons/customicondesignoffice2/256/FAQ.png";
	public static final String FACEBOOK_REDIRECT_URL = "https://developers.facebook.com/apps/1508083336135098/";

	// Twitter Constant
	public static final String TWITTER_CONSUMER_KEY = "BNgFHTbwZFoTAaIYznLBiTU4S";
	public static final String TWITTER_CONSUMER_SECRET = "30lKloVwZcTubyzAPmKApQsBCfU1Lb14pUNCrIqpWMClLzqOiv";
	public static final String TWITTER_CALLBACK_URL = "http://www.qburst.com/";
	public static final String TWITTER_OAUTH_VERIFIER = "oauth_verifier";
	public static final String TWITTER_AUTHENTICATION_URL = "twitter_authentication_url";

	// Strings related to background music
	public static final String MUSIC = "music";
	public static final int MAIN_BACKGROUND_MUSIC = 1;
	public static final int QUIZ_RUNNING_MUSIC = 2;
	public static final int QUIZ_RESULT_MUSIC = 3;
	public static final int QUIZ_NULL = 0;

	// Difficulty level String constants
	public static final String AMATEUR = "amateur";
	public static final String EXPERT = "expert";
	public static final String MASTER = "master";

	// Signed In constants
	public static final int PLAY_GAMES_SIGNED_IN = 1;
	public static final int PLAY_GAMES_NOT_SIGNED_IN = 0;

	// Preference Constants
	public static String SETTINGS_PREF_NAME = "settings";
	public static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLoggedIn";
	public static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
	public static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
	public static final String PREF_USER_NAME = "twitter_user_name";

	public static final String BACK_FROM_DIFFICULTY_LEVEL = "backFromDifficultyLevel";

	public static final String DEFAULT_STATUS = "defaultStatus";
}
