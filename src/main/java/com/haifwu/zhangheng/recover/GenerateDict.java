package com.haifwu.zhangheng.recover;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

import java.io.*;
import java.util.HashMap;

/**
 * Usage of this class.
 *
 * @author Wu Haifeng
 * @CreateDate: 28/3/17
 */
public class GenerateDict {
    private long totalWords = 0;
    private String excel_input;
    private String dict_output;
    private HashMap<String,Integer> wordABCount = new HashMap<String,Integer>(); //count words A B times
    private HashMap<String,Integer> singleWordCount = new HashMap<String,Integer>(); // count word show up times
    private HashMap<String,Double> proAB = new HashMap<String,Double>(); //record the probability of AB pro(AB) = count(A,B)/MAX(count(A*),count(*B))

    private GenerateDict(String excel_input, String dict_output){
        this.excel_input = excel_input;
        this.dict_output = dict_output;
    }

    private void countAB() throws IOException,BiffException {
        WorkbookSettings settings = new WorkbookSettings();
        settings.setEncoding("ISO-8859-1");
        Workbook wb = Workbook.getWorkbook(new File(this.excel_input), settings);
        Sheet sheet = wb.getSheet(0);
        int row_number = sheet.getRows(), column_number = sheet.getColumns();
        System.out.println("row_number=" + row_number + "  column_number=" + column_number);
        for(int i = 1 ; i < row_number ; i++){ // For each row, escape the first row
            for (int k = 0 ; k < column_number; k++){  //For each column
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
                this.totalWords += words.length;
            }
        }
        for (String key : singleWordCount.keySet()) {
            updateSingleWordProbability(key, 0);
        }
    }

    private void increaseHashMapCountValue(HashMap<String, Integer> hashMap, String key){
        if(key.length() < 1) return;
        if(hashMap.containsKey(key)){
            hashMap.put(key, hashMap.get(key) + 1);
        } else {
            hashMap.put(key, 1);
        }
    }


    //calculate the probability word A appears ,then B
    private void countProAB() throws IOException,FileNotFoundException,BiffException {
        for (String key : wordABCount.keySet()) {
            String[] words = key.split("\\s+");
            if(words.length != 2){
                System.out.println("Error happened when split " + key);
            } else {
                try{
                    // According to Naive Bayes: P(AB) = P(A)* P(B|A) = P(B) * P(A|B)
                    // P(A|B) = P(AB) / P(B); P(B|A) = P(AB) / P(A)
                    // Here we use max(p(B|A, p(A|B)）
                    double proA_B = wordABCount.get(key) * 1.0 / singleWordCount.get(words[1]);
                    double proB_A = wordABCount.get(key) * 1.0 / singleWordCount.get(words[0]);
                    this.proAB.put(key, Math.max(proA_B, proB_A));
                    updateSingleWordProbability(words[0], proB_A);
                    updateSingleWordProbability(words[1], proA_B);
                } catch (NullPointerException e){
                    System.out.println("NullPointerException happen when key is " + key);
                }
            }
        }
    }

    private void updateSingleWordProbability(String word, double notSingleProbability){
        if(this.proAB.containsKey(word)){
            this.proAB.put(word, this.proAB.get(word) - notSingleProbability);
        } else {
            this.proAB.put(word, 1.0);
        }
    }

    private void run() throws IOException,BiffException{
        countAB();          //calculate AB
        countProAB();       //calculate the probability
        dumpProbability(this.proAB);
    }

    private void dumpProbability(HashMap<String, Double> hashMap) throws IOException {
        PrintWriter writer = new PrintWriter(this.dict_output, "ISO-8859-1");
        for(String key : hashMap.keySet()){
            writer.write(key + "\t" + hashMap.get(key) + "\n");
        }
        writer.close();
    }

    public static void main(String[] args) throws IOException, BiffException {
        if(args.length < 2){
            System.out.println("Usage: GenerateDic excel_file dict_file");
            return;
        }
        String excel_file = args[0], dict_file = args[1];
        GenerateDict convert = new GenerateDict(excel_file, dict_file);
        convert.run();
    }

}
