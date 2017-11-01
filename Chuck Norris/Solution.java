import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Solution {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        String MESSAGE = in.nextLine();
        
        String code = "";
        
        for(int i = 0; i<MESSAGE.length(); i++) {
            String temp = Integer.toBinaryString(MESSAGE.charAt(i));
            while (temp.length() < 7)
                temp = "0" + temp;
            code += temp;
        }
        
        Character lastBit = ' ';
        int pos = 0;
        String answer = "";
        
        while(pos < code.length()) {
            if(code.charAt(pos) != lastBit)
                answer += (code.charAt(pos) == '1') ? " 0 0" : " 00 0";
            else
                answer += "0";
                
            lastBit = code.charAt(pos);
            pos++;
        }
        
        answer = answer.substring(1, answer.length());
        System.out.println(answer);
    }
}
