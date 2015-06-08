package gpssender.client.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;



public class PreferenceUtils {

    public static void saveSingin(Context context, boolean isSignin){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isSignin", isSignin);
        editor.commit();
    }

    //получаем пользовательские данные
    public static boolean isSignin(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("isSignin", false);
    }
}
