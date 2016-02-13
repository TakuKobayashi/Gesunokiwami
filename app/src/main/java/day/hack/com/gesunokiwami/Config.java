package day.hack.com.gesunokiwami;

public class Config {
	public static final String TAG = "gesu";
	public static final String ROOT_URL = "http://battle7.elasticbeanstalk.com/";
	public static final String SOCKET_SERVER_ROOT_URL = "http://10.10.58.124:8081/";

	public static int GRAB_STATE = 0;
	public static int OPEN_STATE = 1;
	public static int RESET_STATE = 4;

	public enum GrabState{
		touch,
		surprise,
		pervert,
		monster
	}
}
