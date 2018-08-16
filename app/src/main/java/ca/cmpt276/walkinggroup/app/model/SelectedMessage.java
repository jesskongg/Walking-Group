package ca.cmpt276.walkinggroup.app.model;

public class SelectedMessage {

    private static String fromName;
    private static String message;

    public static String[] fromNameArray;
    public static String[] messageArray;

    public static void setFullMessage(int position){
        SelectedMessage.fromName = fromNameArray[position];
        SelectedMessage.message = messageArray[position];
    }

    public static String getFromName() {
        return fromName;
    }

    public static String getMessage() {
        return message;
    }

    public static void setFromNameArray(String[] fromNameArray) {
        SelectedMessage.fromNameArray = fromNameArray;
    }

    public static void setMessageArray(String[] messageSubjectArray) {
        SelectedMessage.messageArray = messageSubjectArray;
    }

}
