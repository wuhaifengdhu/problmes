/**
 * Created by haifwu on 12/7/16.
 */
import java.io.*;
import java.util.*;

import com.google.common.io.Resources;
import jxl.*;
import jxl.read.biff.BiffException;
import jxl.write.*;

import java.util.ArrayList;
import java.util.HashMap;


public class Test{
    private static final String DATA_FILE_NAME = "data3.xls";

    private static HashMap<String,Integer> wordABCount = new HashMap<String,Integer>(); //count words A B times
    private static HashMap<String,Integer> singleWordCount = new HashMap<String,Integer>(); // count word show up times
    private static HashMap<String,Double> proAB = new HashMap<String,Double>(); //record the probability of AB pro(AB) = count(A,B)/MAX(count(A*),count(*B))

    public static void main(String[] args) throws IOException, BiffException, WriteException {
        training();
    }

    public static void countAB () throws IOException,BiffException{
        Workbook wb = Workbook.getWorkbook(new File(Resources.getResource(DATA_FILE_NAME).getFile()));
        Sheet sheet = wb.getSheet(0);
        for(int i = 2 ; i < 42540 ; i++){ // For each row
            for (int k = 0 ; k < 57; k++){  //For each column
                String[] words = sheet.getCell(k,i).getContents().trim().split("\\s+"); // split by non-word characters
                for(int j = 0; j < words.length - 1; j++){
                    //1. Count each word
                    increaseHashMapCountValue(singleWordCount, words[j]);
                    //2. Count continue words
                    increaseHashMapCountValue(wordABCount, words[j]+ " " + words[j + 1]);
                }
                if(words.length > 0){
                    increaseHashMapCountValue(singleWordCount, words[words.length - 1]);
                }
            }
        }
    }

    private static void increaseHashMapCountValue(HashMap<String, Integer> hashMap, String key){
        if(hashMap.containsKey(key)){
            hashMap.put(key, hashMap.get(key) + 1);
        } else {
            hashMap.put(key, 1);
        }
    }


    //calculate the probability word A appears ,then B
    private static void countProAB() throws IOException,FileNotFoundException,BiffException {
        for (String key : wordABCount.keySet()) {
            String[] words = key.split("\\s+");
            if(words.length != 2){
                System.out.println("Error happened when split " + key);
            } else {
                try{
                    proAB.put(key, wordABCount.get(key) * 1.0 / Math.min(singleWordCount.get(words[0]), singleWordCount.get(words[1])));
                } catch (NullPointerException e){
                    System.out.println("NullPointerException happen when key is " + key);
                }

            }
        }

    }

    private static void training() throws IOException,FileNotFoundException,BiffException{
        countAB();          //calculate AB
        countProAB();       //calculate the probability
        dumpProbability(proAB);
    }

    public static void dumpProbability(HashMap<String, Double> hashMap){
        for(String key : hashMap.keySet()){
            System.out.println(key + ": " + hashMap.get(key));
        }
    }
}
