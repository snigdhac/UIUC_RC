package edu.illinois.cs.cogcomp;

import java.util.List;

/**
 * Created by snigdha on 8/8/16.
 */
public class Utils {
    public static String printArrayList(List a){
        String temp = "";
        for(int i=0;i<a.size();i++)
            temp = temp + a.get(i)+" ";
        return temp;
    }
}
